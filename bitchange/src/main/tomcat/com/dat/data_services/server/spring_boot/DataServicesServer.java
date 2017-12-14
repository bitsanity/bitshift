package com.bitsanity.bitchange.server.spring_boot;

import java.io.FileNotFoundException;

import org.apache.catalina.connector.Connector;
import com.bitsanity.bitchange.server.utils.logging.Logger;
import com.bitsanity.bitchange.server.utils.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import com.bitsanity.bitchange.server.spring_boot.AbstractDataServicesServer;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class DataServicesServer extends AbstractDataServicesServer
{

    private static final Logger logger = LoggerFactory.getLogger(DataServicesServer.class);

    public static void main(String[] args) {
	SpringApplication.run(DataServicesServer.class, args);
	logger.info("Server started.");
    }

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
	return new SSLCustomizer();
    }

    private static class SSLCustomizer implements EmbeddedServletContainerCustomizer
    {
	@Value("${keystore.file.ssl}")
	private String keystoreFile;
	@Value("${keystore.password.ssl}")
	private String keystorePassword;
	@Value("${keystore.type.ssl}")
	private String keystoreType;
	@Value("${keystore.alias.ssl}")
	private String keystoreAlias;
	@Value("${keystore.securePort:-1}")
	private int securePort;

	@Override
	public void customize(ConfigurableEmbeddedServletContainer factory) {
	    //FIXME  SSL not working
	    //customizeTomcat((TomcatEmbeddedServletContainerFactory) factory);
	}

	public void customizeTomcat(TomcatEmbeddedServletContainerFactory factory) {
	    factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
	        @Override
	        public void customize(Connector connector) {
	            connector.setPort(securePort);
	            connector.setSecure(true);
	            connector.setScheme("https");
	            //connector.setAttribute("keyAlias", "tomcat");
	            connector.setAttribute("keyAlias", keystoreAlias);
	            //connector.setAttribute("keystorePass", "password");
	            connector.setAttribute("keystorePass", keystorePassword);
	            connector.setAttribute("keystoreType", keystoreType);
	            try {
	                //connector.setAttribute("keystoreFile", ResourceUtils.getFile("src/ssl/tomcat.keystore").getAbsolutePath());
	                connector.setAttribute("keystoreFile", ResourceUtils.getFile(keystoreFile).getAbsolutePath());
	            } catch (FileNotFoundException e) {
	                throw new IllegalStateException("Cannot load keystore", e);
	            }
	            connector.setAttribute("clientAuth", "false");
	            connector.setAttribute("sslProtocol", "TLS");
	            connector.setAttribute("SSLEnabled", true);
	        }
	    });
	}
    }

}
