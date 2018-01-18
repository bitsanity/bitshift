package com.bitsanity.bitchange.server.spring_boot.web;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.hsqldb.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.bitsanity.bitchange.canonical.KeyTransfer;
import com.bitsanity.bitchange.canonical.RestMessage;
import com.bitsanity.bitchange.canonical.response.Respondable;
import com.bitsanity.bitchange.server.spring_boot.BitsanityServer;
import com.bitsanity.bitchange.server.spring_boot.crypto.KeyManagementService;
import com.bitsanity.bitchange.server.spring_boot.crypto.KeyManagementServiceTest;
import com.bitsanity.bitchange.server.spring_boot.dao.UserAuthorization;
import com.bitsanity.bitchange.server.spring_boot.web.BitchangeAdminController;
import com.bitsanity.bitchange.server.spring_boot.web.authentication.TokenAuthenticationService;

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
public class BitchangeAdminControllerTest extends AbstractControllerTest {

	private static final String TEST_CONTROLLER_UPDATE_KEY = "testControllerUpdateKey";
	
	private static Server server;
	
	@Value("${local.server.port}")
	private int port;
	
	@Value("${keystore.alias}")
	private String serverAlias;

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
	public void testUpdateServiceKey() throws Exception {
		//generate base key
		KeyPair baseKeyPair = getKeyPair(TEST_CONTROLLER_UPDATE_KEY);

		//generate new key pair
		KeyPair newKeyPair = KeyManagementServiceTest.generateKeys();

		//create request headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(TokenAuthenticationService.AUTH_HEADER_CLIENT_ID, TEST_CONTROLLER_UPDATE_KEY);
		
		String newPubKey = DatatypeConverter.printBase64Binary(newKeyPair.getPublic().getEncoded());
		KeyTransfer update = new KeyTransfer("RSA", newPubKey );
		
		//generate signature -- NEED PRIVATE KEY to match PUBLIC KEY already in database
		String signature = cryptographyService.sign(update.toString(), baseKeyPair.getPrivate());
		headers.add(TokenAuthenticationService.AUTH_HEADER_SIGNATURE_NAME, signature);
		
		HttpEntity<KeyTransfer> request = new HttpEntity<KeyTransfer>(update, headers);

		//System.err.println(new ObjectMapper().writeValueAsString(update));
		ResponseEntity<Respondable> entity = new TestRestTemplate().postForEntity("http://localhost:" + this.port + BitchangeAdminController.URL_UPDATE_CLIENT_KEY, request, Respondable.class, 
				TEST_CONTROLLER_UPDATE_KEY);

		System.err.println("body: " + entity.getBody().toString());
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		
		validateResponseHeaders(entity, cryptographyService.sign(entity.getBody().toString()));
		
		//verify signature
		//assertTrue("failed signature verification", cryptographyService.verifySignature(new ObjectMapper().writeValueAsString(entity.getBody()), 
				//entity.getHeaders().get(TokenAuthenticationService.AUTH_HEADER_SIGNATURE_NAME).get(0), serverAlias));
		
		//test sending with old signature ==> failure
		entity = new TestRestTemplate().postForEntity("http://localhost:" + this.port + BitchangeAdminController.URL_UPDATE_CLIENT_KEY, request, Respondable.class, 
				TEST_CONTROLLER_UPDATE_KEY);

		System.err.println("body: " + entity.getBody().toString());
		assertEquals(HttpStatus.FORBIDDEN, entity.getStatusCode());
		
		//test sending with new signature ==> success
		baseKeyPair = newKeyPair;
		newKeyPair = KeyManagementServiceTest.generateKeys();
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(TokenAuthenticationService.AUTH_HEADER_CLIENT_ID, TEST_CONTROLLER_UPDATE_KEY);

		newPubKey = DatatypeConverter.printBase64Binary(newKeyPair.getPublic().getEncoded());
		update = new KeyTransfer("RSA", newPubKey);
		signature = cryptographyService.sign(update.toString(), baseKeyPair.getPrivate());
		headers.add(TokenAuthenticationService.AUTH_HEADER_SIGNATURE_NAME, signature);
		
		request = new HttpEntity<KeyTransfer>(update, headers);
		entity = new TestRestTemplate().postForEntity("http://localhost:" + this.port + BitchangeAdminController.URL_UPDATE_CLIENT_KEY, request, Respondable.class, 
				TEST_CONTROLLER_UPDATE_KEY);

		System.err.println("body: " + entity.getBody().toString());
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		
		//sync keys in database
		keyRepository.delete(keyRepository.findBySystemId(TEST_CONTROLLER_UPDATE_KEY));
		refreshKeyCache();
	}

