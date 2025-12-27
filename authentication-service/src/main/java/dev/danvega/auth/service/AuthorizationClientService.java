package dev.danvega.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that communicates with the authorization-service to retrieve user
 * roles.
 */
@Service
public class AuthorizationClientService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationClientService.class);

    @Value("${authorization.service.url}")
    private String authorizationServiceUrl;

    private final RestTemplate restTemplate;

    public AuthorizationClientService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get all granted authorities for a user from the authorization service.
     *
     * @param userIdentifier the user identifier in format "provider:id"
     *                       (e.g., "form:admin", "google:user@gmail.com")
     * @return set of granted authorities for the user
     */
    public Set<GrantedAuthority> getRolesForUser(String userIdentifier) {
        try {
            String url = authorizationServiceUrl + "/api/authorization/roles/" + userIdentifier;

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) response.get("roles");

                Set<GrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());

                logger.debug("Retrieved {} roles for user '{}': {}", authorities.size(), userIdentifier, authorities);
                return authorities;
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve roles for user '{}': {}", userIdentifier, e.getMessage());
        }

        return Collections.emptySet();
    }

    /**
     * Build a user identifier for form-based login.
     *
     * @param username the username
     * @return the user identifier
     */
    public static String buildFormUserIdentifier(String username) {
        return "form:" + username;
    }

    /**
     * Build a user identifier for OAuth2 login.
     *
     * @param provider the OAuth2 provider (e.g., "google", "github")
     * @param email    the user's email
     * @return the user identifier
     */
    public static String buildOAuth2UserIdentifier(String provider, String email) {
        return provider + ":" + email;
    }
}
