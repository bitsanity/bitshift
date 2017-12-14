package com.bitsanity.bitchange.server.spring_boot;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

@Component
@Profile("dev")
public class MockRestOperationsService implements RestOperationsService {

	private final static String SUCCESS_RESPONSE_TEMPLATE = "{\"fulfilment\" : \"bitcoin_tx_hash\": \"%s\"," + 
			"\"tok_purchase_tx_hash\" : \"%s\",\"tok_transfer_tx_hash\" : \"%s\" }";
	
	@Value("${ethereum.buyer.url}")
	private String ethUrl;
	@Value("${bitcoin.generic.quote.url}")
	private String quoteUrl;

	@Value("${mock.ethErrorResponse:false}")
	private boolean respondInError;
	

	private HashMap<RestOperations, MockRestServiceServer> serverMap = new HashMap<RestOperations, MockRestServiceServer>();

	private static final Logger LOGGER = CustomLoggerFactory.getLogger(MockRestOperationsService.class);

	@PostConstruct
	public void init() {
		LOGGER.debug("Rest template service: " + this.getClass().getName());
	}
	
	@Override
	public RestTemplate getRestOperationsTemplate(String url) {
		if (url.equals(quoteUrl)) {
			//create real rest template for quoting service
			return new RestTemplate();
		}
		
		//TODO test timeout
		//templateBuilder.setReadTimeout(100);
		
		//Add logging of body
		//RestTemplate restTemplate = templateBuilder.build();
		RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
		interceptors.add(new LoggingRequestInterceptor());
		restTemplate.setInterceptors(interceptors);
		
		MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
		serverMap.put(restTemplate, mockServer);

		if (respondInError) {
			mockServer.expect(requestTo(ethUrl)).andExpect(method(HttpMethod.POST))
				//.andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
				.andRespond(new FailureResponseCreator());
		} else {
			mockServer.expect(requestTo(ethUrl)).andExpect(method(HttpMethod.POST)).andExpect(content().string(not(isEmptyOrNullString())))
				//.andRespond(withStatus(HttpStatus.CREATED));
				.andRespond(new SuccessResponseCreator());
		}
		return restTemplate;
		
	}
	
	public void verifyAllServers() throws AssertionError {
		// verify all servers
		try {
			for (MockRestServiceServer mockServer : serverMap.values()) {
				mockServer.verify();
			}
		} finally {
			serverMap.clear();
		}
	}
		
	private class SuccessResponseCreator extends DefaultResponseCreator {
		protected SuccessResponseCreator() {
			super(HttpStatus.CREATED);

			contentType(MediaType.APPLICATION_JSON);
			
			String body = String.format(SUCCESS_RESPONSE_TEMPLATE, "bogus", UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19), 
				UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19));
			body(body);
		}
	}

	private class FailureResponseCreator extends DefaultResponseCreator {
		protected FailureResponseCreator() {
			super(HttpStatus.SERVICE_UNAVAILABLE);

			contentType(MediaType.APPLICATION_JSON);
			
			String body = "{ \"error\" : \"An error was encountered.\" }";
			body(body);
		}
	}
}
