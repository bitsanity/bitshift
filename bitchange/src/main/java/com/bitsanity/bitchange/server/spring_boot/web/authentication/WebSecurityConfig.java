package com.bitsanity.bitchange.server.spring_boot.web.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.bitsanity.bitchange.server.spring_boot.crypto.CryptographyService;
import com.bitsanity.bitchange.server.spring_boot.web.BitchangeAdminController;
import com.bitsanity.bitchange.server.spring_boot.web.BitchangeController;

@Configuration
@EnableWebSecurity
@Order(1)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService userDetailsServiceImpl;
	@Autowired
	CryptographyService cryptographyService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
			.authorizeRequests()
				//.antMatchers("/appServices/login").permitAll()
				//.antMatchers("/appServices/logout/**").permitAll()
			
				//require authentication for PR endpoints
				//.antMatchers("/predictive_rates/**").authenticated()
				//FIXME - removed
				//.antMatchers(PredictiveRatesController.URL_PREDICTIVE_RATES_ROOT + "**").authenticated()
				
				//allow anyone to get the Server public key
				.antMatchers(BitchangeAdminController.URL_GET_SERVER_KEY).permitAll()
				
				//require authentication to update a key
				.antMatchers(BitchangeAdminController.URL_UPDATE_CLIENT_KEY).authenticated()
				
				//required SYSADMIN role for any other admin endpoints
				.antMatchers("/admin/**").hasRole("SYSADMIN")
				
				//TODO don't allow anyone to add/remove watched addresses
				.antMatchers(BitchangeController.URL_BITCOIN_PREFIX).permitAll()

				.antMatchers("/**").permitAll().anyRequest().authenticated()
			.and()
				.addFilterBefore(new StatelessAuthenticationFilter(tokenAuthenticationService()), UsernamePasswordAuthenticationFilter.class)
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and().exceptionHandling().authenticationEntryPoint(new ForbiddenEntryPoint())
			// .and().exceptionHandling().accessDeniedPage("/403")
			// .and().exceptionHandling().accessDeniedHandler(new ForbiddenHandler("403"))
		;
	}

	// Expose the UserDetailsService as a Bean
	@Bean
	@Override
	public UserDetailsService userDetailsServiceBean() throws Exception {
		return userDetailsServiceImpl;
	}

	@Bean
	public TokenAuthenticationService tokenAuthenticationService() throws Exception {
		return new TokenAuthenticationService(userDetailsServiceBean(), cryptographyService);
	}
}