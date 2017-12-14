/**
 * 
 */
package com.bitsanity.bitchange.server.spring_boot.web.authentication;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

/**
 * @author billsa
 *
 */
public class StatelessAuthenticationFilter extends GenericFilterBean {

	private final TokenAuthenticationService tokenAuthenticationService;
	private final static Logger LOGGER = CustomLoggerFactory.getLogger(StatelessAuthenticationFilter.class);

	public StatelessAuthenticationFilter(TokenAuthenticationService tokenAuthenticationService) {
		if (tokenAuthenticationService == null) {
			throw new NullPointerException("Invalid/null authentication service specified!");
		}

		this.tokenAuthenticationService = tokenAuthenticationService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

/*
		Authentication authentication = tokenAuthenticationService.getAuthentication((HttpServletRequest) request, "");
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
		SecurityContextHolder.getContext().setAuthentication(null);
*/		
		
		ResettableStreamHttpServletRequest wrappedRequest = new ResettableStreamHttpServletRequest((HttpServletRequest) request);
		String body = IOUtils.toString(wrappedRequest.getReader());
		wrappedRequest.resetInputStream();

		Authentication authentication = tokenAuthenticationService.getAuthentication(wrappedRequest, (HttpServletResponse) response, body);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(wrappedRequest, response);
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	// https://gist.github.com/calo81/2071634

	private static class ResettableStreamHttpServletRequest extends HttpServletRequestWrapper {

		private byte[] rawData;
		private HttpServletRequest request;
		private ResettableServletInputStream servletStream;

		public ResettableStreamHttpServletRequest(HttpServletRequest request) {
			super(request);
			this.request = request;
			servletStream = new ResettableServletInputStream();
		}

		public void resetInputStream() {
			servletStream.stream = new ByteArrayInputStream(rawData);
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			if (rawData == null) {
				rawData = IOUtils.toByteArray(request.getReader());
				servletStream.stream = new ByteArrayInputStream(rawData);
			}
			return servletStream;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			if (rawData == null) {
				rawData = IOUtils.toByteArray(request.getReader());
				servletStream.stream = new ByteArrayInputStream(rawData);
			}
			return new BufferedReader(new InputStreamReader(servletStream));
		}

		private class ResettableServletInputStream extends ServletInputStream {

			private InputStream stream;

			@Override
			public int read() throws IOException {
				return stream.read();
			}

			@Override
			public boolean isFinished() {
				try {
					return request.getInputStream().isFinished();
				} catch (IOException e) {
					LOGGER.error("Unable to determine if HTTP stream is finished.", e);
					return true;
				}
			}

			@Override
			public boolean isReady() {
				try {
					return request.getInputStream().isReady();
				} catch (IOException e) {
					LOGGER.error("Unable to determine if HTTP stream is ready.", e);
					return false;
				}
			}

			@Override
			public void setReadListener(ReadListener arg0) {
				//ignore
			}
		}
	}

}
