package com.bitsanity.bitchange.server.spring_boot.jmx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.support.ConnectorServerFactoryBean;
import org.springframework.remoting.rmi.RmiRegistryFactoryBean;
import org.springframework.util.SocketUtils;

import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;

@Configuration
@Profile("default")
/**
 * RMI configuration class for locking down the JMX RMI ports.  This is needed for direct JMX rather than JSON via Jolokia.  If either no port value 
 * ZERO is specified the system will search for first available port to use.  Host name is determined from the jmx server setting.  
 * 
 * To bypass the configuration, set @ActiveProfile("other_than_default") or specify the spring properties jmx.rmi.port and java.rmi.server.hostname.  
 * Additionally, tests may specify the properties via annotations: 
 * 		@TestPropertySource(properties = {"java.rmi.server.hostname:host_name","jmx.rmi.port:port_value"})
 * 
 * NOTE: test suites may fail if the RMI server does not shut down cleanly.  Use of the spring annotation @DirtiesContext on the test case will force
 * the RMI server to be cleaned up after the test class. 
 * 
 * 
 * @author billsa
 *
 */
public class RMIConfig {
	
	@Value("${java.rmi.server.hostname:localhost}")
	private String rmiHost;

	@Value("${jmx.rmi.port:0}")
	private Integer rmiPort;

	@Bean
	public RmiRegistryFactoryBean rmiRegistry() {
		final RmiRegistryFactoryBean rmiRegistryFactoryBean = new RmiRegistryFactoryBean();
		if (rmiPort == 0) {
			rmiPort = SocketUtils.findAvailableTcpPort();
		}
		
		rmiRegistryFactoryBean.setPort(rmiPort);
		rmiRegistryFactoryBean.setAlwaysCreate(true);
		return rmiRegistryFactoryBean;
	}

	@Bean
	@DependsOn("rmiRegistry")
	public ConnectorServerFactoryBean connectorServerFactoryBean() throws Exception {
		
		final ConnectorServerFactoryBean connectorServerFactoryBean = new ConnectorServerFactoryBean();
		connectorServerFactoryBean.setObjectName("connector:name=rmi");
		String serviceURL = String.format("service:jmx:rmi://%s:%s/jndi/rmi://%s:%s/jmxrmi", rmiHost, rmiPort, rmiHost, rmiPort);
		
		CustomLoggerFactory.getLogger(RMIConfig.class).audit("Setting RMI service URL: " + serviceURL);
		
		connectorServerFactoryBean.setServiceUrl(serviceURL);
		return connectorServerFactoryBean;
	}
}
