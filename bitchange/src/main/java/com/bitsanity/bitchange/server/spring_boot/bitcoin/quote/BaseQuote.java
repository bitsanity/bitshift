/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.bitcoin.quote;

/**
 * @author lou.paloma
 *
 */
public class BaseQuote implements Quote {
	
	private static final double SATOSHI_TO_BTC_DIVISOR = Math.pow(10, 8);
	
	private Double rate;
	private Long timestamp;
	private String market;
	private long satoshi;
	private String generatedBy;

	public BaseQuote(Double rate, Long timestamp, String market, long satoshi, String generatedBy) {
		this.rate = rate;
		this.timestamp = timestamp;
		this.market = market;
		this.satoshi = satoshi;
		this.generatedBy = generatedBy;
	}

	/* (non-Javadoc)
	 * @see com.bitsanity.bitchange.server.spring_boot.bitcoin.quote.Quote#getRate()
	 */
	@Override
	public Double getRate() {
		return rate;
	}

	/* (non-Javadoc)
	 * @see com.bitsanity.bitchange.server.spring_boot.bitcoin.quote.Quote#getTimestamp()
	 */
	@Override
	public Long getTimestamp() {
		return timestamp;
	}

	/* (non-Javadoc)
	 * @see com.bitsanity.bitchange.server.spring_boot.bitcoin.quote.Quote#getMarket()
	 */
	@Override
	public String getMarket() {
		return market;
	}

	/* (non-Javadoc)
	 * @see com.bitsanity.bitchange.server.spring_boot.bitcoin.quote.Quote#getAmount()
	 */
	@Override
	public Double getAmount() {
		return rate * (satoshi / SATOSHI_TO_BTC_DIVISOR);
	}

	/* (non-Javadoc)
	 * @see com.bitsanity.bitchange.server.spring_boot.bitcoin.quote.Quote#getGeneratedBy()
	 */
	@Override
	public String getGeneratedBy() {
		return generatedBy;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BaseQuote [rate=");
		builder.append(rate);
		builder.append(", timestamp=");
		builder.append(timestamp);
		builder.append(", market=");
		builder.append(market);
		builder.append(", satoshi=");
		builder.append(satoshi);
		builder.append(", generatedBy=");
		builder.append(generatedBy);
		builder.append("]");
		return builder.toString();
	}


}
