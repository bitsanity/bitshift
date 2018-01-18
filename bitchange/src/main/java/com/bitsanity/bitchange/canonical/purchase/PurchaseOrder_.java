
package com.bitsanity.bitchange.canonical.purchase;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "bitcoin_tx_hash",
    "bitcoin_acct",
    "bitcoin_change_acct",
    "eth_amount"
})
public class PurchaseOrder_ {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bitcoin_tx_hash")
    private String bitcoinTxHash;
    
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bitcoin_acct")
    private String bitcoinAcct;
    
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bitcoin_change_acct")
    private String bitcoin_change_acct;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("eth_amount")
    private Double ethAmount;
    
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bitcoin_tx_hash")
    public String getBitcoinTxHash() {
        return bitcoinTxHash;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bitcoin_tx_hash")
    public void setBitcoinTxHash(String bitcoinTxHash) {
        this.bitcoinTxHash = bitcoinTxHash;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bitcoin_acct")
    public String getBitcoinAcct() {
        return bitcoinAcct;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bitcoin_acct")
    public void setBitcoinAcct(String bitcoinAcct) {
        this.bitcoinAcct = bitcoinAcct;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bitcoin_change_acct")
    public String getBitcoinChangeAcct() {
        return bitcoin_change_acct;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bitcoin_change_acct")
    public void setBitcoinChangeAcct(String bitcoin_change_acct) {
        this.bitcoin_change_acct = bitcoin_change_acct;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("eth_amount")
    public Double getEthAmount() {
        return ethAmount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("eth_amount")
    public void setEthAmount(Double ethAmount) {
        this.ethAmount = ethAmount;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
