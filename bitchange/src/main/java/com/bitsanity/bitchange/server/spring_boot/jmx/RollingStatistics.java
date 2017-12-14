package com.bitsanity.bitchange.server.spring_boot.jmx;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.stat.descriptive.SynchronizedSummaryStatistics;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.support.MetricType;

import com.bitsanity.bitchange.utils.IntervalClock;

@ManagedResource(objectName = "com.bitsanity.bitchange.jmx:name=RollingStatistics", description = "JMX Rolling Statistics.")
public class RollingStatistics extends AbstractRollingStatistics {

	/**
	 * Exchanger specific statistics 
	 */
	SynchronizedSummaryStatistics incomingAmountStatistics = new SynchronizedSummaryStatistics();
	SynchronizedSummaryStatistics exchangeDurationStatistics = new SynchronizedSummaryStatistics();
	SynchronizedSummaryStatistics ethereumReceiptStatistics = new SynchronizedSummaryStatistics();
	SynchronizedSummaryStatistics brokerFeeStatistics = new SynchronizedSummaryStatistics();
	SynchronizedSummaryStatistics pendingTransactionStatistics = new SynchronizedSummaryStatistics();

	AtomicInteger failedExchangeStatistics = new AtomicInteger(0);
	AtomicInteger successfulExchangeStatistics = new AtomicInteger(0);
	
	/* pkg */ RollingStatistics(long interval, IntervalClock clock) {
		super(interval, clock);
	}

	@ManagedMetric(description = "Number of entries for Incoming amount statistics.", displayName = "Number of entries for Incoming amount statistics.", metricType = MetricType.COUNTER)
	public double getIncomingAmountStatisticsCount() {
		return incomingAmountStatistics.getN();
	}

	@ManagedMetric(description = "Total amount for Incoming amount statistics.", displayName = "Total amount for Incoming amount statistics.", metricType = MetricType.COUNTER)
	public double getIncomingAmountStatisticsTotal() {
		return incomingAmountStatistics.getSum();
	}

	@ManagedMetric(description = "Average amount for Incoming amount statistics.", displayName = "Average amount for Incoming amount statistics.", metricType = MetricType.GAUGE)
	public double getIncomingAmountStatisticsAverage() {
		return incomingAmountStatistics.getMean();
	}

	@ManagedMetric(description = "Standard deviation for Incoming amount statistics.", displayName = "Standard deviation for Incoming amount statistics.", metricType = MetricType.GAUGE)
	public double getIncomingAmountStatisticsSTD() {
		return incomingAmountStatistics.getStandardDeviation();
	}

	@ManagedMetric(description = "Number of entries for Exchange duration statistics.", displayName = "Number of entries for Exchange duration statistics.", metricType = MetricType.COUNTER)
	public double getExchangeDurationStatisticsCount() {
		return exchangeDurationStatistics.getN();
	}

	@ManagedMetric(description = "Average amount for Exchange duration statistics.", displayName = "Average amount for Exchange duration statistics.", metricType = MetricType.GAUGE)
	public double getExchangeDurationStatisticsAverage() {
		return exchangeDurationStatistics.getMean();
	}

	@ManagedMetric(description = "Standard deviation for Exchange duration statistics.", displayName = "Standard deviation for Exchange duration statistics.", metricType = MetricType.GAUGE)
	public double getExchangeDurationStatisticsSTD() {
		return exchangeDurationStatistics.getStandardDeviation();
	}

	@ManagedMetric(description = "Number of entries for Ethereum receipt statistics.", displayName = "Number of entries for  Ethereum receipt statistics.", metricType = MetricType.COUNTER)
	public double getEthereumReceiptStatisticsCount() {
		return ethereumReceiptStatistics.getN();
	}

	@ManagedMetric(description = "Total amount for  Ethereum receipt statistics.", displayName = "Total amount for  Ethereum receipt statistics.", metricType = MetricType.COUNTER)
	public double getEthereumReceiptStatisticsTotal() {
		return ethereumReceiptStatistics.getSum();
	}

