/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.bitcoin.quote;

import org.bitcoinj.core.Coin;

/**
 * @author lou.paloma
 *
 */
public interface QuoteEngine {
	
	public BaseQuote getExchangeRate(Coin coin);

}
