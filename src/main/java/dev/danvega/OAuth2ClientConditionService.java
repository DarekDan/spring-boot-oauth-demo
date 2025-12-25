package dev.danvega;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;

@Service
public class OAuth2ClientConditionService {

    private static final Logger log = LoggerFactory.getLogger(OAuth2ClientConditionService.class);

    private final ClientRegistrationRepository clientRegistrationRepository;

    public OAuth2ClientConditionService(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    public boolean isGoogleEnabled() {
        try {
            return clientRegistrationRepository.findByRegistrationId("google") != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isGithubEnabled() {
        try {
            return clientRegistrationRepository.findByRegistrationId("github") != null;
        } catch (Exception e) {
            return false;
        }
    }

    @PostConstruct
    public void validateConfiguration() {
        log.info("Checking OAuth2 Configuration...");
        if (isGoogleEnabled()) {
            log.info("✅ Google Login is configured.");
        } else {
            log.warn("⚠️ Google Login is NOT configured. (GOOGLE_CLIENT_ID is missing)");
        }

        if (isGithubEnabled()) {
            log.info("✅ GitHub Login is configured.");
        } else {
            log.warn("⚠️ GitHub Login is NOT configured. (GITHUB_CLIENT_ID is missing)");
        }
    }
}