	@Test
	public void testUpdateServiceKey_FailMissingAuthorization() throws Exception {
		//ResponseEntity<String> entity = new TestRestTemplate().getForEntity("http://localhost:" + this.port + DataServicesAdminController.URL_UPDATE_CLIENT_KEY, String.class, KeyManagementServiceTest.TEST_UPDATE_KEY);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		KeyTransfer update = new KeyTransfer();
		HttpEntity<KeyTransfer> request = new HttpEntity<KeyTransfer>(update, headers);

		ResponseEntity<Respondable> entity = new TestRestTemplate().postForEntity("http://localhost:" + this.port + BitchangeAdminController.URL_UPDATE_CLIENT_KEY, request, Respondable.class, 
				KeyManagementServiceTest.TEST_UPDATE_KEY);

		assertEquals(HttpStatus.FORBIDDEN, entity.getStatusCode());
		System.err.println("body: " + entity.getBody().toString());
		assertEquals("{\"@class\":\"com.bitsanity.bitchange.canonical.RestMessage\",\"command\":\"/admin/keys/updateClientKey/testUpdateKey\",\"result\":2000,\"message\":\"Full authentication is required to access this resource\"}", entity.getBody().toString());
	}

	@Test
	public void testGetServerKey() throws Exception {
		ResponseEntity<Respondable> entity = new TestRestTemplate().getForEntity("http://localhost:" + this.port + BitchangeAdminController.URL_GET_SERVER_KEY,
				Respondable.class);

		assertEquals(HttpStatus.OK, entity.getStatusCode());
		
		System.err.println("body: " + entity.getBody().toString());
		assertEquals("{\"@class\":\"com.bitsanity.bitchange.canonical.KeyTransfer\",\"keyType\":\"RSA\",\"key\":\"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAjHwK/CULLQB0fWqt9/ht3DugpjFkDAXY9NqZDdgRhnneCUHoVkJOzdjmh2YRcUZKIkDMDbFZPq2sThi04XtzvUY6jjwGk7lrZ1wqPJeBdMN/y/z6VFFXVQMna0WJks1in3s+SfTuYWkzXe5uh16FDKpAq6JEuu0fvd3Dhh4xmqcNU4pUm4AzQXkhF2LSmum0U8j1Ni9+bUp0GEtRDLjCQ4BXqxZo8Sk7Q9RW10Cp++u5NazAYX8Qv5IyXm6E/+WJgBSl4NJGPkbk7S6L7yN5MoBFI44x5xK/oU0RjHWarM+pY1I3jbl5F8s/pC719IvAJhx3QzZJv6cdLAFSxKiCO0QQw9ajdv25xSOvejYV68tD/MlSlJoSQPgd1xijLjJrdH8m/UUpS8E08JaP0TEx9l6d8xKKMNCfJhW4iY3QXTvz2HWoVYNQbbC0O015/uikjBx5vNsy1s7B7U2Ofz8XZmuOyMygi9QyYTQeP0jpYnvBu5VcRsN7d2Ysqzr8hnvZh6wZx/isb8KxUg0Hqitt+dPpApSX0i1lGZD9HOab+/43WZNcUN+G9SOlOWZS3FLQDSAS32ipCnW3bdr8NZq80ET0mxm+MW7pu4OQ1xNfJAE/8l9WhGDnHtNA3kYFPyw8jolUydJI5NsBwQj4r6OMb/RyB+DptNJmNnPaALxlGnECAwEAAQ==\"}", 
				entity.getBody().toString());
		
		validateResponseHeaders(entity, "gjAbOy87wmDwR5owsAMSdMfroHYlpgxn03sWv1XZFBSaJL6MsfD/aGl+o1rMTAVeG0TnaOsLIqlc/w8Y8z18NUBpOYU8iIe81Ltrzij+C0hVzdG2Svb8DgZ/85WNbwYs5wisqdH30k/Hx8zLuWEQh1548axNgIgCLDMWv3RQGRnrsR7K36gVnM9Ym/ak/3Yq8ngu2a3d5nKtcCinPdkf/sRDti3B49VY2yfHgetuV8G3pCZ/feXo/Gwp+rhuJOvkRIIMKiLc8uC2M3jqKc86PBd4/QrxQWw5qzNZBP9rZCD5WDDEIDaY75JVaelEYBjyqg+JJnli829z/Upc/ksxWf5WbH9iIuzkZZm+ogKNPWLGbsoP/YBf0Hd1UYfEB+21GC/VsUowHOqTKb4qMDVe7A7f7qykqnPadEg26zsS8yGeyNVgJijS9KI5HG6Fm2OtPbYq6lPMO96+70zrPpTjBzL986/4Pdhxt1VEOtMpmj0GOU4gGFkyLfFsKq4Y3PFHRZ22h1lIAReQhxRY704JppCXrcIap5LRTfIve8N9uxvWz7YxIzLRbuvDeJbbagK9zXfXqDmWuIY2oMtYX6HrXAOtkBnvYBikijLhS/yZqZf/mx1biFQw9tJmWZK2nesppSLdOdcS79mZ1rTS1TgvJXEW2cqtKdB6XheQxyOSa8s=");
	}

