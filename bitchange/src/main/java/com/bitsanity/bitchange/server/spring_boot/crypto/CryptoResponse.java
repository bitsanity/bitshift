package com.bitsanity.bitchange.server.spring_boot.crypto;

import java.util.HashMap;

public class CryptoResponse {

	private String encryptedPayload;
	private String signature = null;

	// Only one of the following is used
	private HashMap<String, String> recipientKeys;
	private String expiry;

	/* pkg */ CryptoResponse(String payload, String expiry) {
		encryptedPayload = payload;
		this.expiry = expiry;
	}

	/**
	 * Must use recipient keys
	 * 
	 * @param payload
	 */
	/* pkg */ CryptoResponse(String payload) {
		encryptedPayload = payload;
		recipientKeys = new HashMap<String, String>();
	}

	/* pkg */ boolean addRecipientKey(String alias, String key) throws IllegalStateException {
		if (recipientKeys == null) {
			throw new IllegalStateException("Cannot add recipient keys for this type of crypto response.");
		}

		return recipientKeys.put(alias, key) != null;
	}

	public String getSignature() {
		return signature;
	}

	/* pkg */ void setSignature(String signature) {
		this.signature = signature;
	}

	public String getExpiry() throws IllegalStateException {
		if (recipientKeys != null) {
			throw new IllegalStateException(
					"Initialization vector is not defined for this type of crypto response; using an specific recipient keys .");
		}

		return expiry;
	}

	public String getEncryptedPayload() {
		return encryptedPayload;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, String> getRecipientKeys() throws IllegalStateException {
		if (recipientKeys == null) {
			throw new IllegalStateException(
					"Recipient keys are not defined for this type of crypto response; using an initialization vector.");
		}

		return (HashMap<String, String>) recipientKeys.clone();
	}

	public boolean hasExpiry() {
		return recipientKeys == null;
	}

}
