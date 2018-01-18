/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.bitsanity.bitchange.server.spring_boot.BitsanityServer;

/**
 * @author lou.paloma
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BitsanityServer.class)
@TestPropertySource(properties = {
	"keystore.disableKeyMgmt:true"
	, "spring.datasource.url:"
	, "bitcoin.retryIntervalMillis:1000"
})
@DirtiesContext
public class ReprocessPurchasesTaskTest {

	/**
	 * Test method for {@link com.bitsanity.bitchange.server.spring_boot.tasks.ReprocessPurchasesTask#checkForRetries()}.
	 */
	@Test
	public void testCheckForRetries() throws Exception {
		//String refresh = keyManagementService.getLastRefresh();
		//System.err.println(refresh);
		//assertNotNull("Invalid refresh time", refresh);

		//TODO add validation 

		Thread.sleep(3000);
	}

}
