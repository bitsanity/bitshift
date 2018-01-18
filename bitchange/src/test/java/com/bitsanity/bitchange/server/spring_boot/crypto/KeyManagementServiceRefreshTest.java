package com.bitsanity.bitchange.server.spring_boot.crypto;

import static org.junit.Assert.assertNotNull;
import org.hsqldb.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.bitsanity.bitchange.server.spring_boot.BitsanityServer;
import com.bitsanity.bitchange.server.spring_boot.crypto.KeyManagementService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=BitsanityServer.class)
@TestPropertySource(
		//locations = {"file:config/application.properties","file:config/application.yml"}, 
	    properties = {
	    		"spring.datasource.schema:file:deploy/user_auth_hsqldb.sql"
	    		,"spring.datasource.data:file:deploy/build.server/user_auth_seed_hsqldb.sql"
	    		,"keystore.disableKeyMgmt:false"
	    		,"keystore.refreshMillis:1000"
	    }
)
@DirtiesContext
public class KeyManagementServiceRefreshTest {
	
	private static Server server = null;
	
	public static final String TEST_UPDATE_KEY = "testUpdateKey";

	@Autowired
	private KeyManagementService keyManagementService;
	
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
	public void testGetLastRefresh() throws Exception {
		String refresh = keyManagementService.getLastRefresh();
		System.err.println(refresh);
		assertNotNull("Invalid refresh time", refresh);
		
		Thread.sleep(3000);
	}


}
