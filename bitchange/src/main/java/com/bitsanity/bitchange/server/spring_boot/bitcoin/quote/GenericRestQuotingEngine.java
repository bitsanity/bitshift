/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.bitcoin.quote;

import org.bitcoinj.core.Coin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.bitsanity.bitchange.server.spring_boot.RestOperationsService;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

/**
 * @author lou.paloma
 *
 */
@Component()
@ConditionalOnProperty(name = "bitcoin.generic.quote.url")
//@ConditionalOnProperty(name = "bitcoin.generic.quote.useCaching", havingValue = "false", matchIfMissing = true)
//@ConditionalOnExpression("${bitcoin.generic.quote.url} and ${bitcoin.generic.quote.useCaching} = 'false'")
public class GenericRestQuotingEngine implements QuoteEngine {

	@Value("${bitcoin.generic.quote.url}")
	private String url;
	@Value("${bitcoin.generic.quote.json.path.price}")
	private String jsonPricePath;
	@Value("${bitcoin.generic.quote.json.path.timestamp}")
	private String jsonTimestampPath;
	@Value("${bitcoin.generic.quote.json.path.market:#{null}}")
	private String jsonMarketPath;


	@Autowired
	private RestOperationsService restService;

	private static final Logger LOGGER = CustomLoggerFactory.getLogger(GenericRestQuotingEngine.class);

	/* (non-Javadoc)
	 * @see com.bitsanity.bitchange.server.spring_boot.bitcoin.quote.QuoteEngine#getExchangeRate(org.bitcoinj.core.Coin)
	 */
	@Override
	public BaseQuote getExchangeRate(Coin coin) {
		//generate REST call: https://min-api.cryptocompare.com/data/pricemultifull?fsyms=BTC&tsyms=ETH
		RestTemplate template = restService.getRestOperationsTemplate(url);
		long duration = System.currentTimeMillis();
		ResponseEntity<String> response = template.getForEntity(url, String.class);
		duration = System.currentTimeMillis() - duration;
		if (!response.getStatusCode().equals(HttpStatus.OK) ) {
			//bad response
			LOGGER.error("Error response from quoting source, url: " + url +", returned code: " + response.getStatusCode() + ", content: " + response);
			
			//TODO update stats
			
			return null;
		} else if (!response.hasBody()) {
			LOGGER.error("Successful response from quoting source, url: " + url +", but no body returned, content: " + response);

			//TODO update stats
			return null;
		}
		
		//TODO update stats
		
		//parse ETH into BaseQuote object -- see http://jsonpath.com/
		Configuration conf = Configuration.defaultConfiguration();
		
		Double price;
		Long timestamp;
		try {
			price = JsonPath.using(conf).parse(response.getBody()).read(jsonPricePath, Double.class);
			timestamp = JsonPath.using(conf).parse(response.getBody()).read(jsonTimestampPath, Long.class);
		} catch (Throwable e) {
			//handle missing fields
			LOGGER.error("Error while processing response.", e);
			return null;
		}
		String market = null;
		if (!StringUtils.isEmpty(jsonMarketPath)) {
			market = JsonPath.using(conf).parse(response.getBody()).read(jsonMarketPath, String.class);
		}
		
		return new BaseQuote(price, timestamp, market, coin.getValue(), this.getClass().getSimpleName());
	}

}
