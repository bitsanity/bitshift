package com.bitsanity.bitchange.utils.logging;

import javax.annotation.PostConstruct;

import org.apache.log4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import com.bitsanity.bitchange.utils.logging.CustomLoggerFactory;
import com.bitsanity.bitchange.utils.logging.Logger;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class CustomLoggerApp implements DisposableBean, CommandLineRunner {

	private static final Logger LOGGER = CustomLoggerFactory.getLogger(CustomLoggerApp.class);
	
	@PostConstruct
	public void init() {
		MDC.put("server_id", "localhost");

		LOGGER.audit("This is an audit message during init.");
		LOGGER.audit("STATISTICS: This is an audit KAFKA message during init.");

		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		SpringApplication.run(CustomLoggerApp.class, args);
		
	}

	@Override
	public void destroy() throws Exception {
		LOGGER.audit("This is an audit message during destroy.");
		LOGGER.audit("STATISTICS: This is an audit KAFKA message during destroy.");
		
	}

	@Override
	public void run(String... arg0) throws Exception {
		LOGGER.audit("This is an audit message during run.");
		LOGGER.audit("STATISTICS: This is an audit KAFKA message during run.");
	}

}
