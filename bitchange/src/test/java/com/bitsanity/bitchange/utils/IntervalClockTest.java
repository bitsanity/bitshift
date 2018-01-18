package com.bitsanity.bitchange.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import com.bitsanity.bitchange.utils.IntervalClock;

public class IntervalClockTest {
	
	private static int INTERVAL_IN_SEC = 5;

	@Test
	public void testIntervalClock() throws Exception {
		IntervalClock clock = new IntervalClock(INTERVAL_IN_SEC);
		assertNotNull("invalid current interval", clock.getCurrentInterval());
		
		long current = clock.getCurrentInterval();
		assertTrue("invalid current interval", current > 0);
		
		assertEquals("invalid period", INTERVAL_IN_SEC, clock.getIntervalPeriod());
		
		assertTrue("invalid millis calc", clock.getIntervalStartMillis(1) < clock.getIntervalEndMillis(1));
		
		Thread.sleep(INTERVAL_IN_SEC * 1000);
		
		assertTrue("interval didn't increment", clock.getCurrentInterval() > current);
		
	}
}
