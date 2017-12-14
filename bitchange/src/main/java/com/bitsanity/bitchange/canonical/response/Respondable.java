package com.bitsanity.bitchange.canonical.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
		use = JsonTypeInfo.Id.CLASS,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "@class",
	    visible = false
	    //, defaultImpl = RestMessage.class
	    )
public interface Respondable extends Serializable {

}
