package com.bitsanity.bitchange.server.spring_boot.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bitsanity.bitchange.server.spring_boot.crypto.KeyManagementService;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

@EnableScheduling
@Component
//FIXME disable key retrieval if setting set
//@ConditionalOnExpression("'${keystore.disableKeyMgmt:false}'=='false'}")
@ConditionalOnProperty(name="keystore.disableKeyMgmt", havingValue="false", matchIfMissing=true)
public class KeyRefeshTask
{
    
    private static final Logger LOGGER = CustomLoggerFactory.getLogger(KeyRefeshTask.class);
    
    @Autowired
    private KeyManagementService keyManagementService;

    @Scheduled(initialDelayString="${keystore.refreshMillis}", fixedRateString="${keystore.refreshMillis}")
    public void refreshKeys() {
    	LOGGER.info("Scheduled key refresh started.");
    	
    	keyManagementService.refreshKeys();
    }
}
