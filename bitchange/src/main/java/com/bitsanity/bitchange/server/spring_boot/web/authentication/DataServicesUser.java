/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.web.authentication;

import java.security.PublicKey;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.bitsanity.bitchange.server.spring_boot.dao.UserAuthorization;

/**
 * @author billsa
 *
 */
public class DataServicesUser extends User implements Authentication {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean authenticated = false;
	private UserAuthorization authorization;

	public DataServicesUser(UserAuthorization user, Collection<? extends GrantedAuthority> authorities) {
		super(user.getSystemId(), "INVALID", authorities);
		
		this.authorization = user;

		//erase credentials (password)
		eraseCredentials();
	}
	
	public PublicKey getPublicKey() {
		return authorization.getPublicKey();
	}

	@Override
	public String getName() {
		return getUsername();
	}

	@Override
	public Object getCredentials() {
		return getPassword();
	}

	@Override
	public User getDetails() {
		return this;
	}

	@Override
	public Object getPrincipal() {
		return getUsername();
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		authenticated = isAuthenticated;
	}

}
