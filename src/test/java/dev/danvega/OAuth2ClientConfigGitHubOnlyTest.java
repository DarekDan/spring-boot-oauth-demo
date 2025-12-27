package dev.danvega;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OAuth2ClientConfig with GitHub provider configured.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "GOOGLE_CLIENT_ID=",
        "GOOGLE_CLIENT_SECRET=",
        "GITHUB_CLIENT_ID=test-github-client-id",
        "GITHUB_CLIENT_SECRET=test-github-client-secret"
})
class OAuth2ClientConfigGitHubOnlyTest {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private OAuth2ClientConfig oauth2ClientConfig;

    @Test
    void clientRegistrationRepository_withGitHubOnly_registersGitHub() {
        ClientRegistration github = clientRegistrationRepository.findByRegistrationId("github");

        assertNotNull(github);
        assertEquals("test-github-client-id", github.getClientId());
        assertEquals("GitHub", github.getClientName());
    }

    @Test
    void clientRegistrationRepository_withGitHubOnly_doesNotRegisterGoogle() {
        assertNull(clientRegistrationRepository.findByRegistrationId("google"));
    }

    @Test
    void hasAnyOAuth2Provider_withGitHubOnly_returnsTrue() {
        assertTrue(oauth2ClientConfig.hasAnyOAuth2Provider());
    }
}
