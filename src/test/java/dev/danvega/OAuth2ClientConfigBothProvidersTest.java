package dev.danvega;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OAuth2ClientConfig with both providers configured.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "GOOGLE_CLIENT_ID=test-google-client-id",
        "GOOGLE_CLIENT_SECRET=test-google-client-secret",
        "GITHUB_CLIENT_ID=test-github-client-id",
        "GITHUB_CLIENT_SECRET=test-github-client-secret"
})
class OAuth2ClientConfigBothProvidersTest {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private OAuth2ClientConfig oauth2ClientConfig;

    @Test
    void clientRegistrationRepository_withBothProviders_registersGoogle() {
        ClientRegistration google = clientRegistrationRepository.findByRegistrationId("google");

        assertNotNull(google);
        assertEquals("test-google-client-id", google.getClientId());
    }

    @Test
    void clientRegistrationRepository_withBothProviders_registersGitHub() {
        ClientRegistration github = clientRegistrationRepository.findByRegistrationId("github");

        assertNotNull(github);
        assertEquals("test-github-client-id", github.getClientId());
    }

    @Test
    void hasAnyOAuth2Provider_withBothProviders_returnsTrue() {
        assertTrue(oauth2ClientConfig.hasAnyOAuth2Provider());
    }
}
