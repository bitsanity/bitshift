
package com.bitsanity.bitchange.canonical.purchase;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "purchase_order"
})
public class PurchaseOrder {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("purchase_order")
    private PurchaseOrder_ purchaseOrder;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public PurchaseOrder(String hashAsString, String bitcoinDestAddress, String bitcoinChangeAcct, Double amount) {
		PurchaseOrder_ order = new PurchaseOrder_();
		this.setPurchaseOrder(order);
		order.setBitcoinTxHash(hashAsString);
		order.setBitcoinAcct(bitcoinDestAddress);
		order.setBitcoinChangeAcct(bitcoinChangeAcct);
		order.setEthAmount(amount);
	}

	/**
     * 
     * (Required)
     * 
     */
    @JsonProperty("purchase_order")
    public PurchaseOrder_ getPurchaseOrder() {
        return purchaseOrder;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("purchase_order")
    public void setPurchaseOrder(PurchaseOrder_ purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
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
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return super.toString();
		}
	}
    
    

}