	@ManagedMetric(description = "Average amount for  Ethereum receipt statistics.", displayName = "Average amount for  Ethereum receipt statistics.", metricType = MetricType.GAUGE)
	public double getEthereumReceiptStatisticsAverage() {
		return ethereumReceiptStatistics.getMean();
	}

	@ManagedMetric(description = "Standard deviation for  Ethereum receipt statistics.", displayName = "Standard deviation for  Ethereum receipt statistics.", metricType = MetricType.GAUGE)
	public double getEthereumReceiptStatisticsSTD() {
		return ethereumReceiptStatistics.getStandardDeviation();
	}

	@ManagedMetric(description = "Number of entries for broker fee statistics.", displayName = "Number of entries for broker fee statistics.", metricType = MetricType.COUNTER)
	public double getBrokerFeeStatisticsCount() {
		return brokerFeeStatistics.getN();
	}

	@ManagedMetric(description = "Total amount for broker fee statistics.", displayName = "Total amount for broker fee statistics.", metricType = MetricType.COUNTER)
	public double getBrokerFeeStatisticsTotal() {
		return brokerFeeStatistics.getSum();
	}

	@ManagedMetric(description = "Average amount for broker fee statistics.", displayName = "Average amount for broker fee statistics.", metricType = MetricType.GAUGE)
	public double getBrokerFeeStatisticsAverage() {
		return brokerFeeStatistics.getMean();
	}

	@ManagedMetric(description = "Standard deviation for broker fee statistics.", displayName = "Standard deviation for broker fee statistics.", metricType = MetricType.GAUGE)
	public double getBrokerFeeStatisticsSTD() {
		return brokerFeeStatistics.getStandardDeviation();
	}

	@ManagedMetric(description = "Number of entries for pending transaction duration.", displayName = "Number of entries for pending transaction duration.", metricType = MetricType.COUNTER)
	public double getPendingTransactionStatisticsCount() {
		return pendingTransactionStatistics.getN();
	}

	@ManagedMetric(description = "Average amount for pending transaction duration.", displayName = "Average amount for pending transaction duration.", metricType = MetricType.GAUGE)
	public double getPendingTransactionStatisticsAverage() {
		return pendingTransactionStatistics.getMean();
	}

	@ManagedMetric(description = "Standard deviation for pending transaction duration.", displayName = "Standard deviation for pending transaction duration.", metricType = MetricType.GAUGE)
	public double getPendingTransactionStatisticsSTD() {
		return pendingTransactionStatistics.getStandardDeviation();
	}

	@Override
	protected String getJsonType() {
		return "Bitsanity_Exchanger";
	}

	/*pkg*/ void addIncomingAmountStatistics(long amount) {
		incomingAmountStatistics.addValue(amount);
	}

	/*pkg*/ void addExchangeDuration(long duration) {
		exchangeDurationStatistics.addValue(duration);
	}
	
	/*pkg*/ void incrementFailedExchange() {
		failedExchangeStatistics.incrementAndGet();
	}

	/*pkg*/ int getFailedExchangeCount() {
		return failedExchangeStatistics.get();
	}
	
	/*pkg*/ void incrementSuccessfulExchange() {
		successfulExchangeStatistics.incrementAndGet();
	}
	
	/*pkg*/ int getSuccessfulExchange() {
		return successfulExchangeStatistics.get();
	}

	/*pkg*/ void addEthereumReceipt(Double amount) {		
		ethereumReceiptStatistics.addValue(amount);
	}
	
	/*pkg*/ void addBrokerFee(long fee) {
		brokerFeeStatistics.addValue(fee);
	}

	/*pkg*/ void addPendingTransactionDuration(long duration) {
		pendingTransactionStatistics.addValue(duration);
	}
}
