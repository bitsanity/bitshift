package com.bitsanity.bitchange.server.spring_boot.web;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

import com.bitsanity.bitchange.canonical.RestMessage;
import com.bitsanity.bitchange.canonical.response.Respondable;
import com.bitsanity.bitchange.server.spring_boot.bitcoin.AddressException;
import com.bitsanity.bitchange.server.spring_boot.bitcoin.BitcoinExchangeService;
import com.bitsanity.bitchange.server.spring_boot.crypto.CryptographyService;
import com.bitsanity.bitchange.server.spring_boot.web.authentication.TokenAuthenticationService;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

public class BitchangeController {

	
	public static final String URL_BITCOIN_PREFIX = "/bitcoin/address/";
	public static final String URL_WALLET_ADDRESS_MGMT = URL_BITCOIN_PREFIX + "{address}";

	@Autowired
    private CryptographyService cryptographyService;
	
	@Autowired
	private BitcoinExchangeService exchanger;

    private static final Logger LOGGER = CustomLoggerFactory.getLogger(BitchangeController.class);
    
	@RequestMapping(value = URL_WALLET_ADDRESS_MGMT, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<? extends Serializable> addAddress(HttpServletRequest request, @RequestBody(required=false) String body, @PathVariable String address) {

		LOGGER.audit((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE) + " called with address " + address + ", body: " + body);

		RestMessage authMsg = new RestMessage("Ok");
		authMsg.setCommand((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
		
		HttpStatus retStatus;
		try {
			//add address to wallet
			if (exchanger.addWatchedAddress(address)) {
				retStatus = HttpStatus.CREATED;
				authMsg.setResult(RestMessage.AUTH_RESULT_CODE_SUCCESS);
			} else {
				//already exists
				retStatus = HttpStatus.CONFLICT;
				authMsg.setResult(RestMessage.AUTH_RESULT_CODE_SUCCESS);
			}
		} catch (AddressException t) {
			//add failed
			authMsg.setResult(RestMessage.AUTH_RESULT_CODE_INVALID_KEY_UPDATE);
			authMsg.setMessage(t.getMessage());
			retStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		//generate headers
		HttpHeaders headers = createDefaultResponseHeaders(authMsg);
		
		return new ResponseEntity<RestMessage>(authMsg, headers, retStatus);
	}

	@RequestMapping(value = URL_WALLET_ADDRESS_MGMT, method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<? extends Serializable> removeAddress(HttpServletRequest request, @RequestBody(required=false) String body, @PathVariable String address) {

		LOGGER.audit((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE) + " called with address " + address + ", body: " + body);

		RestMessage authMsg = new RestMessage("Ok");
		authMsg.setCommand((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
		
		HttpStatus retStatus;
		try {
			//remove address from wallet
			if (exchanger.removeWatchedAddress(address)) {
				retStatus = HttpStatus.OK;
				authMsg.setResult(RestMessage.AUTH_RESULT_CODE_SUCCESS);
			} else {
				//not being watched
				retStatus = HttpStatus.NOT_FOUND;
				authMsg.setResult(RestMessage.AUTH_RESULT_CODE_PAGE_NOT_FOUND);
			}
		} catch (AddressException t) {
			//delete failed
			authMsg.setResult(RestMessage.AUTH_RESULT_CODE_INVALID_KEY_UPDATE);
			authMsg.setMessage(t.getMessage());
			retStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		//generate headers
		HttpHeaders headers = createDefaultResponseHeaders(authMsg);
		
		return new ResponseEntity<RestMessage>(authMsg, headers, retStatus);
	}

	/*pkg*/ HttpHeaders createDefaultResponseHeaders(Respondable payload) {
		//generate/add authorization token
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		try {
			headers.add(TokenAuthenticationService.AUTH_HEADER_SIGNATURE_NAME, cryptographyService.sign(payload.toString()));
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			LOGGER.error("Unable to generate response signature, sent without it.", e);
		}
		
		return headers;
    	
    }

}