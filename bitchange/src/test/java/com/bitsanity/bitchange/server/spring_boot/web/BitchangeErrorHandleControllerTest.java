package com.bitsanity.bitchange.server.spring_boot.web;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.hsqldb.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class BitchangeErrorHandleControllerTest {

	private static Server server;
	
	@Value("${local.server.port}")
	private int port;
	
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
	public void testHandleUncaughtException() throws Exception {
		String userName = "Joe";
		String userPassword = "password";
		String body = "This is a test, this is only a test";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> httpRequest = new HttpEntity<String>(body, headers);

		String serviceEndpoint = "unknownServiceEndpoint";
		ResponseEntity<Respondable> entity = new TestRestTemplate(userName, userPassword).postForEntity("http://localhost:" + this.port + serviceEndpoint, httpRequest,
				Respondable.class);

		System.err.println(entity.getBody().toString());
		assertEquals("invalid status", HttpStatus.NOT_FOUND, entity.getStatusCode());
		
		assertThat("invalid response class", entity.getBody(), instanceOf(RestMessage.class));
		RestMessage responseWrapper = (RestMessage)entity.getBody(); 
		assertNotNull("invalid response", responseWrapper);
		
		//"{\"command\":\"/KeyExchange [http://localhost:50540/KeyExchange]\",\"result\":404,\"message\":\"No handler found for endpoint: /KeyExchange [http://localhost:50540/KeyExchange], method='POST' from client 127.0.0.1[127.0.0.1]\"}",
		assertEquals("invalid result code",  RestMessage.AUTH_RESULT_CODE_PAGE_NOT_FOUND, responseWrapper.getResult());
		assertTrue("invalid result command",  responseWrapper.getCommand().contains(serviceEndpoint));
		assertTrue("invalid result message",  responseWrapper.getMessage().contains(serviceEndpoint));
	}

	@Test
	public void testHandleRequestException() throws Exception {
		String userName = "Joe";
		String userPassword = "password";
		String body = "{This is a test, this is only a test";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> httpRequest = new HttpEntity<String>(body, headers);

		ResponseEntity<Respondable> entity = new TestRestTemplate(userName, userPassword).postForEntity("http://localhost:" + this.port + BitchangeAdminController.URL_GET_SERVER_KEY, 
				httpRequest, Respondable.class);

		System.err.println(entity.getBody().toString());
		assertEquals("invalid status", HttpStatus.BAD_REQUEST, entity.getStatusCode());
		
		assertThat("invalid response class", entity.getBody(), instanceOf(RestMessage.class));
		RestMessage responseWrapper = (RestMessage)entity.getBody(); 
		assertNotNull("invalid response", responseWrapper);
		
		//{"@class":"com.bitsanity.bitchange.canonical.RestMessage","result":4000,"message":"Request Error: Request method 'POST' not supported"}
		assertEquals("invalid result code",  RestMessage.AUTH_RESULT_CODE_GENERIC_ERROR2, responseWrapper.getResult());
	}
}
