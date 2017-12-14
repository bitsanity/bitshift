package com.bitsanity.bitchange.server.spring_boot.web.authentication;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bitsanity.bitchange.server.spring_boot.crypto.KeyManagementService;
import com.bitsanity.bitchange.server.spring_boot.dao.UserAuthorization;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService, InitializingBean {

	@Autowired
	private KeyManagementService keyManagementService;

	private static final Logger LOGGER = CustomLoggerFactory.getLogger(UserDetailsServiceImpl.class);

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public DataServicesUser loadUserByUsername(String username) throws DataAccessException {
		// System.err.println("Looking up user : " + username);
		// username = username.toLowerCase();

		LOGGER.info("Looking up system : " + username);
		UserAuthorization userPOJO = keyManagementService.getUserAuthorization(username);
		if (userPOJO == null) {
			return null;
		}

		List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
		
		//TODO roles not yet implemented
		//for (AuthorizationRoles role : userPOJO.getRoles()) {
			//auths.add(new SimpleGrantedAuthority(role.getRoleName()));
		//}

		return new DataServicesUser(userPOJO, auths);
	}

}
