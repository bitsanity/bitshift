package com.bitsanity.bitchange.server.spring_boot.web;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import com.bitsanity.bitchange.server.spring_boot.web.AbstractWebApplicationConfig;
import com.bitsanity.bitchange.server.spring_boot.web.JMXInterceptor;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages="com.bitsanity.bitchange.server.spring_boot.web")
public class TestWebApplicationConfig extends AbstractWebApplicationConfig
{

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JMXInterceptor(jmxServerStatistics)).addPathPatterns("/**");;
    }
}
