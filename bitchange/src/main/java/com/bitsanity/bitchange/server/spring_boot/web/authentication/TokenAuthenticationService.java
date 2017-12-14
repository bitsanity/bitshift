package com.bitsanity.bitchange.server.spring_boot.web.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.bitsanity.bitchange.canonical.RestMessage;
import com.bitsanity.bitchange.server.spring_boot.crypto.CryptographyService;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

public class TokenAuthenticationService {

    public static final String AUTH_HEADER_SIGNATURE_NAME = "X-AUTH-TOKEN";
    public static final String AUTH_HEADER_CLIENT_ID = "X-AUTH-CLIENT-ID";

    public static final String HEADER_AUTH_EXCEPTION_MESSAGE = "AuthenticationExceptionMessage";
    public static final String HEADER_AUTH_EXCEPTION_CODE = "AuthenticationExceptionCode";

    private TokenHandler tokenHandler;
    private CryptographyService cryptographyService;
    
    private static final Logger LOGGER = CustomLoggerFactory.getLogger(TokenAuthenticationService.class);

    public TokenAuthenticationService(UserDetailsService userService, CryptographyService cryptographyService) {
        tokenHandler = new TokenHandler(userService);
        this.cryptographyService = cryptographyService;
    }

    public Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response, String body) {
        String userToken = request.getHeader(AUTH_HEADER_CLIENT_ID);
        String signature = request.getHeader(AUTH_HEADER_SIGNATURE_NAME);
        		
        LOGGER.trace("Authenticating request for client id: " + userToken);
        if (userToken != null) {
        	//get user
            final DataServicesUser user = tokenHandler.extractUserFromToken(userToken);
            if (user != null) {
                LOGGER.debug("Verifying message signature for client id: " + userToken + ", signature: " + signature);

                //validate signature
				try {

					//input stream can only be read once, reset won't fix
					//String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
					//request.getReader().reset();
					//http://stackoverflow.com/questions/1046721/accessing-the-raw-body-of-a-put-or-post-request
					
					user.setAuthenticated(cryptographyService.verifySignature(body, signature, user.getPublicKey()));
					if (!user.isAuthenticated()) {
						return null;
					}
				} catch (IllegalArgumentException e) {
					String msg = "Unable to verify signature for request, reason: " + e.getMessage();
					LOGGER.error(msg, e);
					
					//inject exception information
					response.addHeader(HEADER_AUTH_EXCEPTION_MESSAGE, msg);
					response.addIntHeader(HEADER_AUTH_EXCEPTION_CODE, RestMessage.AUTH_RESULT_CODE_INVALID_SIGNATURE);
				}
            	
                return user;
            }
            
			String msg = "Unable to find user for that specified in the " + AUTH_HEADER_CLIENT_ID + " header: " + userToken;
			LOGGER.debug(msg);

			//inject exception information
			response.addHeader(HEADER_AUTH_EXCEPTION_MESSAGE, msg);
			response.addIntHeader(HEADER_AUTH_EXCEPTION_CODE, RestMessage.AUTH_RESULT_CODE_ACCOUNT_DOES_NOT_EXIST);
        }
        
        return null;
    }
}