package dev.danvega.repository;

import dev.danvega.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RoleRepository.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByName_existingRole_returnsRole() {
        // Given
        Role role = new Role("ROLE_TEST");
        entityManager.persistAndFlush(role);

        // When
        Optional<Role> found = roleRepository.findByName("ROLE_TEST");

        // Then
        assertTrue(found.isPresent());
        assertEquals("ROLE_TEST", found.get().getName());
    }

    @Test
    void findByName_nonExistentRole_returnsEmpty() {
        Optional<Role> found = roleRepository.findByName("ROLE_NONEXISTENT");

        assertTrue(found.isEmpty());
    }

    @Test
    void save_newRole_persistsRole() {
        Role role = new Role("ROLE_NEW");
        Role saved = roleRepository.save(role);

        assertNotNull(saved.getId());
        assertEquals("ROLE_NEW", saved.getName());
    }

    @Test
    void findAll_returnsAllRoles() {
        Role role1 = new Role("ROLE_ONE");
        Role role2 = new Role("ROLE_TWO");
        entityManager.persistAndFlush(role1);
        entityManager.persistAndFlush(role2);

        var allRoles = roleRepository.findAll();

        assertTrue(allRoles.size() >= 2);
    }
}
