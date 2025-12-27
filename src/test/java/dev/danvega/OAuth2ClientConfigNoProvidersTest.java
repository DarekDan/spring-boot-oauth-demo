package dev.danvega;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OAuth2ClientConfig with no providers configured.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "GOOGLE_CLIENT_ID=",
        "GOOGLE_CLIENT_SECRET=",
        "GITHUB_CLIENT_ID=",
        "GITHUB_CLIENT_SECRET="
})
class OAuth2ClientConfigNoProvidersTest {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private OAuth2ClientConfig oauth2ClientConfig;

    @Test
    void clientRegistrationRepository_withNoProviders_returnsEmptyRepository() {
        assertNull(clientRegistrationRepository.findByRegistrationId("google"));
        assertNull(clientRegistrationRepository.findByRegistrationId("github"));
    }

    @Test
    void hasAnyOAuth2Provider_withNoProviders_returnsFalse() {
        assertFalse(oauth2ClientConfig.hasAnyOAuth2Provider());
    }
}
