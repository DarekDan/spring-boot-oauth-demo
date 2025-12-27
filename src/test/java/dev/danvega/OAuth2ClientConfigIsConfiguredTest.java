package dev.danvega;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OAuth2ClientConfig's isConfigured method.
 * Tests all branches: null values, empty strings, blank strings, and valid
 * values.
 */
class OAuth2ClientConfigIsConfiguredTest {

    @Test
    void isConfigured_withBothValid_returnsTrue() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();

        boolean result = invokeIsConfigured(config, "valid-client-id", "valid-client-secret");

        assertTrue(result);
    }

    @Test
    void isConfigured_withNullClientId_returnsFalse() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();

        boolean result = invokeIsConfigured(config, null, "valid-client-secret");

        assertFalse(result);
    }

    @Test
    void isConfigured_withNullClientSecret_returnsFalse() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();

        boolean result = invokeIsConfigured(config, "valid-client-id", null);

        assertFalse(result);
    }

    @Test
    void isConfigured_withBothNull_returnsFalse() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();

        boolean result = invokeIsConfigured(config, null, null);

        assertFalse(result);
    }

    @Test
    void isConfigured_withEmptyClientId_returnsFalse() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();

        boolean result = invokeIsConfigured(config, "", "valid-client-secret");

        assertFalse(result);
    }

    @Test
    void isConfigured_withEmptyClientSecret_returnsFalse() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();

        boolean result = invokeIsConfigured(config, "valid-client-id", "");

        assertFalse(result);
    }

    @Test
    void isConfigured_withBlankClientId_returnsFalse() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();

        boolean result = invokeIsConfigured(config, "   ", "valid-client-secret");

        assertFalse(result);
    }

    @Test
    void isConfigured_withBlankClientSecret_returnsFalse() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();

        boolean result = invokeIsConfigured(config, "valid-client-id", "   ");

        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({
            "'', ''",
            "'   ', '   '",
            "'', 'secret'",
            "'id', ''",
            "'   ', 'secret'",
            "'id', '   '"
    })
    void isConfigured_withInvalidCombinations_returnsFalse(String clientId, String clientSecret) throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();

        boolean result = invokeIsConfigured(config, clientId, clientSecret);

        assertFalse(result);
    }

    @Test
    void hasAnyOAuth2Provider_withNeitherConfigured_returnsFalse() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();
        // Fields are null by default

        boolean result = config.hasAnyOAuth2Provider();

        assertFalse(result);
    }

    @Test
    void hasAnyOAuth2Provider_withOnlyGoogleConfigured_returnsTrue() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();
        setField(config, "googleClientId", "google-client-id");
        setField(config, "googleClientSecret", "google-client-secret");

        boolean result = config.hasAnyOAuth2Provider();

        assertTrue(result);
    }

    @Test
    void hasAnyOAuth2Provider_withOnlyGitHubConfigured_returnsTrue() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();
        setField(config, "githubClientId", "github-client-id");
        setField(config, "githubClientSecret", "github-client-secret");

        boolean result = config.hasAnyOAuth2Provider();

        assertTrue(result);
    }

    @Test
    void hasAnyOAuth2Provider_withBothConfigured_returnsTrue() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();
        setField(config, "googleClientId", "google-client-id");
        setField(config, "googleClientSecret", "google-client-secret");
        setField(config, "githubClientId", "github-client-id");
        setField(config, "githubClientSecret", "github-client-secret");

        boolean result = config.hasAnyOAuth2Provider();

        assertTrue(result);
    }

    @Test
    void hasAnyOAuth2Provider_withGoogleBlankAndGitHubConfigured_returnsTrue() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();
        setField(config, "googleClientId", "");
        setField(config, "googleClientSecret", "");
        setField(config, "githubClientId", "github-client-id");
        setField(config, "githubClientSecret", "github-client-secret");

        boolean result = config.hasAnyOAuth2Provider();

        assertTrue(result);
    }

    @Test
    void hasAnyOAuth2Provider_withGoogleConfiguredAndGitHubBlank_returnsTrue() throws Exception {
        OAuth2ClientConfig config = new OAuth2ClientConfig();
        setField(config, "googleClientId", "google-client-id");
        setField(config, "googleClientSecret", "google-client-secret");
        setField(config, "githubClientId", "");
        setField(config, "githubClientSecret", "");

        boolean result = config.hasAnyOAuth2Provider();

        assertTrue(result);
    }

    // Helper method to invoke private isConfigured method
    private boolean invokeIsConfigured(OAuth2ClientConfig config, String clientId, String clientSecret)
            throws Exception {
        Method method = OAuth2ClientConfig.class.getDeclaredMethod("isConfigured", String.class, String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(config, clientId, clientSecret);
    }

    // Helper method to set private field values
    private void setField(OAuth2ClientConfig config, String fieldName, String value) throws Exception {
        Field field = OAuth2ClientConfig.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(config, value);
    }
}
