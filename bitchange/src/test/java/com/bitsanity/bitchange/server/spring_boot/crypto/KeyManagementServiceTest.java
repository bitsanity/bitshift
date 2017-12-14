package com.bitsanity.bitchange.server.spring_boot.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.xml.bind.DatatypeConverter;

import org.hsqldb.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.codec.Hex;
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
	    }
)
//@IntegrationTest("server.port:0")
@DirtiesContext
public class KeyManagementServiceTest {
	
	private static Server server = null;
	
	public static final String TEST_UPDATE_KEY = "testUpdateKey";

	@Autowired
	private KeyManagementService keyManagementService;
	
	private static KeyPair keyPair;
	
	@BeforeClass
	public static void init() throws NoSuchAlgorithmException, NoSuchProviderException {
		if (server == null) {
			server = new Server();
			server.setSilent(true);
			server.setTrace(false);
			server.setDatabaseName(0, "testdb");
			server.setDatabasePath(0, "mem:testdb");
			server.start();
		}
		generateKeys();
		generateECKeys();
	}
	
	@AfterClass
	public static void stopDb() {
		if (server != null) {
			server.stop();
			server.shutdown();
			server = null;
		}
	}
	
	public static KeyPair generateKeys() throws NoSuchAlgorithmException, NoSuchProviderException {
		System.err.println("Generating keys...");
		
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        //keyGen.initialize(4096, random);
        keyGen.initialize(2048, random);
        
        keyPair = keyGen.generateKeyPair();
        byte[] publicKeyArr = keyPair.getPublic().getEncoded();
        byte[] privateKeyArr = keyPair.getPrivate().getEncoded();
        
		//pubKey = DatatypeConverter.printBase64Binary(publicKeyArr);
        System.err.println("Public key [" + keyPair.getPublic().getAlgorithm() + "]: " + new String(Hex.encode(publicKeyArr)));
        System.err.println("Private key [" + keyPair.getPrivate().getAlgorithm() + "]: " + new String(Hex.encode(privateKeyArr)));
        
        return keyPair;
	}
	
	public static KeyPair generateECKeys() throws NoSuchAlgorithmException, NoSuchProviderException {
		System.err.println("Generating elliptic curve keys...");
		
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(571, random);
        
        KeyPair keyPairEC = keyGen.generateKeyPair();
        byte[] publicKeyArr = keyPairEC.getPublic().getEncoded();
        byte[] privateKeyArr = keyPairEC.getPrivate().getEncoded();
        
        System.err.println("EC Public key [" + keyPairEC.getPublic().getAlgorithm() + "]: " + new String(Hex.encode(publicKeyArr)));
        System.err.println("EC Private key [" + keyPairEC.getPrivate().getAlgorithm() + "]: " + new String(Hex.encode(privateKeyArr)));

        return keyPair;
	}

	@Test
	public void testRefreshKeys() throws Exception {
		assertFalse("Key refresh rejection expected", keyManagementService.refreshKeys());

		//modify/wait for soak to pass and retest
		keyManagementService.setRefreshIntervalInMinutes(1);
		System.err.println("Sleeping 60s to pass threshold.");
		Thread.sleep(60 * 1000);
		System.err.println("Sleep over, starting refresh...");
		assertTrue("Key refresh expected", keyManagementService.refreshKeys());
		keyManagementService.setRefreshIntervalInMinutes(5);
	}

	@Test
	public void testUpdateKey() throws Exception {
		String encodedPubKey = DatatypeConverter.printBase64Binary(keyPair.getPublic().getEncoded());
		keyManagementService.updateKey(TEST_UPDATE_KEY, encodedPubKey, "RSA");
	}

	@Test
	public void testUpdateKeyFailureMissingClient() throws Exception {
		try {
			keyManagementService.updateKey("ABC", "ABC", "RSA");
			fail("Expected exception, none thrown");
		} catch (KeyManagementException kme) {
			//expected
		}
	}

	@Test
	public void testUpdateKeyFailureInvalidKey() throws Exception {
		try {
			keyManagementService.updateKey("TestA", "ABC", "RSA");
			fail("Expected exception, none thrown");
		} catch (KeyManagementException kme) {
			//expected
		}
	}

	/****************************
	 * MOVED TO CRYPTOGRAPHY SERVICE
	 * 

	@Test
		public void testGetPublicKeyEncoded() throws Exception {
			String pbKey = keyManagementService.getPublicKeyEncoded();
			assertNotNull("invalid public key", pbKey);
			assertEquals("invalid key", "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAjHwK/CULLQB0fWqt9/ht3DugpjFkDAXY9NqZDdgRhnneCUHoVkJOzdjmh2YRcUZKIkDMDbFZPq2sThi04XtzvUY6jjwGk7lrZ1wqPJeBdMN/y/z6VFFXVQMna0WJks1in3s+SfTuYWkzXe5uh16FDKpAq6JEuu0fvd3Dhh4xmqcNU4pUm4AzQXkhF2LSmum0U8j1Ni9+bUp0GEtRDLjCQ4BXqxZo8Sk7Q9RW10Cp++u5NazAYX8Qv5IyXm6E/+WJgBSl4NJGPkbk7S6L7yN5MoBFI44x5xK/oU0RjHWarM+pY1I3jbl5F8s/pC719IvAJhx3QzZJv6cdLAFSxKiCO0QQw9ajdv25xSOvejYV68tD/MlSlJoSQPgd1xijLjJrdH8m/UUpS8E08JaP0TEx9l6d8xKKMNCfJhW4iY3QXTvz2HWoVYNQbbC0O015/uikjBx5vNsy1s7B7U2Ofz8XZmuOyMygi9QyYTQeP0jpYnvBu5VcRsN7d2Ysqzr8hnvZh6wZx/isb8KxUg0Hqitt+dPpApSX0i1lGZD9HOab+/43WZNcUN+G9SOlOWZS3FLQDSAS32ipCnW3bdr8NZq80ET0mxm+MW7pu4OQ1xNfJAE/8l9WhGDnHtNA3kYFPyw8jolUydJI5NsBwQj4r6OMb/RyB+DptNJmNnPaALxlGnECAwEAAQ==", pbKey);
		}

	@Test
	public void testGetPrivateKey() throws Exception {
		PrivateKey pvKey = keyManagementService.getPrivateKey();
		assertNotNull("invalid private key", pvKey);
		assertEquals("invalid key format", "PKCS#8", pvKey.getFormat());
	}
	
	*******************************/

	@Test
	public void testGetLastRefresh() throws Exception {
		assertNotNull("Invalid refresh time", keyManagementService.getLastRefresh());
	}

	@Test
	public void testGetKnownClientCount() throws Exception {
		int count = keyManagementService.getKnownClientCount();
		assertEquals("invalid client count",  4, count);
	}

	@Test
	public void testGetRefreshInterval() throws Exception {
		assertEquals("invalid interval", 5,keyManagementService.getRefreshInterval());
	}

	@Test
	public void testRefreshKeysPriviledged() throws Exception {
		Exception ex = new Exception();
		assertTrue(keyManagementService.refreshKeysPriviledged(ex.getStackTrace()));
	}

}
