/**
 * 
 */
package com.bitsanity.bitchange.canonical.purchase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * @author lou.paloma
 *
 */
public class PurchaseOrderTest {

	/**
	 * Test method for {@link com.bitsanity.bitchange.canonical.purchase.PurchaseOrder#PurchaseOrder(java.lang.String, java.lang.String, String, java.lang.Double)}.
	 */
	@Test
	public void testPurchaseOrder() throws Exception {
		PurchaseOrder po = new PurchaseOrder("hash", "address", "changeAcct", 29.95);
		assertNotNull("null po", po);
		
		PurchaseOrder_ poDetails = po.getPurchaseOrder();
		assertNotNull("null po details", poDetails);
		
		assertEquals("invalid hash", "hash", poDetails.getBitcoinTxHash());
		assertEquals("invalid address", "address", poDetails.getBitcoinAcct());
		assertEquals("invalid change acct", "changeAcct", poDetails.getBitcoinChangeAcct());
		assertEquals("invalid amount", 29.95d, poDetails.getEthAmount(), 0);
	}


}
