package dev.danvega.config;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Base64;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

/**
 * Configuration for H2 database with a randomly generated password.
 *
 * <p>The password is set AFTER Liquibase migrations run by executing ALTER USER SA SET PASSWORD.
 * This ensures:
 *
 * <ul>
 *   <li>Liquibase can connect with no password during startup
 *   <li>After startup, SA requires the random password
 *   <li>H2 Console users must use the displayed password
 * </ul>
 *
 * <p>This configuration is disabled during tests (profile "test").
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(
    name = "spring.h2.console.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class H2DatabaseConfig {

  private static final Logger logger = LoggerFactory.getLogger(H2DatabaseConfig.class);
  private static final int PASSWORD_LENGTH = 12;

  private String generatedPassword;

  /**
   * ApplicationRunner that sets the SA password after Liquibase runs. Order 100 ensures this runs
   * after Liquibase (which typically runs at order 0).
   */
  @Bean
  @Order(100)
  public ApplicationRunner h2PasswordInitializer(DataSource dataSource) {
    return args -> {
      this.generatedPassword = generateRandomPassword();

      try (Connection conn = dataSource.getConnection();
          Statement stmt = conn.createStatement()) {

        // Change SA password
        stmt.execute("ALTER USER SA SET PASSWORD '" + generatedPassword + "'");

        logger.info("✓ H2 SA password has been set");
        logDatabaseCredentials();

      } catch (Exception e) {
        logger.warn(
            "Could not set H2 password: {}. Using default (empty) password.", e.getMessage());
        this.generatedPassword = null;
      }
    };
  }

  /** Generate a cryptographically secure random password. */
  private String generateRandomPassword() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] randomBytes = new byte[PASSWORD_LENGTH];
    secureRandom.nextBytes(randomBytes);
    // Use alphanumeric only to avoid SQL escaping issues
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(randomBytes)
        .replace("-", "X")
        .replace("_", "Y");
  }

  private void logDatabaseCredentials() {
    String passwordPadded = StringUtils.rightPad(generatedPassword, 25);

    logger.info("╔═══════════════════════════════════════════════════════════════╗");
    logger.info("║                    H2 DATABASE CREDENTIALS                    ║");
    logger.info("╠═══════════════════════════════════════════════════════════════╣");
    logger.info("║  Console URL:  http://localhost:8080/h2-console               ║");
    logger.info("║  JDBC URL:     jdbc:h2:mem:rolesdb                            ║");
    logger.info("║  Username:     sa                                             ║");
    logger.info("║  Password:     {}                      ║", passwordPadded);
    logger.info("╠═══════════════════════════════════════════════════════════════╣");
    logger.info("║  Note: Password changes on each application restart           ║");
    logger.info("╚═══════════════════════════════════════════════════════════════╝");
  }

  /** Get the generated password (for testing or programmatic access). */
  public String getGeneratedPassword() {
    return generatedPassword;
  }
}
