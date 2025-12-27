package dev.danvega.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for H2DatabaseConfig.
 */
class H2DatabaseConfigTest {

    @Test
    void constructor_generatesRandomPassword() {
        MockEnvironment environment = new MockEnvironment();
        H2DatabaseConfig config = new H2DatabaseConfig(environment);

        String password = config.getGeneratedPassword();

        assertNotNull(password);
        assertFalse(password.isBlank());
    }

    @Test
    void generatedPassword_isDifferentEachTime() {
        MockEnvironment env1 = new MockEnvironment();
        MockEnvironment env2 = new MockEnvironment();
        H2DatabaseConfig config1 = new H2DatabaseConfig(env1);
        H2DatabaseConfig config2 = new H2DatabaseConfig(env2);

        assertNotEquals(config1.getGeneratedPassword(), config2.getGeneratedPassword());
    }

    @Test
    void generatedPassword_hasMinimumLength() {
        MockEnvironment environment = new MockEnvironment();
        H2DatabaseConfig config = new H2DatabaseConfig(environment);

        // Base64 encoding of 16 bytes should produce at least 21 characters
        assertTrue(config.getGeneratedPassword().length() >= 20);
    }

    @Test
    void constructor_setsPasswordInEnvironment() {
        MockEnvironment environment = new MockEnvironment();
        H2DatabaseConfig config = new H2DatabaseConfig(environment);

        String passwordFromEnv = environment.getProperty("spring.datasource.password");

        assertEquals(config.getGeneratedPassword(), passwordFromEnv);
    }

    @Test
    void h2CredentialsLogger_returnsNonNull() {
        MockEnvironment environment = new MockEnvironment();
        H2DatabaseConfig config = new H2DatabaseConfig(environment);

        assertNotNull(config.h2CredentialsLogger());
    }

    @Test
    void getGeneratedPassword_returnsSameValueOnMultipleCalls() {
        MockEnvironment environment = new MockEnvironment();
        H2DatabaseConfig config = new H2DatabaseConfig(environment);

        String password1 = config.getGeneratedPassword();
        String password2 = config.getGeneratedPassword();

        assertEquals(password1, password2);
    }
}
