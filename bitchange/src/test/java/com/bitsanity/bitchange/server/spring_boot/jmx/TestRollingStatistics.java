package com.bitsanity.bitchange.server.spring_boot.jmx;

import org.springframework.jmx.export.annotation.ManagedResource;

import com.bitsanity.bitchange.server.spring_boot.jmx.AbstractRollingStatistics;
import com.bitsanity.bitchange.utils.IntervalClock;

@ManagedResource(objectName = "com.bitsanity.bitchange.jmx:name=TestRollingStatistics", description = "JMX Rolling Statistics.")
public class TestRollingStatistics extends AbstractRollingStatistics {

	
	/* pkg */ TestRollingStatistics(long interval, IntervalClock clock) {
		super(interval, clock);
	}
}
