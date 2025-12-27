package dev.danvega;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OAuth2ClientConditionService.
 */
@ExtendWith(MockitoExtension.class)
class OAuth2ClientConditionServiceTest {

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @Mock
    private ClientRegistration googleRegistration;

    @Mock
    private ClientRegistration githubRegistration;

    @Test
    void isGoogleEnabled_whenGoogleConfigured_returnsTrue() {
        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(googleRegistration);

        OAuth2ClientConditionService service = new OAuth2ClientConditionService(clientRegistrationRepository);

        assertTrue(service.isGoogleEnabled());
    }

    @Test
    void isGoogleEnabled_whenGoogleNotConfigured_returnsFalse() {
        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(null);

        OAuth2ClientConditionService service = new OAuth2ClientConditionService(clientRegistrationRepository);

        assertFalse(service.isGoogleEnabled());
    }

    @Test
    void isGoogleEnabled_whenExceptionThrown_returnsFalse() {
        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenThrow(new RuntimeException("Test exception"));

        OAuth2ClientConditionService service = new OAuth2ClientConditionService(clientRegistrationRepository);

        assertFalse(service.isGoogleEnabled());
    }

    @Test
    void isGithubEnabled_whenGithubConfigured_returnsTrue() {
        when(clientRegistrationRepository.findByRegistrationId("github"))
                .thenReturn(githubRegistration);

        OAuth2ClientConditionService service = new OAuth2ClientConditionService(clientRegistrationRepository);

        assertTrue(service.isGithubEnabled());
    }

    @Test
    void isGithubEnabled_whenGithubNotConfigured_returnsFalse() {
        when(clientRegistrationRepository.findByRegistrationId("github"))
                .thenReturn(null);

        OAuth2ClientConditionService service = new OAuth2ClientConditionService(clientRegistrationRepository);

        assertFalse(service.isGithubEnabled());
    }

    @Test
    void isGithubEnabled_whenExceptionThrown_returnsFalse() {
        when(clientRegistrationRepository.findByRegistrationId("github"))
                .thenThrow(new RuntimeException("Test exception"));

        OAuth2ClientConditionService service = new OAuth2ClientConditionService(clientRegistrationRepository);

        assertFalse(service.isGithubEnabled());
    }

    @Test
    void validateConfiguration_logsConfigurationStatus() {
        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(googleRegistration);
        when(clientRegistrationRepository.findByRegistrationId("github"))
                .thenReturn(null);

        OAuth2ClientConditionService service = new OAuth2ClientConditionService(clientRegistrationRepository);

        // This should not throw any exception
        assertDoesNotThrow(() -> service.validateConfiguration());
    }

    @Test
    void validateConfiguration_withBothProviders_logsSuccess() {
        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(googleRegistration);
        when(clientRegistrationRepository.findByRegistrationId("github"))
                .thenReturn(githubRegistration);

        OAuth2ClientConditionService service = new OAuth2ClientConditionService(clientRegistrationRepository);

        assertDoesNotThrow(() -> service.validateConfiguration());
    }

    @Test
    void validateConfiguration_withNoProviders_logsWarnings() {
        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(null);
        when(clientRegistrationRepository.findByRegistrationId("github"))
                .thenReturn(null);

        OAuth2ClientConditionService service = new OAuth2ClientConditionService(clientRegistrationRepository);

        assertDoesNotThrow(() -> service.validateConfiguration());
    }
}
