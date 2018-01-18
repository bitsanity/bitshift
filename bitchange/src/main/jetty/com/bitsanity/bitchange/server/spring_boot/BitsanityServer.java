package com.bitsanity.bitchange.server.spring_boot;


import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.bitsanity.bitchange.server.spring_boot.AbstractBitsanityServer;
import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

@Configuration
@EnableAutoConfiguration
@ComponentScan
//@EnableHawtio
public class BitsanityServer extends AbstractBitsanityServer {
	private static final Logger logger = CustomLoggerFactory.getLogger(BitsanityServer.class);

	public static void main(String[] args) {
		SpringApplication.run(BitsanityServer.class, args);
		logger.audit("Server started.");
	}

	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
		return new SSLCustomizer();
	}

	private static class SSLCustomizer implements EmbeddedServletContainerCustomizer {
		@Value("${keystore.file.ssl}")
		private String keystoreFile;
		@Value("${keystore.password.ssl}")
		private String keystorePassword;
		@Value("${keystore.type.ssl}")
		private String keystoreType;
		@Value("${keystore.alias.ssl}")
		private String keystoreAlias;
		@Value("${keystore.securePort}")
		private String securePort;

		@Override
		public void customize(ConfigurableEmbeddedServletContainer factory) {
			customizeJetty((JettyEmbeddedServletContainerFactory) factory);
		}

		public void customizeJetty(JettyEmbeddedServletContainerFactory factory) {
			factory.addServerCustomizers(new JettyServerCustomizer() {

				@Override
				public void customize(Server server) {
					if ( (securePort!=null) && (!securePort.isEmpty())) {
						int securePortVal = Integer.parseInt(securePort);
						if (securePortVal != -1) {
							// System.err.println("Customizing JETTY secure port: " + securePort);
							logger.audit("Customizing JETTY secure port: " + securePort);
							
							//logger.error(MarkerFactory.getMarker("AUDIT"), "Customizing JETTY secure port: " + securePort);
							//System.err.println("Logger: " + logger.getName() + ", class: " + logger.getClass());
							
							HttpConfiguration https = new HttpConfiguration();
							https.addCustomizer(new SecureRequestCustomizer());
							SslContextFactory sslContextFactory = new SslContextFactory();
							sslContextFactory.setKeyStorePath(keystoreFile);
							sslContextFactory.setKeyStorePassword(keystorePassword);
							// sslContextFactory.setKeyManagerPassword(keystorePassword);
							sslContextFactory.setCertAlias(keystoreAlias);
							sslContextFactory.setKeyStoreType(keystoreType);
							
							ServerConnector sslConnector = new ServerConnector(server,
									new SslConnectionFactory(sslContextFactory, "http/1.1"),
									new HttpConnectionFactory(https));
							sslConnector.setPort(securePortVal);
							
							server.addConnector(sslConnector);
							return;
						}
					}
					
					logger.audit("Secure JETTY not configured, only supporting HTTP.");
				}

			});
		}
	}
}
