package com.bitsanity.bitchange.server.spring_boot.web;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.hsqldb.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import static org.mockito.Matchers.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.bitsanity.bitchange.canonical.RestMessage;
import com.bitsanity.bitchange.canonical.response.Respondable;
import com.bitsanity.bitchange.server.spring_boot.BitsanityServer;
import com.bitsanity.bitchange.server.spring_boot.web.BitchangeAdminController;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=BitsanityServer.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
		//locations = {"file:config/application.properties","file:config/application.yml"}, 
	    properties = {
	    		"spring.datasource.schema:file:deploy/user_auth_hsqldb.sql"
	    		,"spring.datasource.data:file:deploy/build.server/user_auth_seed_hsqldb.sql"
	    }
)
@DirtiesContext
public class BitchangeErrorHandlerControllerUnhandledTest extends AbstractControllerTest {

	private static Server server;
	
	@Value("${local.server.port}")
	private int port;
	
	@Value("${keystore.alias}")
	private String serverAlias;

	@SpyBean
	private BitchangeAdminController controller;
	
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
	public void testGetServerKey500Failure() throws Exception {
		Mockito.doThrow(Exception.class).when(controller).getServerKey(any(HttpServletRequest.class), any(Principal.class));
		
		ResponseEntity<Respondable> entity = new TestRestTemplate().getForEntity("http://localhost:" + this.port + BitchangeAdminController.URL_GET_SERVER_KEY,
				Respondable.class);

		System.err.println(entity.getBody().toString());
		assertEquals("invalid status", HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
		
		assertThat("invalid response class", entity.getBody(), instanceOf(RestMessage.class));
		RestMessage responseWrapper = (RestMessage)entity.getBody(); 
		assertNotNull("invalid response", responseWrapper);
		
		//{"@class":"com.bitsanity.bitchange.canonical.RestMessage","command":"/admin/keys/ServerKey","result":6000,"message":"Unmanaged Error: null"}
		assertEquals("invalid result code",  RestMessage.AUTH_RESULT_CODE_GENERIC_ERROR4, responseWrapper.getResult());

		Mockito.doThrow(new RuntimeException("testGetServerKey500Failure")).when(controller).getServerKey(any(HttpServletRequest.class), any(Principal.class));
		entity = new TestRestTemplate().getForEntity("http://localhost:" + this.port + BitchangeAdminController.URL_GET_SERVER_KEY, Respondable.class);

		System.err.println(entity.getBody().toString());
		assertEquals("invalid status", HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
		
		assertThat("invalid response class", entity.getBody(), instanceOf(RestMessage.class));
		responseWrapper = (RestMessage)entity.getBody(); 
		assertNotNull("invalid response", responseWrapper);
		
		//{"@class":"com.bitsanity.bitchange.canonical.RestMessage","command":"/admin/keys/ServerKey","result":6000,"message":"Unmanaged Error: testGetServerKey500Failure"}
		assertEquals("invalid result code",  RestMessage.AUTH_RESULT_CODE_GENERIC_ERROR4, responseWrapper.getResult());
		assertTrue("invalid result message",  responseWrapper.getMessage().contains("testGetServerKey500Failure"));
	}


}