	@Test
	public void testGetServerKeyClientIdAuth() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		//headers.add(TokenAuthenticationService.AUTH_HEADER_CLIENT_ID, TEST_CONTROLLER_UPDATE_KEY);
		headers.add(TokenAuthenticationService.AUTH_HEADER_CLIENT_ID, "csb.local");
		HttpEntity<String> request = new HttpEntity<String>(null, headers);

		ResponseEntity<Respondable> entity = new TestRestTemplate().exchange("http://localhost:" + this.port + BitchangeAdminController.URL_GET_SERVER_KEY,
				HttpMethod.GET, request, Respondable.class);

		assertEquals(HttpStatus.OK, entity.getStatusCode());
		
		System.err.println("body: " + entity.getBody().toString());
		assertEquals("{\"@class\":\"com.bitsanity.bitchange.canonical.KeyTransfer\",\"keyType\":\"RSA\",\"key\":\"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAjHwK/CULLQB0fWqt9/ht3DugpjFkDAXY9NqZDdgRhnneCUHoVkJOzdjmh2YRcUZKIkDMDbFZPq2sThi04XtzvUY6jjwGk7lrZ1wqPJeBdMN/y/z6VFFXVQMna0WJks1in3s+SfTuYWkzXe5uh16FDKpAq6JEuu0fvd3Dhh4xmqcNU4pUm4AzQXkhF2LSmum0U8j1Ni9+bUp0GEtRDLjCQ4BXqxZo8Sk7Q9RW10Cp++u5NazAYX8Qv5IyXm6E/+WJgBSl4NJGPkbk7S6L7yN5MoBFI44x5xK/oU0RjHWarM+pY1I3jbl5F8s/pC719IvAJhx3QzZJv6cdLAFSxKiCO0QQw9ajdv25xSOvejYV68tD/MlSlJoSQPgd1xijLjJrdH8m/UUpS8E08JaP0TEx9l6d8xKKMNCfJhW4iY3QXTvz2HWoVYNQbbC0O015/uikjBx5vNsy1s7B7U2Ofz8XZmuOyMygi9QyYTQeP0jpYnvBu5VcRsN7d2Ysqzr8hnvZh6wZx/isb8KxUg0Hqitt+dPpApSX0i1lGZD9HOab+/43WZNcUN+G9SOlOWZS3FLQDSAS32ipCnW3bdr8NZq80ET0mxm+MW7pu4OQ1xNfJAE/8l9WhGDnHtNA3kYFPyw8jolUydJI5NsBwQj4r6OMb/RyB+DptNJmNnPaALxlGnECAwEAAQ==\"}", 
				entity.getBody().toString());
		
