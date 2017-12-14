/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.web.authentication;

import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author billsa
 *
 */
public class TokenHandler {
    private final UserDetailsService userService;

    public TokenHandler(UserDetailsService userService) {
    	if (userService == null) {
    		throw new NullPointerException("Invalid token handler initialization, invalid User Service supplied");
    	}

    	this.userService = userService;
    }

    public DataServicesUser extractUserFromToken(String token) {
        return (DataServicesUser) userService.loadUserByUsername(token);
    }

}
