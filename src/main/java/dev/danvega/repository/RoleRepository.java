package dev.danvega.repository;

import dev.danvega.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entities.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by its name.
     * 
     * @param name the role name (e.g., "ROLE_ADMIN")
     * @return the role if found
     */
    Optional<Role> findByName(String name);
}
