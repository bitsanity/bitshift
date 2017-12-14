package com.bitsanity.bitchange.server.spring_boot.jmx;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

import org.hsqldb.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.bitsanity.bitchange.server.spring_boot.BitsanityServer;
import com.bitsanity.bitchange.server.spring_boot.crypto.KeyManagementService;
import com.bitsanity.bitchange.server.spring_boot.web.AbstractControllerTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=BitsanityServer.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
		//locations = {"file:config/application.properties","file:config/application.yml"}, 
	    properties = {
	    		"spring.datasource.schema:file:deploy/user_auth_hsqldb.sql"
	    		,"spring.datasource.data:file:deploy/build.server/user_auth_seed_hsqldb.sql"
	    }
)
@ActiveProfiles({"abstractStatistics", "dev"})
@DirtiesContext
public class AbstractServerStatisticsTest extends AbstractControllerTest {

	private static Server server;
	
	@Value("${local.server.port}")
	private int port;
	
	@Value("${statistics.interval.duration.minutes}")
	private int interval;
	
	@Value("${keystore.soakInMinutes}")
	private long keyRefresh;
	
	@Autowired
	KeyManagementService keyManagementService;
	
	@Autowired
	TestServicesServerStatistics jmxStatistics;
	
	@BeforeClass
	public static void init() {
		if (server == null) {
			server = new Server();
			server.setSilent(true);
			server.setTrace(false);
			server.setDatabaseName(0, "testdb");
			server.setDatabasePath(0, "mem:testdb");
			server.start();
		}
	}
	
	@AfterClass
	public static void stopDb() {
		if (server != null) {
			server.stop();
			server.shutdown();
			server = null;
		}
	}

	@Test
	public void testGetUptime() throws Exception {
		assertNotNull("null uptime", jmxStatistics.getUptime());
	}

	@Test
	public void testGetServerVersion() throws Exception {
		assertNotNull("null server version", jmxStatistics.getServerVersion());
	}

	@Test
	public void testGetIntervalStart() throws Exception {
		assertNotNull("null interval start", jmxStatistics.getIntervalStart());
	}

	@Test
	public void testGetIntervalDuration() throws Exception {
		assertEquals("invalid interval duration", interval, jmxStatistics.getIntervalDuration());
	}

	@Test
	public void testGetLastKeyRefresh() throws Exception {
		assertNotNull("null key refresh", jmxStatistics.getLastKeyRefresh());
	}

	@Test
	public void testGetKeyRefreshInterval() throws Exception {
		assertEquals("invalid refresh duration", keyRefresh, jmxStatistics.getKeyRefreshInterval());
	}

	@Test
	public void testGetKnownClientCount() throws Exception {
		assertEquals("invalid client count", 4, jmxStatistics.getKnownClientCount());
	}

	@Test
	public void testRefreshClientKeys() throws Exception {
		assertFalse("failed to fail on refreshing clients", jmxStatistics.refreshClientKeys());
	}

	@Test
	public void testIncrementRequests() throws Exception {
		assertEquals("invalid request count", 0, jmxStatistics.getRequests());
		jmxStatistics.incrementRequests();
		assertEquals("invalid request count", 1, jmxStatistics.getRequests());
	}

	@Test
	public void testIncrementSuccess() throws Exception {
		assertEquals("invalid success count", 0, jmxStatistics.getSuccess());
		jmxStatistics.incrementSuccess();
		assertEquals("invalid success count", 1, jmxStatistics.getSuccess());
	}

	@Test
	public void testIncrementFailures() throws Exception {
		assertEquals("invalid failure count", 0, jmxStatistics.getFailures());
		jmxStatistics.incrementFailures();
		assertEquals("invalid failure count", 1, jmxStatistics.getFailures());
	}

	@Test
	public void testIncrementRejections() throws Exception {
		assertEquals("invalid rejection count", 0, jmxStatistics.getRejections());
		jmxStatistics.incrementRejections();
		assertEquals("invalid rejection count", 1, jmxStatistics.getRejections());
	}

	@Test
	public void testGetCurrentRequests() throws Exception {
		assertEquals("invalid current count", 0, jmxStatistics.getCurrentRequests());
		jmxStatistics.incrementCurrentRequests();
		assertEquals("invalid current count", 1, jmxStatistics.getCurrentRequests());
		jmxStatistics.decrementCurrentRequests();
		assertEquals("invalid current count", 0, jmxStatistics.getCurrentRequests());
	}

	@Test
	public void testGetRollingStatistics() throws Exception {
		assertNotNull("no stats", jmxStatistics.getRollingStatistics());
	}

	@Test
	public void testLogStatistics() throws Exception {
		//TODO check log
		jmxStatistics.logStatistics(jmxStatistics.getCurrentRollingStatistic());
	}

	@Test
	public void testLogAllStatistics() throws Exception {
		//TODO check log
		jmxStatistics.logStatistics();
	}

	@Test
	public void testGetCurrentRollingStatistic() throws Exception {
		assertNotNull("invalid current statistic", jmxStatistics.getCurrentRollingStatistic());
		assertThat("invalid statistic class", jmxStatistics.getCurrentRollingStatistic(), instanceOf(TestRollingStatistics.class));
	}

}
