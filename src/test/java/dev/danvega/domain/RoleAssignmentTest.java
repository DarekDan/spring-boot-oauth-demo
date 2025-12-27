package dev.danvega.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RoleAssignment entity.
 */
class RoleAssignmentTest {

    @Test
    void defaultConstructor_createsEmptyRoleAssignment() {
        RoleAssignment assignment = new RoleAssignment();

        assertNull(assignment.getId());
        assertNull(assignment.getUserIdentifier());
        assertNull(assignment.getRole());
    }

    @Test
    void parameterizedConstructor_setsValues() {
        Role role = new Role("ROLE_ADMIN");
        RoleAssignment assignment = new RoleAssignment("form:admin", role);

        assertNull(assignment.getId());
        assertEquals("form:admin", assignment.getUserIdentifier());
        assertEquals(role, assignment.getRole());
    }

    @Test
    void setters_setValues() {
        RoleAssignment assignment = new RoleAssignment();
        Role role = new Role("ROLE_USER");

        assignment.setId(1L);
        assignment.setUserIdentifier("google:user@example.com");
        assignment.setRole(role);

        assertEquals(1L, assignment.getId());
        assertEquals("google:user@example.com", assignment.getUserIdentifier());
        assertEquals(role, assignment.getRole());
    }

    @Test
    void toString_returnsFormattedString() {
        Role role = new Role("ROLE_ADMIN");
        role.setId(1L);
        RoleAssignment assignment = new RoleAssignment("form:admin", role);
        assignment.setId(10L);

        String result = assignment.toString();

        assertTrue(result.contains("RoleAssignment"));
        assertTrue(result.contains("id=10"));
        assertTrue(result.contains("userIdentifier='form:admin'"));
        assertTrue(result.contains("role="));
    }

    @Test
    void toString_handlesNullValues() {
        RoleAssignment assignment = new RoleAssignment();

        String result = assignment.toString();

        assertTrue(result.contains("id=null"));
        assertTrue(result.contains("userIdentifier='null'"));
        assertTrue(result.contains("role=null"));
    }
}
