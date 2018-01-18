package com.bitsanity.bitchange.server.spring_boot.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.bitsanity.bitchange.server.spring_boot.crypto.CryptographyService;
import com.bitsanity.bitchange.server.spring_boot.dao.KeyRepository;
import com.bitsanity.bitchange.server.spring_boot.dao.UserAuthorization;
import com.bitsanity.bitchange.server.spring_boot.web.authentication.TokenAuthenticationService;

public abstract class AbstractControllerTest {

	protected final static String TEST_CONTROLLER_EC_PAIR_KEY = "Elliptic";
	
	@Autowired
	protected CryptographyService cryptographyService;
	@Autowired
	protected KeyRepository keyRepository;

	protected KeyPair getKeyECPair(String clientId) throws Exception {
		KeyFactory ellipticKeyFac = KeyFactory.getInstance("EC");

		//retrieve private and public keys from repo
		List<UserAuthorization> pojos = keyRepository.findBySystemId(clientId);
		if (pojos.size() == 2) {
			//build keys
			PublicKey publicKey = null;
			PrivateKey privateKey = null;
			for (UserAuthorization auth : pojos) {
				if (auth.getKeyType().equals(UserAuthorization.KEY_TYPE_PRIVATE)) {
					PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(auth.getKey());
					privateKey = ellipticKeyFac.generatePrivate(encodedKeySpec);
					//System.err.println("private EC [" + privateKey.getClass() + "]: " + ((ECPrivateKey)privateKey).getParams());
					//System.err.println("\tCurve: " + ((ECParameterSpec)((ECPrivateKey)privateKey).getParams()).getCurve().toString());
					//System.err.println("\tGenerator: " + ((ECParameterSpec)((ECPrivateKey)privateKey).getParams()).getGenerator().toString());
				} else {
					X509EncodedKeySpec keySpec = new X509EncodedKeySpec(auth.getKey());
					publicKey = ellipticKeyFac.generatePublic(keySpec);
					//System.err.println("public EC [" + publicKey.getClass() + "]: " + ((ECPublicKey)publicKey).getParams());
				}
			}
			
			//System.err.println("\tCurve equality: " + ((ECParameterSpec)((ECPrivateKey)privateKey).getParams()).getCurve().equals(
					//((ECParameterSpec)((ECPublicKey)publicKey).getParams()).getCurve()));
			//System.err.println("\tGenerator equality: " + ((ECParameterSpec)((ECPrivateKey)privateKey).getParams()).getGenerator().equals(
					//((ECParameterSpec)((ECPublicKey)publicKey).getParams()).getGenerator()));
			
			
			//validate that pairs are mated
			if (((ECPrivateKey)privateKey).getParams().getCurve().equals(((ECPublicKey)publicKey).getParams().getCurve())
					&& ((ECPrivateKey)privateKey).getParams().getGenerator().equals(((ECPublicKey)publicKey).getParams().getGenerator())
					) {
				//build Key pair
				return new KeyPair(publicKey, privateKey);
			}
			
			throw new Exception("Unmatched keys.");
		}
		
		throw new Exception("Invalid number of keys found for id: " + clientId + ", count: " + pojos.size());
	}

	protected void validateResponseHeaders(ResponseEntity<? extends Serializable> entity, String expectedAuthHeader) {
		assertTrue("missing signature", entity.getHeaders().containsKey(TokenAuthenticationService.AUTH_HEADER_SIGNATURE_NAME));
		System.err.println("response signature header: " + entity.getHeaders().getFirst(TokenAuthenticationService.AUTH_HEADER_SIGNATURE_NAME));
		assertEquals("invalid auth header", expectedAuthHeader, entity.getHeaders().getFirst(TokenAuthenticationService.AUTH_HEADER_SIGNATURE_NAME));
		
		assertTrue("invalid signature", cryptographyService.verifySignature(entity.getBody().toString(), entity.getHeaders().getFirst(TokenAuthenticationService.AUTH_HEADER_SIGNATURE_NAME), 
				cryptographyService.getPublicKey()));
	}

}