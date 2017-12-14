package com.bitsanity.bitchange.server.spring_boot;

import org.springframework.web.client.RestTemplate;

/**
 * General interface for Translation service, providing a framework for an injectable mock RestTemplate.
 *
 */public interface RestOperationsService {

    public RestTemplate getRestOperationsTemplate(String url) ;
        
}
