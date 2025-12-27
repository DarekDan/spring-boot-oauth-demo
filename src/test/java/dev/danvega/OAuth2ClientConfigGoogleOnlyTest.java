package dev.danvega;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OAuth2ClientConfig with Google provider configured.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "GOOGLE_CLIENT_ID=test-google-client-id",
        "GOOGLE_CLIENT_SECRET=test-google-client-secret",
        "GITHUB_CLIENT_ID=",
        "GITHUB_CLIENT_SECRET="
})
class OAuth2ClientConfigGoogleOnlyTest {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private OAuth2ClientConfig oauth2ClientConfig;

    @Test
    void clientRegistrationRepository_withGoogleOnly_registersGoogle() {
        ClientRegistration google = clientRegistrationRepository.findByRegistrationId("google");

        assertNotNull(google);
        assertEquals("test-google-client-id", google.getClientId());
        assertEquals("Google", google.getClientName());
    }

    @Test
    void clientRegistrationRepository_withGoogleOnly_doesNotRegisterGitHub() {
        assertNull(clientRegistrationRepository.findByRegistrationId("github"));
    }

    @Test
    void hasAnyOAuth2Provider_withGoogleOnly_returnsTrue() {
        assertTrue(oauth2ClientConfig.hasAnyOAuth2Provider());
    }
}
