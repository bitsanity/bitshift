
package com.bitsanity.bitchange.canonical.request;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "requestCode",
    "timeStamp"
})
public class Meta {

    @JsonProperty("requestCode")
    private String requestCode;
    @JsonProperty("timeStamp")
    private String timeStamp;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The requestCode
     */
    @JsonProperty("requestCode")
    public String getRequestCode() {
        return requestCode;
    }

    /**
     * 
     * @param requestCode
     *     The requestCode
     */
    @JsonProperty("requestCode")
    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    /**
     * 
     * @return
     *     The timeStamp
     */
    @JsonProperty("timeStamp")
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     * 
     * @param timeStamp
     *     The timeStamp
     */
    @JsonProperty("timeStamp")
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
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
