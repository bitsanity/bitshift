package com.bitsanity.bitchange.server.spring_boot;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

@Component
@Profile("default")
public class DefaultRestOperationsService implements RestOperationsService {

	@Autowired
	private RestTemplateBuilder templateBuilder;
	
	private static final Logger LOGGER = CustomLoggerFactory.getLogger(DefaultRestOperationsService.class);

	@PostConstruct
	public void init() {
		LOGGER.debug("Rest template service: " + this.getClass().getName());
	}

	@Override
	public RestTemplate getRestOperationsTemplate(String url) {
		LOGGER.debug("Rest template service: " + this.getClass().getName());

		// ignore arguments
		RestTemplate template = templateBuilder.build();
		template.setErrorHandler(new NullResponseErrorHandler());
		return template;
	}

	private class NullResponseErrorHandler implements ResponseErrorHandler {
		@Override
		public boolean hasError(ClientHttpResponse clienthttpresponse) throws IOException {
			return (new DefaultResponseErrorHandler()).hasError(clienthttpresponse);
		}

		@Override
		public void handleError(ClientHttpResponse clienthttpresponse) throws IOException {
			// no processing
		}
	}

}
