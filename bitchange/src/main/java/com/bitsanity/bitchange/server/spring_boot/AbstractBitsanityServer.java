package com.bitsanity.bitchange.server.spring_boot;

import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.bitsanity.bitchange.server.spring_boot.jmx.AbstractServerStatistics;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractBitsanityServer implements DisposableBean, InitializingBean {

	@Value("${ext.server.serverId:NO_SERVER_ID}")
	public String serverName;

	@Autowired
	private AbstractServerStatistics<?> jmxStatistics;

	private final static Logger LOGGER = CustomLoggerFactory.getLogger(AbstractBitsanityServer.class);
	
	@Override
	public void destroy() throws Exception {
		// dump the rolling metric array to the logs
		//jmxStatistics.logStatistics();
		LOGGER.audit("STATISTICS: " + new ObjectMapper().writeValueAsString(jmxStatistics));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		MDC.put("server_id", serverName);
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();

		// handle empty Strings as null value
		pspc.setNullValue(""); 
		
		return pspc;
	}
}
