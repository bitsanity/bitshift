package com.bitsanity.bitchange.server.spring_boot.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.bitsanity.bitchange.server.spring_boot.jmx.AbstractServerStatistics;

public class JMXInterceptor implements HandlerInterceptor {

	AbstractServerStatistics<?> jmxServerStatistics;

	/* pkg */ public JMXInterceptor(AbstractServerStatistics<?> jmxComponent) {
		jmxServerStatistics = jmxComponent;
	}

	@Override
	public boolean preHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2) throws Exception {
		// increment total requests
		jmxServerStatistics.incrementRequests();

		// increment currently processing requests
		jmxServerStatistics.incrementCurrentRequests();
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		// decrement currently processing requests
		jmxServerStatistics.decrementCurrentRequests();
	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
			throws Exception {
		// unused
	}
}
