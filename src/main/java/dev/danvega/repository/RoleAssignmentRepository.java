package dev.danvega.repository;

import dev.danvega.domain.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for RoleAssignment entities.
 */
@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Long> {

    /**
     * Find all role assignments for a given user identifier.
     * 
     * @param userIdentifier the user identifier (e.g., "form:admin",
     *                       "google:user@gmail.com")
     * @return list of role assignments for the user
     */
    List<RoleAssignment> findByUserIdentifier(String userIdentifier);
}
