package com.bitsanity.bitchange.utils;

public class IntervalClock {
	private int intervalPeriod;

	public IntervalClock(int intervalPeriodSeconds) {
		if (intervalPeriodSeconds < 1) {
			throw new IllegalArgumentException(
					"Interval duration must be greater than zero; value supplied: " + intervalPeriodSeconds);
		}
		intervalPeriod = intervalPeriodSeconds;
	}

	/*
	 * @return The current interval
	 */
	public long getCurrentInterval() {
		long currentTimeSeconds = System.currentTimeMillis() / 1000;
		return currentTimeSeconds / getIntervalPeriod();
	}

	public int getIntervalPeriod() {
		return intervalPeriod;
	}

	public long getIntervalStartMillis(long interval) {
		return interval * intervalPeriod * 1000;
	}

	public long getIntervalEndMillis(long interval) {
		return ((interval + 1) * intervalPeriod * 1000) - 1;
	}
}