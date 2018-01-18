package com.bitsanity.bitchange.server.spring_boot.jmx;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.support.MetricType;

import com.bitsanity.bitchange.server.spring_boot.crypto.KeyManagementService;
import com.bitsanity.bitchange.utils.IntervalClock;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Concrete implementations should uncomment the following
 * Required annotation to access MBeans during JUnit run
 */
//@EnableMBeanExport(registration = RegistrationPolicy.REPLACE_EXISTING)
//@Component
//@ManagedResource(objectName = "com.bitsanity.bitchange:name=TestServicesServerStatistics", description = "DAT Data Services Statistics.")
public abstract class AbstractServerStatistics<E extends AbstractRollingStatistics> {

	private final Logger logger = CustomLoggerFactory.getLogger(AbstractServerStatistics.class);

	// web services
	private long startTime = System.currentTimeMillis();
	private AtomicLong totalRequests = new AtomicLong(0);
	private AtomicLong successfulRequests = new AtomicLong(0);
	private AtomicLong failedRequests = new AtomicLong(0);
	private AtomicLong rejectedRequests = new AtomicLong(0);
	private AtomicInteger currentRequests = new AtomicInteger(0);
	
	@Autowired
	KeyManagementService keyManagementService;

	@Value("${application.version}")
	private String buildVersion;
	@Value("${statistics.interval.count:6}")
	private int statCount;
	@Value("${statistics.interval.duration.minutes:10}")
	private int statIntervalDuration;
	@Value("${statistics.log.marker:STATISTICS}")
	private String loggingMarker;

	private CircularFifoQueue<E> rollingStats;
	private IntervalClock intervalClock;
	private ObjectMapper mapper = new ObjectMapper();

	private Object semaphore = new Object();

	@PostConstruct
	private void init() {
		// sanity check properties
		if (statCount < 1) {
			throw new IllegalArgumentException("The property specifying the number of rolling metrics (statistics.interval.count) must be greater than zero; value supplied: "
					+ statCount);
		}
		
		if (statIntervalDuration < 1) {
			throw new IllegalArgumentException("The property specifying the rolling metrics interval duration (statistics.interval.duration.minutes) must be greater than zero; value supplied: "
					+ statIntervalDuration);
		}

		// initialize stats rolling queue
		logger.debug("Creating rolling statistics queue of size: " + statCount);
		rollingStats = new CircularFifoQueue<E>(statCount);

		// convert minute intervals to seconds
		intervalClock = new IntervalClock(statIntervalDuration * 60);
	}
	

	/***************************
	 * 
	 * SERVER METRICS
	 * 
	 ***************************/
	
	/**
	 * Current uptime:
	 * http://localhost:9191/jolokia/read/com.bitsanity.bitchange.jmx:name=
	 * TestServicesServerStatistics/Uptime
	 */
	@ManagedMetric(description = "Current system uptime.", displayName = "Current system uptime.", metricType = MetricType.COUNTER, currencyTimeLimit = 10)
	public String getUptime() {
		long upTime = System.currentTimeMillis() - startTime;

		long days = TimeUnit.MILLISECONDS.toDays(upTime);
		long hoursRaw = TimeUnit.MILLISECONDS.toHours(upTime);
		long minutesRaw = TimeUnit.MILLISECONDS.toMinutes(upTime);

		// convert to string
		return String.format("%d days, %d hrs, %d min, %d sec", days, hoursRaw - TimeUnit.DAYS.toHours(days), minutesRaw - TimeUnit.HOURS.toMinutes(hoursRaw),
				TimeUnit.MILLISECONDS.toSeconds(upTime) - TimeUnit.MINUTES.toSeconds(minutesRaw));
	}

	@ManagedMetric(description = "Server version information.", displayName = "Server Version.")
	public String getServerVersion() {
		return buildVersion;
	}

	@ManagedMetric(description = "Current interval start time.", displayName = "Current interval start time.", metricType = MetricType.COUNTER, currencyTimeLimit = 120)
	public String getIntervalStart() {
		long start = intervalClock.getIntervalStartMillis(intervalClock.getCurrentInterval());

		// format
		SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss z");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));

		return df.format(new Date(start));
	}

	@ManagedMetric(description = "Current interval duration setting.", displayName = "Current interval duration setting.", unit = "minutes")
	public int getIntervalDuration() {
		return statIntervalDuration;
	}
	
	/***************************
	 * 
	 * KEY/USER METRICS/SERVICES
	 * 
	 ***************************/
	
	@ManagedMetric(description = "Timestamp of last key refresh.", displayName = "Timestamp of last key refresh.")
	public String getLastKeyRefresh() {
		return keyManagementService.getLastRefresh();
	}

	@ManagedMetric(description = "Key refresh interval in minutes.", displayName = "Key refresh interval.", unit = "minutes")
	public int getKeyRefreshInterval() {
		return keyManagementService.getRefreshInterval();
	}
	
