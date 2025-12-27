package dev.danvega.authz.config;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manual H2 Console configuration for Spring Boot 4.0+
 * 
 * Spring Boot 4.0 removed automatic H2 console servlet registration.
 * This configuration manually registers the H2 console servlet.
 */
@Configuration
@ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true", matchIfMissing = false)
public class H2ConsoleConfig {

    @Bean
    public ServletRegistrationBean<JakartaWebServlet> h2Console() {
        ServletRegistrationBean<JakartaWebServlet> registration = 
            new ServletRegistrationBean<>(new JakartaWebServlet());
        
        registration.addUrlMappings("/h2-console/*");
        registration.setLoadOnStartup(1);
        
        return registration;
    }
}
