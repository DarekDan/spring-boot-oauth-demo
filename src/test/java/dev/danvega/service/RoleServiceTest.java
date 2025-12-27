package dev.danvega.service;

import dev.danvega.domain.Role;
import dev.danvega.domain.RoleAssignment;
import dev.danvega.repository.RoleAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleService.
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleAssignmentRepository roleAssignmentRepository;

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleService(roleAssignmentRepository);
    }

    @Test
    void getRolesForUser_withNoRoles_returnsEmptySet() {
        when(roleAssignmentRepository.findByUserIdentifier("form:unknown"))
                .thenReturn(Collections.emptyList());

        Set<GrantedAuthority> authorities = roleService.getRolesForUser("form:unknown");

        assertTrue(authorities.isEmpty());
        verify(roleAssignmentRepository).findByUserIdentifier("form:unknown");
    }

    @Test
    void getRolesForUser_withSingleRole_returnsAuthority() {
        Role role = new Role("ROLE_ADMIN");
        RoleAssignment assignment = new RoleAssignment("form:admin", role);
        when(roleAssignmentRepository.findByUserIdentifier("form:admin"))
                .thenReturn(List.of(assignment));

        Set<GrantedAuthority> authorities = roleService.getRolesForUser("form:admin");

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
    }

    @Test
    void getRolesForUser_withMultipleRoles_returnsAllAuthorities() {
        Role adminRole = new Role("ROLE_ADMIN");
        Role userRole = new Role("ROLE_USER");
        RoleAssignment adminAssignment = new RoleAssignment("form:admin", adminRole);
        RoleAssignment userAssignment = new RoleAssignment("form:admin", userRole);
        when(roleAssignmentRepository.findByUserIdentifier("form:admin"))
                .thenReturn(List.of(adminAssignment, userAssignment));

        Set<GrantedAuthority> authorities = roleService.getRolesForUser("form:admin");

        assertEquals(2, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
        assertTrue(authorities.stream()
                .anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));
    }

    @Test
    void buildFormUserIdentifier_returnsFormattedIdentifier() {
        String identifier = RoleService.buildFormUserIdentifier("testuser");

        assertEquals("form:testuser", identifier);
    }

    @Test
    void buildFormUserIdentifier_handlesSpecialCharacters() {
        String identifier = RoleService.buildFormUserIdentifier("user@domain.com");

        assertEquals("form:user@domain.com", identifier);
    }

    @Test
    void buildOAuth2UserIdentifier_returnsFormattedIdentifier() {
        String identifier = RoleService.buildOAuth2UserIdentifier("google", "user@gmail.com");

        assertEquals("google:user@gmail.com", identifier);
    }

    @Test
    void buildOAuth2UserIdentifier_handlesGitHub() {
        String identifier = RoleService.buildOAuth2UserIdentifier("github", "developer@github.com");

        assertEquals("github:developer@github.com", identifier);
    }
}
