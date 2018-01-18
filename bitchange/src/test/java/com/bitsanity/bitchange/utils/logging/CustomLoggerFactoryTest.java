package com.bitsanity.bitchange.utils.logging;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=CustomLoggerApp.class)
//@ActiveProfiles(profiles = "test")
public class CustomLoggerFactoryTest {

	@Test
	public void testLogging() throws Exception {
		String dateFormat = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String logContent = FileUtils.readFileToString(new File("log/Bitchange." + dateFormat + ".0.log"));
		

		//LOGGER.audit("This is an audit message during init.");
		//LOGGER.audit("STATISTICS: This is an audit KAFKA message during init.");
		assertTrue("Missing init audit message", logContent.contains("This is an audit message during init."));
		assertTrue("Missing init audit message", logContent.contains("STATISTICS: This is an audit KAFKA message during init."));
		
		//LOGGER.audit("This is an audit message during run.");
		//LOGGER.audit("STATISTICS: This is an audit KAFKA message during run.");
		assertTrue("Missing run audit message", logContent.contains("This is an audit message during run."));
		assertTrue("Missing run audit message", logContent.contains("STATISTICS: This is an audit KAFKA message during run."));
		
		//LOGGER.audit("This is an audit message during destroy.");
		//LOGGER.audit("STATISTICS: This is an audit KAFKA message during destroy.");
		//assertTrue("Missing destroy audit message", logContent.contains("This is an audit message during destroy."));
		//assertTrue("Missing destroy audit message", logContent.contains("STATISTICS: This is an audit KAFKA message during destroy."));

		logContent = FileUtils.readFileToString(new File("log/Bitchange.Statistics." + dateFormat + ".0.log"));

		assertTrue("Missing init audit message", logContent.contains("This is an audit KAFKA message during init."));
		assertTrue("Missing run audit message", logContent.contains("This is an audit KAFKA message during run."));
	}

}
