package com.bitsanity.bitchange.server.spring_boot.jmx;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.support.MetricType;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.stereotype.Component;

import com.bitsanity.bitchange.server.spring_boot.bitcoin.PendingTransactionTracker;
import com.bitsanity.bitchange.utils.IntervalClock;

/*
 * Required annotation to access MBeans during JUnit run
 */
@EnableMBeanExport(registration = RegistrationPolicy.REPLACE_EXISTING)

@Component
@ManagedResource(objectName = "com.bitsanity.bitchange.jmx:name=ExchangerServerStatistics", description = "DAT Data Services Statistics.")
public class ExchangerServerStatistics extends AbstractServerStatistics<RollingStatistics> {

	//@Autowired
	private PendingTransactionTracker pendingTransactions = null;
	
	//incoming coin
	private AtomicInteger incomingCoinNotifications = new AtomicInteger(0);
	private AtomicInteger pendingCoinReceipts = new AtomicInteger(0);
	private AtomicInteger processingCoinReceipts = new AtomicInteger(0);
	private AtomicInteger failedCoinReceipts = new AtomicInteger(0);
	private AtomicInteger pendingExchanges = new AtomicInteger(0);
	private AtomicInteger successfulExchanges = new AtomicInteger(0);
	private AtomicInteger failedExchanges = new AtomicInteger(0);
	private DoubleAdder etherAmount = new DoubleAdder();
	private LongAdder brokerFeeAmount = new LongAdder();
	private Set<String> watchedAddresses = new HashSet<>();

	//private final Logger logger = CustomLoggerFactory.getLogger(ExchangerServerStatistics.class);

	/***************************
	 * 
	 * INCOMING COIN METRICS
	 * 
	 ***************************/

	public void addCoinReceipt(long amount) {
		incomingCoinNotifications.incrementAndGet();
		getCurrentRollingStatistic().addIncomingAmountStatistics(amount);
	}

	@ManagedMetric(description = "Count of incoming coin notifications.", displayName = "Count of incoming coin notifications.", metricType = MetricType.COUNTER)
	public long getCoinNotificationCount() {
		return incomingCoinNotifications.longValue();
	}

	public void incrementPendingReceipts() {
		pendingCoinReceipts.incrementAndGet();
	}

	public void decrementPendingReceipts() {
		pendingCoinReceipts.decrementAndGet();
	}

	@ManagedMetric(description = "Count of pending coin receipts.", displayName = "Count of pending coin receipts.", metricType = MetricType.GAUGE)
	public long getPendingReceiptCount() {
		return pendingCoinReceipts.longValue();
	}

	public void incrementProcessingExchange() {
		processingCoinReceipts.incrementAndGet();		
	}
	
	public void decrementProcessingExchange() {
		processingCoinReceipts.decrementAndGet();		
	}
	
	@ManagedMetric(description = "Count of currently processing coin receipts.", displayName = "Count of currently processing coin receipts.", metricType = MetricType.GAUGE)
	public long getProcessingReceiptCount() {
		return processingCoinReceipts.longValue();
	}

	public void incrementFailedReceipts() {
		failedCoinReceipts.incrementAndGet();
	}
	
	@ManagedMetric(description = "Count of failed coin receipts.", displayName = "Count of failed coin receipts.", metricType = MetricType.GAUGE)
	public long getFailedReceiptCount() {
		return failedCoinReceipts.longValue();
	}

	public void incrementPendingExchange() {
		pendingExchanges.incrementAndGet();
	}
	
	public void decrementPendingExchange() {
		pendingExchanges.decrementAndGet();
	}

	@ManagedMetric(description = "Count of pending coin exchanges.", displayName = "Count of pending coin exchanges.", metricType = MetricType.GAUGE)
	public long getPendingExchangeCount() {
		return pendingExchanges.longValue();
	}

	
	public void setPendingTransactionTracker(PendingTransactionTracker tracker) {
		pendingTransactions = tracker;
	}
	
	@ManagedMetric(description = "Count of pending coin confirmations.", displayName = "Count of pending coin confirmations.", metricType = MetricType.GAUGE)
	public long getPendingTransactionBacklog() {
		return pendingTransactions.getBacklogSize();
	}

	@ManagedMetric(description = "Duration of longest pending coin confirmation.", displayName = "Duration of longest pending coin confirmation.", metricType = MetricType.GAUGE)
	public long getPendingTransactionMaxWait() {
		return pendingTransactions.getMaxWaitTime();
	}

	@ManagedMetric(description = "Average wait time for pending coin confirmations.", displayName = "Average wait time for pending coin confirmations.", metricType = MetricType.GAUGE)
	public double getPendingTransactionAvgWait() {
		return pendingTransactions.getAverageWaitTime();
	}

	
	/***************************
	 * 
	 * STATISTICAL METRICS
	 * 
	 ***************************/
	

	@Override
	RollingStatistics constructNewStatistic(long currentInterval, IntervalClock intervalClock) {
		return new RollingStatistics(intervalClock.getCurrentInterval(), intervalClock);
	}

	public void addExchangeDuration(long duration) {
		getCurrentRollingStatistic().addExchangeDuration(duration);
	}

	public void failedProcessingExchange() {
		failedExchanges.incrementAndGet();
		getCurrentRollingStatistic().incrementFailedExchange();
	}

	@ManagedMetric(description = "Count of failed coin exchanges.", displayName = "Count of failed coin exchanges.", metricType = MetricType.COUNTER)
	public long getFailedExchangeCount() {
		return failedExchanges.longValue();
	}

	public void successfulProcessingExchange() {
		successfulExchanges.incrementAndGet();
		getCurrentRollingStatistic().incrementSuccessfulExchange();
	}
	
	@ManagedMetric(description = "Count of successful coin exchanges.", displayName = "Count of successful coin exchanges.", metricType = MetricType.COUNTER)
	public long getSuccessfulExchangeCount() {
		return successfulExchanges.longValue();
	}

	public void addEthereumReceipt(Double amount) {
		etherAmount.add(amount);
		getCurrentRollingStatistic().addEthereumReceipt(amount);	
	}

	@ManagedMetric(description = "Amount of total ethereum buys.", displayName = "Amount of total ethereum buys.", metricType = MetricType.COUNTER)
	public double getEthereumReceipts() {
		return etherAmount.doubleValue();
	}

	public void addBrokerFee(long brokerageFee) {
		brokerFeeAmount.add(brokerageFee);
		getCurrentRollingStatistic().addBrokerFee(brokerageFee);
	}

	@ManagedMetric(description = "Amount of collected broker fees.", displayName = "Amount of collected broker fees.", metricType = MetricType.COUNTER)
	public long getBrokerFees() {
		return brokerFeeAmount.longValue();
	}

	public void setWatchedAddresses(Set<String> addresses) {
		this.watchedAddresses= addresses;
	}
	
	public boolean addWatchedAddress(String address) {
		return watchedAddresses.add(address);
	}
	
	public boolean removeWatchedAddress(String address) {
		return watchedAddresses.remove(address);
	}

	@ManagedMetric(description = "Currently watched addresses.", displayName = "Watched Addreses.")
	public Set<String> getWatchedAddresses() {
		return Collections.unmodifiableSet(watchedAddresses);
	}

	public void addPendingTransactionDuration(long duration) {
		getCurrentRollingStatistic().addPendingTransactionDuration(duration);
	}
}
