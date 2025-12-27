package dev.danvega.service;

import dev.danvega.domain.Role;
import dev.danvega.domain.RoleAssignment;
import dev.danvega.repository.RoleAssignmentRepository;
import dev.danvega.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RoleService with actual database.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "GOOGLE_CLIENT_ID=",
        "GOOGLE_CLIENT_SECRET=",
        "GITHUB_CLIENT_ID=",
        "GITHUB_CLIENT_SECRET="
})
@Transactional
class RoleServiceIntegrationTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Test
    void getRolesForUser_existingUser_returnsRoles() {
        // The seeded data should have form:admin with ROLE_ADMIN
        Set<GrantedAuthority> roles = roleService.getRolesForUser("form:admin");

        assertFalse(roles.isEmpty());
        assertTrue(roles.stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
    }

    @Test
    void getRolesForUser_googlePowerUser_returnsRoles() {
        // The seeded data should have google:ddanielewski@gmail.com with
        // ROLE_POWER_USER
        Set<GrantedAuthority> roles = roleService.getRolesForUser("google:ddanielewski@gmail.com");

        assertFalse(roles.isEmpty());
        assertTrue(roles.stream()
                .anyMatch(a -> "ROLE_POWER_USER".equals(a.getAuthority())));
    }

    @Test
    void getRolesForUser_newUser_addsAndRetrievesRoles() {
        // Create a new role assignment
        Role role = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role("ROLE_USER");
            return roleRepository.save(r);
        });
        roleAssignmentRepository.save(new RoleAssignment("github:newuser@example.com", role));

        // Retrieve roles
        Set<GrantedAuthority> roles = roleService.getRolesForUser("github:newuser@example.com");

        assertEquals(1, roles.size());
        assertTrue(roles.stream()
                .anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));
    }
}
