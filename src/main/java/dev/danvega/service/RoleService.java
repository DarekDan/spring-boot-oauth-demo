package dev.danvega.service;

import dev.danvega.domain.RoleAssignment;
import dev.danvega.repository.RoleAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing and retrieving user roles from the database.
 */
@Service
public class RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    private final RoleAssignmentRepository roleAssignmentRepository;

    public RoleService(RoleAssignmentRepository roleAssignmentRepository) {
        this.roleAssignmentRepository = roleAssignmentRepository;
    }

    /**
     * Get all granted authorities for a user.
     *
     * @param userIdentifier the user identifier in format "provider:id"
     *                       (e.g., "form:admin", "google:user@gmail.com")
     * @return set of granted authorities for the user
     */
    public Set<GrantedAuthority> getRolesForUser(String userIdentifier) {
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUserIdentifier(userIdentifier);

        Set<GrantedAuthority> authorities = assignments.stream()
                .map(ra -> new SimpleGrantedAuthority(ra.getRole().getName()))
                .collect(Collectors.toSet());

        logger.debug("Found {} roles for user '{}': {}", authorities.size(), userIdentifier, authorities);

        return authorities;
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
