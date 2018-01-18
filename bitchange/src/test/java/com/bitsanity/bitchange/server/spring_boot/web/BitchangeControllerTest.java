/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import com.bitsanity.bitchange.canonical.RestMessage;
import com.bitsanity.bitchange.canonical.response.Respondable;
import com.bitsanity.bitchange.server.spring_boot.BitsanityServer;
import com.bitsanity.bitchange.server.spring_boot.jmx.ExchangerServerStatistics;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

/**
 * @author lou.paloma
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes=BitsanityServer.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
    	"keystore.disableKeyMgmt:true"
    	,"spring.datasource.url:"
    }
)
public class BitchangeControllerTest {

	@Value("${local.server.port}")
	private int port;
	
	@Value("${bitcoin.network}")
	private String network;
	
	@Autowired
	private ExchangerServerStatistics statistics;


	private static final Logger LOGGER = CustomLoggerFactory.getLogger(BitchangeControllerTest.class);
	
	/**
	 * Test method for {@link com.bitsanity.bitchange.server.spring_boot.web.BitchangeController#addAddress(javax.servlet.http.HttpServletRequest, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAddAddress() throws Exception {
		NetworkParameters params;
		if (network.equals("testnet")) {
		    params = TestNet3Params.get();
		} else if (network.equals("regtest")) {
		    params = RegTestParams.get();
		} else {
			LOGGER.error("Invalid Bitcoin network specified: " + network);
			
			//throw FATAL exception
			throw new RuntimeException();
		}

		//generate address from private key
		ECKey key = new ECKey();
		Address addr = key.toAddress(params);
		String address = addr.toBase58();

		//test
		Set<String> watchedAddresses = statistics.getWatchedAddresses();
		assertFalse("invalid watched address list", watchedAddresses.isEmpty());
		assertFalse("address already being watched", watchedAddresses.contains(address));
		
		//add
		ResponseEntity<Respondable> entity = new TestRestTemplate().postForEntity("http://localhost:" + this.port + BitchangeController.URL_WALLET_ADDRESS_MGMT,
			null, Respondable.class, address);

		assertEquals(HttpStatus.CREATED, entity.getStatusCode());
	
		System.err.println("body: " + entity.getBody().toString());
		assertEquals("Invalid response body", 
			"{\"@class\":\"com.bitsanity.bitchange.canonical.RestMessage\",\"command\":\"/bitcoin/address/" + address + "\",\"result\":0,\"message\":\"Ok\"}",
			entity.getBody().toString());
		watchedAddresses = statistics.getWatchedAddresses();
		assertFalse("invalid watched address list", watchedAddresses.isEmpty());
		assertTrue("address not being watched", watchedAddresses.contains(address));

		//add second time = fail
		entity = new TestRestTemplate().postForEntity("http://localhost:" + this.port + BitchangeController.URL_WALLET_ADDRESS_MGMT,
			null, Respondable.class, address);

		assertEquals(HttpStatus.CONFLICT, entity.getStatusCode());
	
		System.err.println("body: " + entity.getBody().toString());
		assertEquals("Invalid response body", 
			"{\"@class\":\"com.bitsanity.bitchange.canonical.RestMessage\",\"command\":\"/bitcoin/address/" + address + "\",\"result\":0,\"message\":\"Ok\"}",
			entity.getBody().toString());
		watchedAddresses = statistics.getWatchedAddresses();
		assertFalse("invalid watched address list", watchedAddresses.isEmpty());
		assertTrue("address not being watched", watchedAddresses.contains(address));
		
		//delete
		entity = new TestRestTemplate().exchange("http://localhost:" + this.port + BitchangeController.URL_WALLET_ADDRESS_MGMT, HttpMethod.DELETE, null, 
			Respondable.class, address);

		assertEquals(HttpStatus.OK, entity.getStatusCode());

		System.err.println("body: " + entity.getBody().toString());
		assertEquals("Invalid response body", 
			"{\"@class\":\"com.bitsanity.bitchange.canonical.RestMessage\",\"command\":\"/bitcoin/address/" + address + "\",\"result\":0,\"message\":\"Ok\"}",
			entity.getBody().toString());
		watchedAddresses = statistics.getWatchedAddresses();
		assertFalse("invalid watched address list", watchedAddresses.isEmpty());
		assertFalse("address still being watched", watchedAddresses.contains(address));
	}
	
	@Test
	public void testAddInvalidAddress() {
		String address = "abc123";
		
		//test
		Set<String> watchedAddresses = statistics.getWatchedAddresses();
		assertFalse("invalid watched address list", watchedAddresses.isEmpty());
		assertFalse("address already being watched", watchedAddresses.contains(address));
		
		ResponseEntity<Respondable> entity = new TestRestTemplate().postForEntity("http://localhost:" + this.port + BitchangeController.URL_WALLET_ADDRESS_MGMT,
			null, Respondable.class, address);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());

		System.err.println("body: " + entity.getBody().toString());
		assertEquals("Invalid response body", "{\"@class\":\"com.bitsanity.bitchange.canonical.RestMessage\",\"command\":\"/bitcoin/address/" + address 
				+ "\",\"result\":" + RestMessage.AUTH_RESULT_CODE_INVALID_KEY_UPDATE + ",\"message\":\"Trying to add invalid address to watch: " + address + "\"}",
			entity.getBody().toString());
		watchedAddresses = statistics.getWatchedAddresses();
		assertFalse("invalid watched address list", watchedAddresses.isEmpty());
		assertFalse("address still being watched", watchedAddresses.contains(address));	
	}

	@Test
	public void testRemoveFailed() {
		String address = "mqVBoqw2j1BAfC9G7HY6NW6VPjfdZvWF7B";
		
		//test
		Set<String> watchedAddresses = statistics.getWatchedAddresses();
		assertFalse("invalid watched address list", watchedAddresses.isEmpty());
		assertFalse("address already being watched", watchedAddresses.contains(address));
		
		ResponseEntity<Respondable> entity = new TestRestTemplate().exchange("http://localhost:" + this.port + BitchangeController.URL_WALLET_ADDRESS_MGMT, 
			HttpMethod.DELETE, null, Respondable.class, address);

		assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());

		System.err.println("body: " + entity.getBody().toString());
		assertEquals("Invalid response body", 
			"{\"@class\":\"com.bitsanity.bitchange.canonical.RestMessage\",\"command\":\"/bitcoin/address/" + address + "\",\"result\":404,\"message\":\"Ok\"}",
			entity.getBody().toString());
		watchedAddresses = statistics.getWatchedAddresses();
		assertFalse("invalid watched address list", watchedAddresses.isEmpty());
		assertFalse("address still being watched", watchedAddresses.contains(address));		
	}

	@Test
	public void testRemoveInvalidAddress() {
		String address = "abc123";
		
		//test
		Set<String> watchedAddresses = statistics.getWatchedAddresses();
		assertFalse("invalid watched address list", watchedAddresses.isEmpty());
		assertFalse("address already being watched", watchedAddresses.contains(address));
		
		ResponseEntity<Respondable> entity = new TestRestTemplate().exchange("http://localhost:" + this.port + BitchangeController.URL_WALLET_ADDRESS_MGMT, 
			HttpMethod.DELETE, null, Respondable.class, address);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());

		System.err.println("body: " + entity.getBody().toString());
		assertEquals("Invalid response body", "{\"@class\":\"com.bitsanity.bitchange.canonical.RestMessage\",\"command\":\"/bitcoin/address/" + address 
				+ "\",\"result\":" + RestMessage.AUTH_RESULT_CODE_INVALID_KEY_UPDATE + ",\"message\":\"Trying to remove invalid watch address: " + address + "\"}",
			entity.getBody().toString());
		watchedAddresses = statistics.getWatchedAddresses();
		assertFalse("invalid watched address list", watchedAddresses.isEmpty());
		assertFalse("address still being watched", watchedAddresses.contains(address));	
	}
}
