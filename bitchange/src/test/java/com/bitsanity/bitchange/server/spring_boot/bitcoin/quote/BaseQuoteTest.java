/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.bitcoin.quote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.bitcoinj.core.Coin;
import org.junit.Test;

/**
 * @author lou.paloma
 *
 */
public class BaseQuoteTest {

	/**
	 * Test method for {@link com.bitsanity.bitchange.server.spring_boot.bitcoin.quote.BaseQuote#BaseQuote(java.lang.Double, java.lang.Long, java.lang.String, long, java.lang.String)}.
	 */
	@Test
	public void testBaseQuote() throws Exception {
		long stamp = System.currentTimeMillis();
		BaseQuote quote = new BaseQuote(29.95, stamp, "market", Coin.COIN.getValue(), "test");
		
		assertNotNull("null quote", quote);
		assertEquals("invalid rate", 29.95, quote.getRate(), 0);
		assertEquals("invalid stamp", stamp, quote.getTimestamp(), 0);
		assertEquals("invalid market", "market", quote.getMarket());
		assertEquals("invalid amount", 29.95, quote.getAmount(), 0);
		assertEquals("invalid generator", "test", quote.getGeneratedBy());
	}

}
