package com.bitsanity.bitchange.server.spring_boot.jmx;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.math3.stat.descriptive.SynchronizedSummaryStatistics;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.support.MetricType;

import com.bitsanity.bitchange.utils.IntervalClock;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Concrete implementations should uncomment the following
 * Required annotation to access MBeans during JUnit run
 */
//@ManagedResource(objectName = "com.bitsanity.bitchange.jmx:name=TestRollingStatistics", description = "JMX Rolling Statistics.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractRollingStatistics {

	// web services
	private AtomicLong requests = new AtomicLong(0);
	private AtomicLong success = new AtomicLong(0);
	private AtomicLong rejections = new AtomicLong(0);

	private long interval;
	private IntervalClock clock;
	
	private final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss z").withZone(ZoneId.of("Z"));

	/**
	 * All requests types
	 */
	private SynchronizedSummaryStatistics requestProcessingStatistics = new SynchronizedSummaryStatistics();
	
	
	
	/* pkg */ AbstractRollingStatistics(long interval, IntervalClock clock) {
		this.interval = interval;
		this.clock = clock;
	}

	/**
	 * Current interval
	 */
	@ManagedMetric(description = "Interval start time.", displayName = "Interval start time.", metricType = MetricType.COUNTER)
	public String getIntervalStart() {
		long start = clock.getIntervalStartMillis(interval);

		return dtFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.of("UTC")));
	}

	@ManagedMetric(description = "Count of the number of requests.", displayName = "Count of the number of requests.", metricType = MetricType.COUNTER)
	public long getSubmittedRequests() {
		return requests.longValue();
	}

	@ManagedMetric(description = "Count of the number of succesfully processed requests.", displayName = "Count of the number of succesfully processed requests.", metricType = MetricType.COUNTER)
	public long getSuccessfulRequests() {
		return success.longValue();
	}

	@ManagedMetric(description = "Count of the number of rejected requests.", displayName = "Count of the number of rejected requests.", metricType = MetricType.COUNTER)
	public long getRejectedRequests() {
		return rejections.longValue();
	}

	@ManagedMetric(description = "Number of all requests.", displayName = "Number of all requests.", metricType = MetricType.COUNTER)
	public double getRequestProcessingStatisticsCount() {
		return requestProcessingStatistics.getN();
	}

	@ManagedMetric(description = "Average duration of all requests", displayName = "Average duration of all requests", metricType = MetricType.GAUGE)
	public double getRequestProcessingStatisticsAverage() {
		return requestProcessingStatistics.getMean();
	}

	@ManagedMetric(description = "Standard deviation of all requests.", displayName = "Standard deviation of all requests", metricType = MetricType.GAUGE)
	public double getRequestProcessingStatisticsSTD() {
		return requestProcessingStatistics.getStandardDeviation();
	}

	public long getCurrentInterval() {
		return interval;
	}
	
	public long getIntervalDuration() {
		return clock.getIntervalPeriod();
	}

	@JsonProperty("@type")
	protected String getJsonType() {
		return null;
	}

	/*pkg*/ void incrementRequest() {
		requests.incrementAndGet();
	}

	/*pkg*/ void incrementSuccess() {
		success.incrementAndGet();
	}

	/*pkg*/ void incrementRejection() {
		rejections.incrementAndGet();
	}

	/*pkg*/ void addRequestProcessingStatistics(long duration) {
		requestProcessingStatistics.addValue(duration);
	}
}
