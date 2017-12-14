
package com.bitsanity.bitchange.canonical;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

import com.bitsanity.bitchange.canonical.response.Respondable;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ 
	"keyType", 
	"key" })
public class KeyTransfer implements Respondable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("keyType")
	private String keyType;
	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("key")
	private String key;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();
	
	private final static Logger LOGGER = CustomLoggerFactory.getLogger(KeyTransfer.class);

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public KeyTransfer() {
	}

	/**
	 *
	 * @param keyType
	 * @param key
	 */
	public KeyTransfer(String keyType, String key) {
		this.keyType = keyType;
		this.key = key;
	}

	/**
	 *
	 * (Required)
	 *
	 * @return The keyType
	 */
	@JsonProperty("keyType")
	public String getKeyType() {
		return keyType;
	}

	/**
	 *
	 * (Required)
	 *
	 * @param keyType
	 *            The keyType
	 */
	@JsonProperty("keyType")
	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}

	/**
	 *
	 * (Required)
	 *
	 * @return The key
	 */
	@JsonProperty("key")
	public String getKey() {
		return key;
	}

	/**
	 *
	 * (Required)
	 *
	 * @param key
	 *            The key
	 */
	@JsonProperty("key")
	public void setKey(String key) {
		this.key = key;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

    @Override
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		String body;
		try {
			body = mapper.writeValueAsString(this);
		} catch (JsonProcessingException jpe) {
			LOGGER.error("Unable to convert JSON Request to string.", jpe);
			body = super.toString();
		}
		
		return body;
    }
}
