package dev.danvega.authz.controller;

import dev.danvega.authz.service.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API controller for authorization queries.
 * This service is internal and should not be exposed directly to the internet.
 */
@RestController
@RequestMapping("/api/authorization")
public class AuthorizationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

    private final AuthorizationService authorizationService;

    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * Get roles for a specific user.
     * 
     * @param userIdentifier user identifier in format "provider:id"
     * @return list of role names
     */
    @GetMapping("/roles/{userIdentifier}")
    public ResponseEntity<Map<String, Object>> getRoles(@PathVariable String userIdentifier) {
        logger.info("Authorization request for user: {}", userIdentifier);

        List<String> roles = authorizationService.getRolesForUser(userIdentifier);

        return ResponseEntity.ok(Map.of(
                "userIdentifier", userIdentifier,
                "roles", roles));
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "authorization-service"));
    }
}
