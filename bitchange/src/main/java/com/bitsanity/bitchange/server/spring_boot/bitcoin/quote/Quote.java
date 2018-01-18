package com.bitsanity.bitchange.server.spring_boot.bitcoin.quote;

public interface Quote {

	Double getRate();

	Long getTimestamp();

	String getMarket();
	
	Double getAmount();
	
	String getGeneratedBy();

}