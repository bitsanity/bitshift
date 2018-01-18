package com.bitsanity.bitchange.server.spring_boot.web.authentication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

import com.bitsanity.bitchange.canonical.RestMessage;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ForbiddenEntryPoint extends Http403ForbiddenEntryPoint {

	private static final Logger logger = CustomLoggerFactory.getLogger(ForbiddenEntryPoint.class);

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
			throws IOException, ServletException {
		// super.commence(request, response, ex);
		// org.eclipse.jetty.server.Response.commence(request, response, ex);

		response.setStatus(HttpStatus.FORBIDDEN.value());
		// response.setContentType(MimeTypes.Type.TEXT_HTML_8859_1.toString());
		//response.setContentType("text/html;charset=ISO-8859-1");
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		RestMessage errMessage;

		//this is only used to inject error messages during authentication
		if (response.containsHeader(TokenAuthenticationService.HEADER_AUTH_EXCEPTION_CODE)
				&& response.containsHeader(TokenAuthenticationService.HEADER_AUTH_EXCEPTION_CODE)) {
			errMessage = new RestMessage(response.getHeader(TokenAuthenticationService.HEADER_AUTH_EXCEPTION_MESSAGE));
			try {
				errMessage.setResult(Integer.parseInt(response.getHeader(TokenAuthenticationService.HEADER_AUTH_EXCEPTION_CODE)));
			} catch (NumberFormatException e1) {
				errMessage.setResult(RestMessage.AUTH_RESULT_CODE_INVALID_CODE_HEADER);
				errMessage.setMessage(errMessage.getMessage() + "; code=" + response.getHeader(TokenAuthenticationService.HEADER_AUTH_EXCEPTION_CODE));
			}
		} else {
			errMessage = new RestMessage(ex.getLocalizedMessage());
			errMessage.setResult(RestMessage.AUTH_RESULT_CODE_USER_NOT_AUTHORIZED);
		}

		errMessage.setCommand(request.getServletPath());

		try (ByteArrayOutputStream writer = new ByteArrayOutputStream(2048)) {
			//String body = errMessage.toString();
			ObjectMapper mapper = new ObjectMapper();
			String body;
			try {
				body = mapper.writeValueAsString(errMessage);
			} catch (JsonProcessingException e) {
				body = ex.toString();
			}

			logger.info("JSON body: " + body);

			writer.write(body.getBytes());
			writer.flush();
			response.setContentLength(writer.size());
			try (ServletOutputStream outputStream = response.getOutputStream()) {
				writer.writeTo(outputStream);
				writer.close();
			}
		}

		response.getOutputStream().close();
	}
}
