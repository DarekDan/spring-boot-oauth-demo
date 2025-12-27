package dev.danvega.authz.domain;

import jakarta.persistence.*;

/**
 * Entity representing a role assignment to a user.
 * User is identified by a string in the format "provider:identifier"
 * (e.g., "form:admin", "google:user@gmail.com", "github:user@example.com")
 */
@Entity
@Table(name = "role_assignments")
public class RoleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String userIdentifier;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public RoleAssignment() {
    }

    public RoleAssignment(String userIdentifier, Role role) {
        this.userIdentifier = userIdentifier;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "RoleAssignment{" +
                "id=" + id +
                ", userIdentifier='" + userIdentifier + '\'' +
                ", role=" + role +
                '}';
    }
}
