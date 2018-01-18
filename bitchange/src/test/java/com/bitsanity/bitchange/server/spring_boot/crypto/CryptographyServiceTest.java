package com.bitsanity.bitchange.server.spring_boot.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.ZoneId;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.bitsanity.bitchange.server.spring_boot.BitsanityServer;
import com.bitsanity.bitchange.server.spring_boot.crypto.CryptoResponse;
import com.bitsanity.bitchange.server.spring_boot.crypto.CryptographyService;

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
public class CryptographyServiceTest {
	
	private static Server server;
	
	@Value("${keystore.alias}")
	String keyAlias;
	
	@Autowired
	CryptographyService cryptographyService;
	
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
	public void testEncryptStringStringString() throws Exception {
		String clearTextMessage = "testEncryptStringStringString";
		String expiry = LocalDate.now(ZoneId.of("UTC")).plusDays(10).toString();
		
		//encrypt
		CryptoResponse response = cryptographyService.encrypt(keyAlias, clearTextMessage, expiry);
		assertNotNull("null response", response);
		assertTrue("has expiry", response.hasExpiry());
		assertNotNull("null expiration", response.getExpiry());
		assertEquals("unexpected expiration", expiry, response.getExpiry());
		try {
			response.getRecipientKeys();
			fail("expected exception for recipients");
		} catch (IllegalStateException e) {
			//expected
		}

		//verify signature
		String signature = cryptographyService.sign(response.getEncryptedPayload());
		assertNotNull("null signature", signature);
		assertEquals("unmatched signature", signature, response.getSignature());
		assertTrue("invalid verify", cryptographyService.verifySignature(response.getEncryptedPayload(), signature, cryptographyService.getPublicKey()));

		//FIXME -- private key for decrypting is set FROM keyAlias via KeyManagementService init
		//decrypt
		//String decryptedMessage = cryptographyService.decrypt(response.getEncryptedPayload());
		//assertEquals("nonmatching message", clearTextMessage, decryptedMessage);
		//decryptedMessage = cryptographyService.decrypt(response.getEncryptedPayload(), cryptographyService.getPublicKeyEncoded());
		//assertEquals("nonmatching message", clearTextMessage, decryptedMessage);
	}

	@Test
	public void testEncryptAES() throws Exception {
		String clearTextMessage = "testEncryptStringStringString";
		String expiry = LocalDate.now(ZoneId.of("UTC")).plusDays(10).toString();
		
		//encrypt
		CryptoResponse response = cryptographyService.encryptAES(clearTextMessage, expiry);
		assertNotNull("null response", response);
		assertTrue("has expiry", response.hasExpiry());
		assertNotNull("null expiration", response.getExpiry());
		assertEquals("unexpected expiration", expiry, response.getExpiry());
		try {
			response.getRecipientKeys();
			fail("expected exception for recipients");
		} catch (IllegalStateException e) {
			//expected
		}

		//verify signature
		String signature = cryptographyService.sign(response.getEncryptedPayload());
		assertNotNull("null signature", signature);
		assertEquals("unmatched signature", signature, response.getSignature());
		assertTrue("invalid verify", cryptographyService.verifySignature(response.getEncryptedPayload(), signature, cryptographyService.getPublicKey()));

		//decrypt
		String decryptedMessage = cryptographyService.decryptAES(response.getEncryptedPayload(), expiry);
		assertEquals("nonmatching message", clearTextMessage, decryptedMessage);
	}

	@Test
	public void testSignString() throws Exception {
		CryptoResponse response = cryptographyService.encodeBase64("testSignString");
		assertNotNull("null response", response);
		assertNotNull("null expiration", response.getExpiry());
		try {
			response.getRecipientKeys();
			fail("expected exception for recipients");
		} catch (IllegalStateException e) {
			//expected
		}
		
		String signature = cryptographyService.sign(response.getEncryptedPayload());
		assertNotNull("null signature", signature);
		response.setSignature(signature);
		assertEquals("unmatched signature", signature, response.getSignature());

		assertTrue("invalid verify", cryptographyService.verifySignature(response.getEncryptedPayload(), signature, cryptographyService.getPublicKey()));
	}

	@Test
	public void testPublishCertificate() throws Exception {
		assertNotNull("invalid public cert", cryptographyService.publishCertificate());
	}

	@Test
	public void testGetPublicKeyEncoded() throws Exception {
		assertNotNull("invalid public key", cryptographyService.getPublicKeyEncoded());
	}

	@Test
	public void testGetPublicKey() throws Exception {
		assertNotNull("invalid public key", cryptographyService.getPublicKey());
	}

	@Test
	public void testGetPrivateKey() throws Exception {
		assertNotNull("invalid private key", cryptographyService.getPrivateKey());
	}
}