	@ManagedMetric(description = "Count of currently known clients.", displayName = "Count of currently known clients.", metricType = MetricType.GAUGE)
	public int getKnownClientCount() {
		return keyManagementService.getKnownClientCount();
	}
	
	@ManagedOperation(description="Refresh client keys, if soak threshold has passed.") 
	public boolean refreshClientKeys() {
		Exception ex = new Exception();
		return keyManagementService.refreshKeysPriviledged(ex.getStackTrace());
	}



	/***************************
	 * 
	 * REQUEST METRICS
	 * 
	 ***************************/

	@ManagedMetric(description = "Count of submitted requests.", displayName = "Count of submitted requests.", metricType = MetricType.COUNTER)
	public long getRequests() {
		return totalRequests.longValue();
	}

	public void incrementRequests() {
		totalRequests.incrementAndGet();

		// update rolling statistics
		getCurrentRollingStatistic().incrementRequest();
	}

	@ManagedMetric(description = "Count of successful requests.", displayName = "Count of successful requests.", metricType = MetricType.COUNTER)
	public long getSuccess() {
		return successfulRequests.longValue();
	}

	public void incrementSuccess() {
		successfulRequests.incrementAndGet();

		// update rolling statistics
		getCurrentRollingStatistic().incrementSuccess();
	}

	@ManagedMetric(description = "Count of failed requests.", displayName = "Count of failed requests.", metricType = MetricType.COUNTER)
	public long getFailures() {
		return failedRequests.longValue();
	}

	public void incrementFailures() {
		failedRequests.incrementAndGet();
	}

	@ManagedMetric(description = "Count of requests rejected for invalid authorization status.", displayName = "Count of requests rejected for invalid authorization status.", metricType = MetricType.COUNTER)
	public long getRejections() {
		return rejectedRequests.longValue();
	}

	public void incrementRejections() {
		rejectedRequests.incrementAndGet();

		// update rolling statistics
		getCurrentRollingStatistic().incrementRejection();
	}

	@ManagedMetric(description = "Count of currently processing requests.", displayName = "Count of currently processing requests.", metricType = MetricType.GAUGE)
	public long getCurrentRequests() {
		return currentRequests.longValue();
	}

	public void incrementCurrentRequests() {
		currentRequests.incrementAndGet();
	}

	public void decrementCurrentRequests() {
		currentRequests.decrementAndGet();
	}

	@ManagedMetric(description = "Update Manager Statistics.", displayName = "Update Manager Statistics.", category = "performance")
	public AbstractRollingStatistics[] getRollingStatistics() {
		return rollingStats.toArray(new AbstractRollingStatistics[rollingStats.size()]);
	}

	/*pkg*/ void logStatistics(AbstractRollingStatistics stat) {
		if ((loggingMarker != null) && (loggingMarker.length() > 0)) {
			// log only if specified
			try {
				logger.audit(loggingMarker + ": " + mapper.writeValueAsString(stat));
			} catch (JsonProcessingException e) {
				logger.error("Unable to generate JSON response.", e);
			}
		}
	}

	public void logStatistics() {
		if ((loggingMarker != null) && (loggingMarker.length() > 0)) {
			rollingStats.forEach((stat) -> logStatistics(stat));
		}
	}

	protected final E getCurrentRollingStatistic() {
		// determine current interval
		long currentInterval = intervalClock.getCurrentInterval();

		// get most recent from queue tail
		E currentStats = null;
		if (!rollingStats.isEmpty()) {
			currentStats = rollingStats.get(rollingStats.size() - 1);
		}

		// validate
		if ((currentStats == null)
				// if not matching current interval
				|| (currentStats.getCurrentInterval() != currentInterval)) {

			// sync block on creation only
			synchronized (semaphore) {
				// test again
				if (!rollingStats.isEmpty()) {
					currentStats = rollingStats.get(rollingStats.size() - 1);
				}

				// make sure it wasn't created while awaiting block
				if ((currentStats == null)
						// if not matching current interval
						|| (currentStats.getCurrentInterval() != currentInterval)) {

					// create new entry
					currentStats = constructNewStatistic(intervalClock.getCurrentInterval(), intervalClock); 
							//new TestRollingStatistics(intervalClock.getCurrentInterval(), intervalClock);

					// log if rollover will occur
					if (rollingStats.size() == rollingStats.maxSize()) {
						logStatistics(rollingStats.peek());
					}

					// add to queue
					rollingStats.add(currentStats);
				}
			}
			logger.debug("Adding new rollings statistics entry for interval " + currentStats.getCurrentInterval());
		}

		return currentStats;
	}
	
	abstract E constructNewStatistic(long currentInterval, IntervalClock intervalClock);
}
