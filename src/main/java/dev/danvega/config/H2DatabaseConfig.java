package dev.danvega.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for H2 database with a randomly generated password.
 * The password is displayed in the console at startup for H2 console access.
 */
@Configuration
public class H2DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(H2DatabaseConfig.class);
    private static final int PASSWORD_LENGTH = 16;

    private final String generatedPassword;

    public H2DatabaseConfig(ConfigurableEnvironment environment) {
        this.generatedPassword = generateRandomPassword();

        // Set the password as a property so Spring Boot's auto-configuration picks it
        // up
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.password", generatedPassword);
        environment.getPropertySources().addFirst(new MapPropertySource("h2RandomPassword", properties));
    }

    /**
     * Generate a cryptographically secure random password.
     */
    private String generateRandomPassword() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[PASSWORD_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> h2CredentialsLogger() {
        return event -> {
            String url = event.getApplicationContext().getEnvironment()
                    .getProperty("spring.datasource.url", "jdbc:h2:mem:rolesdb");
            String username = event.getApplicationContext().getEnvironment()
                    .getProperty("spring.datasource.username", "sa");

            logDatabaseCredentials(url, username);
        };
    }

    private void logDatabaseCredentials(String url, String username) {
        if (logger.isInfoEnabled()) {
            String urlPadded = padRight(url, 46);
            String usernamePadded = padRight(username, 46);
            String passwordPadded = padRight(generatedPassword, 46);

            logger.info("╔════════════════════════════════════════════════════════════════╗");
            logger.info("║                    H2 DATABASE CREDENTIALS                     ║");
            logger.info("╠════════════════════════════════════════════════════════════════╣");
            logger.info("║  JDBC URL:  {}  ", urlPadded);
            logger.info("║  Username:  {}  ", usernamePadded);
            logger.info("║  Password:  {}  ", passwordPadded);
            logger.info("╠════════════════════════════════════════════════════════════════╣");
            logger.info("║  H2 Console: http://localhost:8080/h2-console                  ║");
            logger.info("║  Note: Password changes on each application restart            ║");
            logger.info("╚════════════════════════════════════════════════════════════════╝");
        }
    }

    private String padRight(String s, int length) {
        if (s == null) {
            s = "null";
        }
        if (s.length() >= length) {
            return s.substring(0, length);
        }
        return s + " ".repeat(length - s.length());
    }

    /**
     * Get the generated password (for testing purposes).
     */
    public String getGeneratedPassword() {
        return generatedPassword;
    }
}
