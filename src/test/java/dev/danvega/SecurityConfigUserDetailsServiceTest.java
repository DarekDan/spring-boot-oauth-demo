package dev.danvega;

import dev.danvega.domain.Role;
import dev.danvega.domain.RoleAssignment;
import dev.danvega.repository.RoleAssignmentRepository;
import dev.danvega.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserDetailsService that loads roles from database.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "GOOGLE_CLIENT_ID=",
        "GOOGLE_CLIENT_SECRET=",
        "GITHUB_CLIENT_ID=",
        "GITHUB_CLIENT_SECRET="
})
@Transactional
class SecurityConfigUserDetailsServiceTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Test
    void loadUserByUsername_adminUser_returnsUserWithRoles() {
        UserDetails user = userDetailsService.loadUserByUsername("admin");

        assertNotNull(user);
        assertEquals("admin", user.getUsername());
        assertTrue(user.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
    }

    @Test
    void loadUserByUsername_unknownUser_throwsException() {
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("unknownuser"));
    }

    @Test
    void loadUserByUsername_adminWithMultipleRoles_returnsAllRoles() {
        // Add extra role for admin
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role("ROLE_USER");
            return roleRepository.save(r);
        });
        roleAssignmentRepository.save(new RoleAssignment("form:admin", userRole));

        UserDetails user = userDetailsService.loadUserByUsername("admin");

        assertTrue(user.getAuthorities().size() >= 2);
    }
}
