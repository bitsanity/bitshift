package com.bitsanity.bitchange.utils.logging;

import static org.junit.Assert.*;
import static net.logstash.logback.marker.Markers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=CustomLoggerApp.class)
public class AuditLoggerTest {

	private static final Logger LOGGER = CustomLoggerFactory.getLogger(AuditLoggerTest.class.getName());
	
	@Test
	public void testTrace() throws Exception {
		assertEquals("Invalid logger name", AuditLoggerTest.class.getName(), LOGGER.getName());
		
		LOGGER.trace("String trace.");
		LOGGER.trace(append("marker", "trace"), "Marker trace.");
		LOGGER.trace("String/Object trace.", new Object());
		LOGGER.trace("String/Object[] trace", new Object(), new Object(), new Object());
		LOGGER.trace("String/Throwable trace.", new Exception("trace"));
		LOGGER.trace(append("marker", "trace"), "Marker/String/Object", new Object());
		LOGGER.trace(append("marker", "trace"), "Marker/String/Object[]", new Object(), new Object(), new Object());
		LOGGER.trace(append("marker", "trace"), "Marker/String/Throwable", new Exception("trace"));
		LOGGER.trace("String/Object/Object", new Object(), new Object());
		LOGGER.trace(append("marker", "trace"), "Marker/String/Object/Object", new Object(), new Object());
		
		assertTrue("Trace not enabled.", LOGGER.isTraceEnabled());
		assertTrue("Trace Marker not enabled.", LOGGER.isTraceEnabled(append("marker", "trace")));
	}


	@Test
	public void testDebug() throws Exception {
		LOGGER.debug("String debug.");
		LOGGER.debug(append("marker", "debug"), "Marker debug.");
		LOGGER.debug("String/Object debug.", new Object());
		LOGGER.debug("String/Object[] debug", new Object(), new Object(), new Object());
		LOGGER.debug("String/Throwable debug.", new Exception("debug"));
		LOGGER.debug(append("marker", "debug"), "Marker/String/Object", new Object());
		LOGGER.debug(append("marker", "debug"), "Marker/String/Object[]", new Object(), new Object(), new Object());
		LOGGER.debug(append("marker", "debug"), "Marker/String/Throwable", new Exception("debug"));
		LOGGER.debug("String/Object/Object", new Object(), new Object());
		LOGGER.debug(append("marker", "debug"), "Marker/String/Object/Object", new Object(), new Object());
		
		assertTrue("debug not enabled.", LOGGER.isDebugEnabled());
		assertTrue("debug Marker not enabled.", LOGGER.isDebugEnabled(append("marker", "debug")));
	}

	@Test
	public void testInfo() throws Exception {
		LOGGER.info("String info.");
		LOGGER.info(append("marker", "info"), "Marker info.");
		LOGGER.info("String/Object info.", new Object());
		LOGGER.info("String/Object[] info", new Object(), new Object(), new Object());
		LOGGER.info("String/Throwable info.", new Exception("info"));
		LOGGER.info(append("marker", "info"), "Marker/String/Object", new Object());
		LOGGER.info(append("marker", "info"), "Marker/String/Object[]", new Object(), new Object(), new Object());
		LOGGER.info(append("marker", "info"), "Marker/String/Throwable", new Exception("info"));
		LOGGER.info("String/Object/Object", new Object(), new Object());
		LOGGER.info(append("marker", "info"), "Marker/String/Object/Object", new Object(), new Object());
		
		assertTrue("Info not enabled.", LOGGER.isInfoEnabled());
		assertTrue("Info Marker not enabled.", LOGGER.isInfoEnabled(append("marker", "info")));
	}

	@Test
	public void testWarn() throws Exception {
		LOGGER.warn("String warn.");
		LOGGER.warn(append("marker", "warn"), "Marker warn.");
		LOGGER.warn("String/Object warn.", new Object());
		LOGGER.warn("String/Object[] warn", new Object(), new Object(), new Object());
		LOGGER.warn("String/Throwable warn.", new Exception("warn"));
		LOGGER.warn(append("marker", "warn"), "Marker/String/Object", new Object());
		LOGGER.warn(append("marker", "warn"), "Marker/String/Object[]", new Object(), new Object(), new Object());
		LOGGER.warn(append("marker", "warn"), "Marker/String/Throwable", new Exception("warn"));
		LOGGER.warn("String/Object/Object", new Object(), new Object());
		LOGGER.warn(append("marker", "warn"), "Marker/String/Object/Object", new Object(), new Object());
		
		assertTrue("warn not enabled.", LOGGER.isWarnEnabled());
		assertTrue("warn Marker not enabled.", LOGGER.isWarnEnabled(append("marker", "warn")));
	}

	@Test
	public void testError() throws Exception {
		LOGGER.error("String error.");
		LOGGER.error(append("marker", "error"), "Marker error.");
		LOGGER.error("String/Object error.", new Object());
		LOGGER.error("String/Object[] error", new Object(), new Object(), new Object());
		LOGGER.error("String/Throwable error.", new Exception("error"));
		LOGGER.error(append("marker", "error"), "Marker/String/Object", new Object());
		LOGGER.error(append("marker", "error"), "Marker/String/Object[]", new Object(), new Object(), new Object());
		LOGGER.error(append("marker", "error"), "Marker/String/Throwable", new Exception("error"));
		LOGGER.error("String/Object/Object", new Object(), new Object());
		LOGGER.error(append("marker", "error"), "Marker/String/Object/Object", new Object(), new Object());
		
		assertTrue("error not enabled.", LOGGER.isErrorEnabled());
		assertTrue("error Marker not enabled.", LOGGER.isErrorEnabled(append("marker", "error")));
	}

	@Test
	public void testAudit() throws Exception {
		LOGGER.audit("String audit.");
		LOGGER.audit("String/Object audit.", new Object());
		LOGGER.audit("String/Object[] audit", new Object(), new Object(), new Object());
		LOGGER.audit("String/Throwable audit.", new Exception("audit"));
		LOGGER.audit("String/Object/Object", new Object(), new Object());
		
		assertTrue("audit not enabled.", LOGGER.isAuditEnabled());
	}

}
