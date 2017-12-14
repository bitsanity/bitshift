/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.bitcoin;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.bitcoinj.core.Coin;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bitsanity.bitchange.server.spring_boot.bitcoin.quote.BaseQuote;
import com.bitsanity.bitchange.server.spring_boot.bitcoin.quote.Quote;
import com.bitsanity.bitchange.server.spring_boot.bitcoin.quote.QuoteEngine;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

/**
 * @author lou.paloma
 *
 */
@Service
public class MarketService {

	@Autowired
	private List<QuoteEngine> quoteEngines;
	
	private static final Logger LOGGER = CustomLoggerFactory.getLogger(MarketService.class);
	
	@PostConstruct
	public void init() {
		if (quoteEngines.isEmpty()) {
			LOGGER.error("No quoting engines specified.");
			throw new BeanInitializationException("No quoting engines specified.");
		}
		
		LOGGER.audit("Loaded exchange quote engines: " + quoteEngines);
	}
	
	public Quote getMostRecentQuote(Coin coin) {
		//List<BaseQuote> quotes = quoteEngines.stream().map( engine -> engine.getExchangeRate(coin)).collect( Collectors.toList() );
		Optional<BaseQuote> latest = quoteEngines.stream().map( engine -> engine.getExchangeRate(coin)).max(Comparator.comparing(BaseQuote::getTimestamp));
		if (latest.isPresent()) {
			LOGGER.info("Using latest quote of: " + latest.get());
		} else {
			LOGGER.warn("No exchage rate quote available for coin: " + coin);
		}
		return latest.orElse(null);
	}

}
