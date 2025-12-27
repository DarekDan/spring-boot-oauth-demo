package dev.danvega.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Role entity.
 */
class RoleTest {

    @Test
    void defaultConstructor_createsEmptyRole() {
        Role role = new Role();

        assertNull(role.getId());
        assertNull(role.getName());
    }

    @Test
    void parameterizedConstructor_setsName() {
        Role role = new Role("ROLE_ADMIN");

        assertNull(role.getId());
        assertEquals("ROLE_ADMIN", role.getName());
    }

    @Test
    void setters_setValues() {
        Role role = new Role();

        role.setId(1L);
        role.setName("ROLE_USER");

        assertEquals(1L, role.getId());
        assertEquals("ROLE_USER", role.getName());
    }

    @Test
    void toString_returnsFormattedString() {
        Role role = new Role("ROLE_ADMIN");
        role.setId(1L);

        String result = role.toString();

        assertTrue(result.contains("Role"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("name='ROLE_ADMIN'"));
    }

    @Test
    void toString_handlesNullValues() {
        Role role = new Role();

        String result = role.toString();

        assertTrue(result.contains("id=null"));
        assertTrue(result.contains("name='null'"));
    }
}
