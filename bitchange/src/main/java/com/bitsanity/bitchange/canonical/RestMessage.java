package com.bitsanity.bitchange.canonical;

import com.bitsanity.bitchange.canonical.response.Respondable;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "command",
    "result",
    "message",
    "requestId"
})
@JsonIgnoreProperties({ "additionalProperties", 
	//ignore Exception fields in JSON serialization
	"stackTrace", "localizedMessage", "suppressed" 
	})
@JsonRootName(value = "error")
public class RestMessage extends Exception implements Respondable {
    
    //only used in authentication when code in header does not parse to integer
    public static final int AUTH_RESULT_CODE_INVALID_CODE_HEADER = -1;
    
    public static final int AUTH_RESULT_CODE_SUCCESS = 0;
    public static final int AUTH_RESULT_CODE_PAGE_NOT_FOUND = 404;
    public static final int AUTH_RESULT_CODE_JSON_PARSE_ERROR = 700;
    
    public static final int AUTH_RESULT_CODE_INVALID_KEY_UPDATE = 1000;

    public static final int AUTH_RESULT_CODE_USER_NOT_AUTHORIZED = 2000;
    public static final int AUTH_RESULT_CODE_INVALID_SIGNATURE = 2001;
    public static final int AUTH_RESULT_CODE_ACCOUNT_DOES_NOT_EXIST = 2002;

    //Generic error codes
    public static final int AUTH_RESULT_CODE_GENERIC_ERROR2 = 4000;
    public static final int AUTH_RESULT_CODE_GENERIC_ERROR3 = 5000;
    public static final int AUTH_RESULT_CODE_GENERIC_ERROR4 = 6000;


    private static final long serialVersionUID = -1L;
    
    private static final Logger LOGGER = CustomLoggerFactory.getLogger(RestMessage.class);
	
    @JsonProperty("command")
    private String command;
    
    @JsonProperty("result")
    private int result;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("requestId")
    private String requestId;

    public RestMessage(String message) {
    	super(message);
    	this.message = message;
    }   
    
    public void setCommand(String command){
    	this.command = command;
    }
    
    @JsonProperty("command")
    public String getCommand() {
    	return command;
    }
    
    public void setResult(int result) {
    	this.result = result;
    }
    
    @JsonProperty("result")
    public int getResult() {
    	return result;
    }
    
    public void setMessage(String message){
    	this.message = message;
    }
    
    @Override
    @JsonProperty("message")
    public String getMessage() {
    	return message;
    }

    /**
	 * @return the requestId
	 */
    @JsonProperty("requestId")
	public String getRequestId() {
		return requestId;
	}

	/**
	 * @param requestId the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

    @Override
    public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		String body;
		try {
			body = mapper.writeValueAsString(this);
		} catch (JsonProcessingException jpe) {
			LOGGER.error("Unable to convert JSON Request to string.", jpe);
			body = "{\"command\":\"" + command +"\",\"result\":" + result +",\"message\":\"" + message + "\"}";
		}
		
		return body;
    }
}
