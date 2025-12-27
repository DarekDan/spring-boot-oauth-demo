package dev.danvega.authz.service;

import dev.danvega.authz.domain.RoleAssignment;
import dev.danvega.authz.repository.RoleAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing and retrieving user roles from the database.
 */
@Service
public class AuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);

    private final RoleAssignmentRepository roleAssignmentRepository;

    public AuthorizationService(RoleAssignmentRepository roleAssignmentRepository) {
        this.roleAssignmentRepository = roleAssignmentRepository;
    }

    /**
     * Get all role names for a user.
     *
     * @param userIdentifier the user identifier in format "provider:id"
     *                       (e.g., "form:admin", "google:user@gmail.com")
     * @return list of role names for the user
     */
    public List<String> getRolesForUser(String userIdentifier) {
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUserIdentifier(userIdentifier);

        List<String> roles = assignments.stream()
                .map(ra -> ra.getRole().getName())
                .collect(Collectors.toList());

        logger.debug("Found {} roles for user '{}': {}", roles.size(), userIdentifier, roles);

        return roles;
    }
}
