package dev.danvega.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

/**
 * Spring Cloud Gateway configuration to proxy H2 console from authorization
 * service.
 * This allows frontend users with appropriate roles to access the H2 console
 * without exposing the authorization service directly.
 */
@Configuration
public class GatewayConfig {

    private static final Logger logger = LoggerFactory.getLogger(GatewayConfig.class);

    @Value("${authorization.service.url}")
    private String authorizationServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> h2ConsoleProxy() {
        logger.info("Configuring H2 Console proxy to: {}/h2-console", authorizationServiceUrl);

        return route("h2_console_proxy")
                .route(path("/h2-console/**"), http(authorizationServiceUrl))
                .build();
    }
}
