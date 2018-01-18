package com.bitsanity.bitchange.server.spring_boot.web;

import java.io.Serializable;
import java.security.KeyManagementException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

import com.bitsanity.bitchange.canonical.KeyTransfer;
import com.bitsanity.bitchange.canonical.RestMessage;
import com.bitsanity.bitchange.server.spring_boot.crypto.CryptographyService;
import com.bitsanity.bitchange.server.spring_boot.crypto.KeyManagementService;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

@Controller
public class BitchangeAdminController extends BitchangeController {

	public static final String URL_UPDATE_CLIENT_KEY = "/admin/keys/updateClientKey/{clientId}";
	public static final String URL_GET_SERVER_KEY = "/admin/keys/ServerKey";
	
	@Autowired
	private KeyManagementService keyManager;
	
	@Autowired
	private CryptographyService cryptographyService;

	private static final Logger logger = CustomLoggerFactory.getLogger(BitchangeAdminController.class);

	@RequestMapping(value = URL_UPDATE_CLIENT_KEY, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<? extends Serializable> updateServiceKey(HttpServletRequest request, @RequestBody KeyTransfer keyUpdate, @PathVariable String clientId, 
			Principal principal, Authentication auth) {

		logger.audit((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE) + " called with client " + clientId + ", public key: " + keyUpdate.getKey());
		logger.debug("*** Principal: " + principal);

		//FIXME -- validate the clientId is same as principal
		
		RestMessage authMsg = new RestMessage("Ok");
		authMsg.setCommand((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
		
		try {
			//store client key
			keyManager.updateKey(clientId, keyUpdate.getKey(), keyUpdate.getKeyType());
			authMsg.setResult(RestMessage.AUTH_RESULT_CODE_SUCCESS);
		} catch (KeyManagementException kme) {
			//update failed
			authMsg.setResult(RestMessage.AUTH_RESULT_CODE_INVALID_KEY_UPDATE);
			authMsg.setMessage(kme.getMessage());
		}
		
		//generate headers
		HttpHeaders headers = createDefaultResponseHeaders(authMsg);
		
		return new ResponseEntity<RestMessage>(authMsg, headers, HttpStatus.OK);
	}

	@RequestMapping(value = URL_GET_SERVER_KEY, method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<? extends Serializable> getServerKey(HttpServletRequest request, Principal principal) {

		logger.audit((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE) + " called by user: " + principal);

		//retrieve public key
		KeyTransfer key = new KeyTransfer(cryptographyService.getPublicKey().getAlgorithm(), cryptographyService.getPublicKeyEncoded());
		
		//generate headers
		HttpHeaders headers = createDefaultResponseHeaders(key);
		
		return new ResponseEntity<KeyTransfer>(key, headers, HttpStatus.OK);
	}

}