		validateResponseHeaders(entity, "gjAbOy87wmDwR5owsAMSdMfroHYlpgxn03sWv1XZFBSaJL6MsfD/aGl+o1rMTAVeG0TnaOsLIqlc/w8Y8z18NUBpOYU8iIe81Ltrzij+C0hVzdG2Svb8DgZ/85WNbwYs5wisqdH30k/Hx8zLuWEQh1548axNgIgCLDMWv3RQGRnrsR7K36gVnM9Ym/ak/3Yq8ngu2a3d5nKtcCinPdkf/sRDti3B49VY2yfHgetuV8G3pCZ/feXo/Gwp+rhuJOvkRIIMKiLc8uC2M3jqKc86PBd4/QrxQWw5qzNZBP9rZCD5WDDEIDaY75JVaelEYBjyqg+JJnli829z/Upc/ksxWf5WbH9iIuzkZZm+ogKNPWLGbsoP/YBf0Hd1UYfEB+21GC/VsUowHOqTKb4qMDVe7A7f7qykqnPadEg26zsS8yGeyNVgJijS9KI5HG6Fm2OtPbYq6lPMO96+70zrPpTjBzL986/4Pdhxt1VEOtMpmj0GOU4gGFkyLfFsKq4Y3PFHRZ22h1lIAReQhxRY704JppCXrcIap5LRTfIve8N9uxvWz7YxIzLRbuvDeJbbagK9zXfXqDmWuIY2oMtYX6HrXAOtkBnvYBikijLhS/yZqZf/mx1biFQw9tJmWZK2nesppSLdOdcS79mZ1rTS1TgvJXEW2cqtKdB6XheQxyOSa8s=");
	}

	@Test
	public void testGetServerKeyEC() throws Exception {
		KeyPair keyPair = getKeyECPair(TEST_CONTROLLER_EC_PAIR_KEY);
		//KeyPair keyPair = getKeyPair(TEST_CONTROLLER_UPDATE_KEY);
		String payload = "This is a bogus payload, used only for signature generation.";

		//create request headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.add(TokenAuthenticationService.AUTH_HEADER_CLIENT_ID, 
				TEST_CONTROLLER_EC_PAIR_KEY);
				//TEST_CONTROLLER_UPDATE_KEY);
		
		//generate signature -- NEED PRIVATE KEY to match PUBLIC KEY already in database
		String signature = cryptographyService.sign(payload, keyPair.getPrivate());
		headers.add(TokenAuthenticationService.AUTH_HEADER_SIGNATURE_NAME, signature);
		
		HttpEntity<String> request = new HttpEntity<String>(payload, headers);

		ResponseEntity<String> entity = new TestRestTemplate().exchange("http://localhost:" + this.port + BitchangeAdminController.URL_GET_SERVER_KEY,
				HttpMethod.GET, request, String.class);

		assertEquals(HttpStatus.OK, entity.getStatusCode());
		
		System.err.println("body: " + entity.getBody());
		assertEquals("{\"@class\":\"com.bitsanity.bitchange.canonical.KeyTransfer\",\"keyType\":\"RSA\",\"key\":\"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAjHwK/CULLQB0fWqt9/ht3DugpjFkDAXY9NqZDdgRhnneCUHoVkJOzdjmh2YRcUZKIkDMDbFZPq2sThi04XtzvUY6jjwGk7lrZ1wqPJeBdMN/y/z6VFFXVQMna0WJks1in3s+SfTuYWkzXe5uh16FDKpAq6JEuu0fvd3Dhh4xmqcNU4pUm4AzQXkhF2LSmum0U8j1Ni9+bUp0GEtRDLjCQ4BXqxZo8Sk7Q9RW10Cp++u5NazAYX8Qv5IyXm6E/+WJgBSl4NJGPkbk7S6L7yN5MoBFI44x5xK/oU0RjHWarM+pY1I3jbl5F8s/pC719IvAJhx3QzZJv6cdLAFSxKiCO0QQw9ajdv25xSOvejYV68tD/MlSlJoSQPgd1xijLjJrdH8m/UUpS8E08JaP0TEx9l6d8xKKMNCfJhW4iY3QXTvz2HWoVYNQbbC0O015/uikjBx5vNsy1s7B7U2Ofz8XZmuOyMygi9QyYTQeP0jpYnvBu5VcRsN7d2Ysqzr8hnvZh6wZx/isb8KxUg0Hqitt+dPpApSX0i1lGZD9HOab+/43WZNcUN+G9SOlOWZS3FLQDSAS32ipCnW3bdr8NZq80ET0mxm+MW7pu4OQ1xNfJAE/8l9WhGDnHtNA3kYFPyw8jolUydJI5NsBwQj4r6OMb/RyB+DptNJmNnPaALxlGnECAwEAAQ==\"}", 
				entity.getBody().toString());
		
		validateResponseHeaders(entity, "gjAbOy87wmDwR5owsAMSdMfroHYlpgxn03sWv1XZFBSaJL6MsfD/aGl+o1rMTAVeG0TnaOsLIqlc/w8Y8z18NUBpOYU8iIe81Ltrzij+C0hVzdG2Svb8DgZ/85WNbwYs5wisqdH30k/Hx8zLuWEQh1548axNgIgCLDMWv3RQGRnrsR7K36gVnM9Ym/ak/3Yq8ngu2a3d5nKtcCinPdkf/sRDti3B49VY2yfHgetuV8G3pCZ/feXo/Gwp+rhuJOvkRIIMKiLc8uC2M3jqKc86PBd4/QrxQWw5qzNZBP9rZCD5WDDEIDaY75JVaelEYBjyqg+JJnli829z/Upc/ksxWf5WbH9iIuzkZZm+ogKNPWLGbsoP/YBf0Hd1UYfEB+21GC/VsUowHOqTKb4qMDVe7A7f7qykqnPadEg26zsS8yGeyNVgJijS9KI5HG6Fm2OtPbYq6lPMO96+70zrPpTjBzL986/4Pdhxt1VEOtMpmj0GOU4gGFkyLfFsKq4Y3PFHRZ22h1lIAReQhxRY704JppCXrcIap5LRTfIve8N9uxvWz7YxIzLRbuvDeJbbagK9zXfXqDmWuIY2oMtYX6HrXAOtkBnvYBikijLhS/yZqZf/mx1biFQw9tJmWZK2nesppSLdOdcS79mZ1rTS1TgvJXEW2cqtKdB6XheQxyOSa8s=");
	}

	@Test
	public void testHandleJSONParseError() throws Exception {
		//generate base key
		KeyPair baseKeyPair = getKeyPair(TEST_CONTROLLER_UPDATE_KEY);

		//create request headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(TokenAuthenticationService.AUTH_HEADER_CLIENT_ID, TEST_CONTROLLER_UPDATE_KEY);
		
		String update = "{bad Json, 1 , b c";
		
		//generate signature -- NEED PRIVATE KEY to match PUBLIC KEY already in database
		String signature = cryptographyService.sign(update.toString(), baseKeyPair.getPrivate());
		headers.add(TokenAuthenticationService.AUTH_HEADER_SIGNATURE_NAME, signature);
		
		HttpEntity<String> request = new HttpEntity<String>(update, headers);

		//System.err.println(new ObjectMapper().writeValueAsString(update));
		ResponseEntity<Respondable> entity = new TestRestTemplate().postForEntity("http://localhost:" + this.port + BitchangeAdminController.URL_UPDATE_CLIENT_KEY, request, Respondable.class, 
				TEST_CONTROLLER_UPDATE_KEY);

		System.err.println(entity.getBody().toString());
		assertEquals("invalid status", HttpStatus.BAD_REQUEST, entity.getStatusCode());
		
		assertThat("invalid response class", entity.getBody(), instanceOf(RestMessage.class));
		RestMessage responseWrapper = (RestMessage)entity.getBody(); 
		assertNotNull("invalid response", responseWrapper);
		
		//{"@class":"com.bitsanity.bitchange.canonical.RestMessage","result":4000,"message":"Request Error: Request method 'POST' not supported"}
		assertEquals("invalid result code",  RestMessage.AUTH_RESULT_CODE_JSON_PARSE_ERROR, responseWrapper.getResult());
	}

	@Test
	public void testHandleUnsupportedMediaTypeException() throws Exception {
		//generate base key
		KeyPair baseKeyPair = getKeyPair(TEST_CONTROLLER_UPDATE_KEY);

		//create request headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("bogus", "bogus"));
		headers.add(TokenAuthenticationService.AUTH_HEADER_CLIENT_ID, TEST_CONTROLLER_UPDATE_KEY);
		
		String update = "{bad Json, 1 , b c";
		
		//generate signature -- NEED PRIVATE KEY to match PUBLIC KEY already in database
		String signature = cryptographyService.sign(update.toString(), baseKeyPair.getPrivate());
		headers.add(TokenAuthenticationService.AUTH_HEADER_SIGNATURE_NAME, signature);
		
		HttpEntity<String> request = new HttpEntity<String>(update, headers);

		//System.err.println(new ObjectMapper().writeValueAsString(update));
		ResponseEntity<Respondable> entity = new TestRestTemplate().postForEntity("http://localhost:" + this.port + BitchangeAdminController.URL_UPDATE_CLIENT_KEY, request, Respondable.class, 
				TEST_CONTROLLER_UPDATE_KEY);

		System.err.println(entity.getBody().toString());
		assertEquals("invalid status", HttpStatus.UNSUPPORTED_MEDIA_TYPE, entity.getStatusCode());
		
		assertThat("invalid response class", entity.getBody(), instanceOf(RestMessage.class));
		RestMessage responseWrapper = (RestMessage)entity.getBody(); 
		assertNotNull("invalid response", responseWrapper);
		
		//{"@class":"com.bitsanity.bitchange.canonical.RestMessage","command":"/admin/keys/updateClientKey/testControllerUpdateKey","result":5000,"message":"Unsupported Media Type: Content type 'bogus/bogus;charset=UTF-8' not supported[application/octet-stream, text/plain, application/xml, text/xml, application/x-www-form-urlencoded, application/*+xml, multipart/form-data, application/json, application/*+json, */*]"}
		assertEquals("invalid result code",  RestMessage.AUTH_RESULT_CODE_GENERIC_ERROR3, responseWrapper.getResult());
	}

	private KeyPair getKeyPair(String clientId) throws Exception {
		KeyFactory rsaKeyFac = KeyFactory.getInstance("RSA");

		//retrieve private and public keys from repo
		List<UserAuthorization> pojos = keyRepository.findBySystemId(clientId);
		if (pojos.size() == 2) {
			//build keys
			RSAPublicKey publicKey = null;
			RSAPrivateKey privateKey = null;
			for (UserAuthorization auth : pojos) {
				if (auth.getKeyType().equals(UserAuthorization.KEY_TYPE_PRIVATE)) {
					PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(auth.getKey());
					privateKey = (RSAPrivateKey) rsaKeyFac.generatePrivate(encodedKeySpec);
				} else {
					X509EncodedKeySpec keySpec = new X509EncodedKeySpec(auth.getKey());
					publicKey = (RSAPublicKey )rsaKeyFac .generatePublic(keySpec);
				}
			}
			
			//validate that pairs are mated
			if (publicKey.getModulus().equals(privateKey.getModulus())) {
				//build Key pair
				return new KeyPair(publicKey, privateKey);
			}
		} else if (pojos.size() > 2) {
			throw new Exception("Invalid number of keys found for id: " + clientId + ", count: " + pojos.size());
		}
		
		//if not exists or bad, pair, generate new key pair
		KeyPair keyPair = KeyManagementServiceTest.generateKeys();
		saveUserAuthenticationPair(clientId, keyPair);

		return keyPair;
	}

	private void saveUserAuthenticationPair(String clientId, KeyPair keyPair) throws InterruptedException {
		//insert base key pair
		UserAuthorization pojo;
		for (int counter=0 ; counter < 2 ; counter++) {
			pojo = new UserAuthorization(clientId, (counter == 0 ? UserAuthorization.KEY_TYPE_PRIVATE : UserAuthorization.KEY_TYPE_PUBLIC), clientId + " unit test", 
					new Timestamp(System.currentTimeMillis() + 300000), (counter == 0 ? keyPair.getPrivate().getEncoded() : keyPair.getPublic().getEncoded()) );
			pojo.setLastModified(new Timestamp(new java.util.Date().getTime()));
			try {
				keyRepository.save(pojo);
			} catch (Throwable t) {
				t.printStackTrace();
				throw t;
			}
		}
		
		refreshKeyCache();
	}

	private void refreshKeyCache() throws InterruptedException {
		//refresh cache
		Exception ex = new Exception();
		if (!keyManagementService.refreshKeysPriviledged(ex.getStackTrace())) {
			int refreshVal = keyManagementService.getRefreshInterval();
			keyManagementService.setRefreshIntervalInMinutes(1);
			while (!keyManagementService.refreshKeysPriviledged(ex.getStackTrace())) {
				System.err.println("Sleeping 30s until key refresh success.");
				Thread.sleep(30 * 1000);
			}
			keyManagementService.setRefreshIntervalInMinutes(refreshVal);
		}
	}

}
