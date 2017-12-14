package com.bitsanity.bitchange.server.spring_boot.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.bitsanity.bitchange.server.spring_boot.jmx.AbstractServerStatistics;

//Enable following lines in concrete classes
//@Configuration
//@EnableWebMvc
//@ComponentScan(basePackages="com.bitsanity.bitchange.server.spring_boot.web")
public abstract class AbstractWebApplicationConfig extends WebMvcConfigurerAdapter
{

    @Autowired
    protected AbstractServerStatistics<?> jmxServerStatistics;
    
    @Bean 
    public DispatcherServlet dispatcherServlet() {
        DispatcherServlet ds = new DispatcherServlet();
        
        //allow for handling of 404
        ds.setThrowExceptionIfNoHandlerFound(true);
        return ds;
    }
}
