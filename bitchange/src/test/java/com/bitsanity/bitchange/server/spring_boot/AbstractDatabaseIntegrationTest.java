package com.bitsanity.bitchange.server.spring_boot;

import org.hsqldb.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
	//locations = {"file:config/application.properties","file:config/application.yml"}, 
    properties = {
    		"spring.datasource.schema:file:deploy/user_auth_hsqldb.sql"
    		,"spring.datasource.data:file:deploy/build.server/user_auth_seed_hsqldb.sql"
    }
)
public abstract class AbstractDatabaseIntegrationTest {

	private static Server server;
	
	@Value("${keystore.alias}")
	String keyAlias;
	
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

}
