package com.bitsanity.bitchange.server.spring_boot.jmx;

import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.stereotype.Component;

import com.bitsanity.bitchange.server.spring_boot.jmx.AbstractServerStatistics;
import com.bitsanity.bitchange.utils.IntervalClock;

/*
 * Required annotation to access MBeans during JUnit run
 */
@EnableMBeanExport(registration = RegistrationPolicy.REPLACE_EXISTING)

@Component
@Profile("abstractStatistics")
@Primary
@ManagedResource(objectName = "com.bitsanity.bitchange:name=TestServicesServerStatistics", description = "DAT Data Services Statistics.")
public class TestServicesServerStatistics extends AbstractServerStatistics<TestRollingStatistics> {

	/***************************
	 * 
	 * REQUEST METRICS
	 * 
	 ***************************/


	/***************************
	 * 
	 * STATISTICAL METRICS
	 * 
	 ***************************/
	
	@Override
	TestRollingStatistics constructNewStatistic(long currentInterval, IntervalClock intervalClock) {
		return new TestRollingStatistics(intervalClock.getCurrentInterval(), intervalClock);
	}
}
