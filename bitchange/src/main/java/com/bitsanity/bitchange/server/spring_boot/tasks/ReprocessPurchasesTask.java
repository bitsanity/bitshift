package com.bitsanity.bitchange.server.spring_boot.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bitsanity.bitchange.server.spring_boot.bitcoin.BitcoinExchangeService;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

@EnableScheduling
@Component
@ConditionalOnProperty(name="bitcoin.retryIntervalMillis", matchIfMissing=false)
public class ReprocessPurchasesTask
{
    
    private static final Logger LOGGER = CustomLoggerFactory.getLogger(ReprocessPurchasesTask.class);
    
    @Autowired
    private BitcoinExchangeService exchanger;

    @Scheduled(initialDelayString="${bitcoin.retryIntervalMillis}", fixedRateString="${bitcoin.retryIntervalMillis}")
    public void checkForRetries() {
    	LOGGER.info("Scheduled purchase retry started.");
    	
    	exchanger.retryPurchases();
    }
}
