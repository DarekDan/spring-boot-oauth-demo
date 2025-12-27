package dev.danvega.domain;

import jakarta.persistence.*;

/**
 * Entity representing the assignment of a role to a user.
 * User is identified by a string: "provider:identifier" format.
 * Examples: "form:admin", "google:user@gmail.com", "github:username"
 */
@Entity
@Table(name = "role_assignments", indexes = {
        @Index(name = "idx_user_identifier", columnList = "user_identifier")
})
public class RoleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_identifier", nullable = false)
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
