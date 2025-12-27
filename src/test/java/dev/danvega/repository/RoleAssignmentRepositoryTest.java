package dev.danvega.repository;

import dev.danvega.domain.Role;
import dev.danvega.domain.RoleAssignment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RoleAssignmentRepository.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RoleAssignmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Test
    void findByUserIdentifier_existingAssignment_returnsAssignments() {
        // Given
        Role role = new Role("ROLE_ADMIN");
        entityManager.persistAndFlush(role);

        RoleAssignment assignment = new RoleAssignment("form:testuser", role);
        entityManager.persistAndFlush(assignment);

        // When
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUserIdentifier("form:testuser");

        // Then
        assertEquals(1, assignments.size());
        assertEquals("form:testuser", assignments.get(0).getUserIdentifier());
        assertEquals("ROLE_ADMIN", assignments.get(0).getRole().getName());
    }

    @Test
    void findByUserIdentifier_nonExistentUser_returnsEmptyList() {
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUserIdentifier("form:unknown");

        assertTrue(assignments.isEmpty());
    }

    @Test
    void findByUserIdentifier_multipleRoles_returnsAllAssignments() {
        // Given
        Role adminRole = new Role("ROLE_ADMIN");
        Role userRole = new Role("ROLE_USER");
        entityManager.persistAndFlush(adminRole);
        entityManager.persistAndFlush(userRole);

        RoleAssignment adminAssignment = new RoleAssignment("form:multiuser", adminRole);
        RoleAssignment userAssignment = new RoleAssignment("form:multiuser", userRole);
        entityManager.persistAndFlush(adminAssignment);
        entityManager.persistAndFlush(userAssignment);

        // When
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUserIdentifier("form:multiuser");

        // Then
        assertEquals(2, assignments.size());
    }

    @Test
    void save_newAssignment_persistsAssignment() {
        // Given
        Role role = new Role("ROLE_NEW");
        entityManager.persistAndFlush(role);

        // When
        RoleAssignment assignment = new RoleAssignment("google:newuser@example.com", role);
        RoleAssignment saved = roleAssignmentRepository.save(assignment);

        // Then
        assertNotNull(saved.getId());
        assertEquals("google:newuser@example.com", saved.getUserIdentifier());
    }

    @Test
    void findByUserIdentifier_oAuth2Format_returnsAssignment() {
        // Given
        Role role = new Role("ROLE_POWER_USER");
        entityManager.persistAndFlush(role);

        RoleAssignment assignment = new RoleAssignment("google:user@gmail.com", role);
        entityManager.persistAndFlush(assignment);

        // When
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUserIdentifier("google:user@gmail.com");

        // Then
        assertEquals(1, assignments.size());
        assertEquals("ROLE_POWER_USER", assignments.get(0).getRole().getName());
    }
}
