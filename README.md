# Spring Boot OAuth2 Login Demo

A comprehensive Spring Boot application demonstrating modern authentication patterns including:

- **Form-based authentication** with username/password
- **OAuth2/OIDC authentication** with Google and GitHub
- **Database-backed role management** with H2 and Liquibase
- **JTE (Java Template Engine)** for server-side rendering with Tailwind CSS
- **CSRF protection** and security best practices

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Authentication Flow](#authentication-flow)
5. [Role Management System](#role-management-system)
6. [Configuration Reference](#configuration-reference)
7. [API & Endpoints](#api--endpoints)
8. [Security Configuration](#security-configuration)
9. [Templates & UI](#templates--ui)
10. [Database Schema](#database-schema)
11. [Development Guide](#development-guide)
12. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Spring Boot Application                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐          │
│  │   Controllers   │    │    Security     │    │     OAuth2      │          │
│  │                 │    │   Filter Chain  │    │   Client Config │          │
│  │ - LoginController    │                 │    │                 │          │
│  │ - DashboardController│ - Form Login    │    │ - Google        │          │
│  │                 │    │ - OAuth2 Login  │    │ - GitHub        │          │
│  └────────┬────────┘    └────────┬────────┘    └────────┬────────┘          │
│           │                      │                      │                   │
│           ▼                      ▼                      ▼                   │
│  ┌─────────────────────────────────────────────────────────────────┐        │
│  │                        Service Layer                            │        │
│  │  ┌─────────────────┐  ┌─────────────────────────────────────┐   │        │
│  │  │   RoleService   │  │ OAuth2ClientConditionService        │   │        │
│  │  │                 │  │                                     │   │        │
│  │  │ - getRolesForUser  │ - isGoogleEnabled()                 │   │        │
│  │  │ - buildUserIdentifier - isGithubEnabled()                │   │        │
│  │  └────────┬────────┘  └─────────────────────────────────────┘   │        │
│  └───────────┼─────────────────────────────────────────────────────┘        │
│              │                                                              │
│              ▼                                                              │
│  ┌─────────────────────────────────────────────────────────────────┐        │
│  │                     Repository Layer (JPA)                      │        │
│  │  ┌─────────────────┐  ┌─────────────────────────────────────┐   │        │
│  │  │ RoleRepository  │  │ RoleAssignmentRepository            │   │        │
│  │  └────────┬────────┘  └────────┬────────────────────────────┘   │        │
│  └───────────┼────────────────────┼────────────────────────────────┘        │
│              │                    │                                         │
│              ▼                    ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────┐        │
│  │                    H2 In-Memory Database                        │        │
│  │                    (Managed by Liquibase)                       │        │
│  │  ┌──────────────┐  ┌──────────────────────────┐                 │        │
│  │  │    ROLES     │  │    ROLE_ASSIGNMENTS      │                 │        │
│  │  │              │  │                          │                 │        │
│  │  │ id | name    │  │ id | user_identifier | role_id             │        │
│  │  └──────────────┘  └──────────────────────────┘                 │        │
│  └─────────────────────────────────────────────────────────────────┘        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Framework** | Spring Boot | 3.5.9 |
| **Language** | Java | 23 |
| **Security** | Spring Security | 6.x |
| **OAuth2** | Spring Security OAuth2 Client | 6.x |
| **Template Engine** | JTE (Java Template Engine) | 3.2.1 |
| **CSS Framework** | Tailwind CSS | CDN |
| **Database** | H2 (In-Memory) | Runtime |
| **Migrations** | Liquibase | Managed by Spring Boot |
| **ORM** | Spring Data JPA / Hibernate | 6.x |
| **Build Tool** | Maven | 3.x |

---

## Project Structure

```
spring-boot-oauth-demo/
├── pom.xml                                    # Maven build configuration
├── README.md                                  # This documentation
│
├── src/main/
│   ├── java/dev/danvega/
│   │   │
│   │   │── Application.java                   # Spring Boot entry point
│   │   │
│   │   │── # === CONTROLLERS ===
│   │   ├── LoginController.java               # Login/home page endpoints
│   │   ├── DashboardController.java           # Dashboard (protected) endpoint
│   │   │
│   │   │── # === SECURITY ===
│   │   ├── SecurityConfig.java                # Spring Security configuration
│   │   ├── OAuth2ClientConfig.java            # OAuth2 provider registration
│   │   ├── OAuth2ClientConditionService.java  # OAuth2 availability checker
│   │   │
│   │   │── # === CSRF SUPPORT ===
│   │   ├── CsrfHiddenInput.java               # JTE-compatible CSRF token
│   │   ├── CsrfTokenAdvice.java               # Controller advice for CSRF
│   │   │
│   │   │── # === DOMAIN LAYER ===
│   │   ├── domain/
│   │   │   ├── Role.java                      # Role entity
│   │   │   └── RoleAssignment.java            # User-to-role mapping entity
│   │   │
│   │   │── # === REPOSITORY LAYER ===
│   │   ├── repository/
│   │   │   ├── RoleRepository.java            # Role data access
│   │   │   └── RoleAssignmentRepository.java  # Role assignment data access
│   │   │
│   │   │── # === SERVICE LAYER ===
│   │   └── service/
│   │       └── RoleService.java               # Role retrieval logic
│   │
│   ├── resources/
│   │   ├── application.yaml                   # Application configuration
│   │   │
│   │   └── db/changelog/                      # Liquibase migrations
│   │       ├── db.changelog-master.yaml       # Master changelog
│   │       ├── 001-create-roles-schema.yaml   # Schema creation
│   │       └── 002-seed-roles-data.yaml       # Initial data
│   │
│   └── jte/                                   # JTE templates
│       ├── layout/
│       │   └── default.jte                    # Base layout template
│       └── pages/
│           ├── home.jte                       # Home page
│           ├── login.jte                      # Login page
│           └── dashboard.jte                  # Dashboard page
│
└── src/test/                                  # Test classes
```

---

## Authentication Flow

### Form-Based Authentication

```
┌──────────┐     ┌───────────────┐     ┌─────────────────┐     ┌────────────┐
│  Browser │────▶│ /login (GET)  │────▶│ Login Form      │────▶│  Display   │
└──────────┘     └───────────────┘     │ (login.jte)     │     │  Login UI  │
                                       └─────────────────┘     └────────────┘
                                                                      │
                                                                      ▼
┌──────────┐     ┌───────────────┐     ┌─────────────────┐     ┌────────────┐
│ Dashboard│◀────│ /dashboard    │◀────│ Authentication  │◀────│ POST /login│
│   Page   │     │ (redirect)    │     │ Success         │     │ username   │
└──────────┘     └───────────────┘     └─────────────────┘     │ password   │
                                              │                └────────────┘
                                              ▼
                                       ┌─────────────────┐
                                       │ RoleService     │
                                       │ .getRolesForUser│
                                       │ ("form:admin")  │
                                       └─────────────────┘
```

### OAuth2/OIDC Authentication (Google)

```
┌──────────┐     ┌─────────────────────────────┐     ┌────────────────────┐
│  Browser │────▶│ /oauth2/authorization/google│────▶│ Redirect to Google │
└──────────┘     └─────────────────────────────┘     └────────────────────┘
                                                              │
                                                              ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                        Google OAuth2 Server                              │
│  1. User authenticates with Google credentials                           │
│  2. User consents to requested scopes                                    │
│  3. Google redirects back with authorization code                        │
└──────────────────────────────────────────────────────────────────────────┘
                                                              │
                                                              ▼
┌──────────┐     ┌───────────────────────────────┐     ┌────────────────────┐
│ Dashboard│◀────│ /login/oauth2/code/google     │◀────│ Exchange code for  │
│   Page   │     │                               │     │ tokens, load user  │
└──────────┘     └───────────────────────────────┘     └────────────────────┘
                                                              │
                                                              ▼
                                                       ┌─────────────────┐
                                                       │ OidcUserService │
                                                       │ + RoleService   │
                                                       │ .getRolesForUser│
                                                       │ ("google:email")│
                                                       └─────────────────┘
```

---

## Role Management System

### User Identifier Format

The system uses a unified user identifier format: `provider:identifier`

| Login Type | Format | Example |
|------------|--------|---------|
| Form login | `form:<username>` | `form:admin` |
| Google OAuth2 | `google:<email>` | `google:user@gmail.com` |
| GitHub OAuth2 | `github:<email>` | `github:user@github.com` |

### Database Tables

```sql
-- ROLES: Defines available roles in the system
CREATE TABLE roles (
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    name    VARCHAR(50) NOT NULL UNIQUE  -- e.g., 'ROLE_ADMIN'
);

-- ROLE_ASSIGNMENTS: Maps users to roles
CREATE TABLE role_assignments (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_identifier VARCHAR(255) NOT NULL,  -- e.g., 'form:admin'
    role_id         BIGINT NOT NULL REFERENCES roles(id)
);

CREATE INDEX idx_user_identifier ON role_assignments(user_identifier);
```

### Pre-Seeded Data

| Role | Description |
|------|-------------|
| `ROLE_USER` | Basic user access |
| `ROLE_ADMIN` | Administrative privileges |
| `ROLE_POWER_USER` | Enhanced user privileges |

| User Identifier | Roles |
|-----------------|-------|
| `form:admin` | `ROLE_ADMIN` |
| `google:ddanielewski@gmail.com` | `ROLE_POWER_USER` |

### Adding New Role Assignments

Via H2 Console (`http://localhost:8080/h2-console`):

```sql
-- Assign ROLE_USER to a GitHub user
INSERT INTO role_assignments (user_identifier, role_id)
VALUES ('github:newuser@example.com', (SELECT id FROM roles WHERE name = 'ROLE_USER'));

-- Add multiple roles to a user
INSERT INTO role_assignments (user_identifier, role_id)
VALUES ('form:admin', (SELECT id FROM roles WHERE name = 'ROLE_USER'));
```

---

## Configuration Reference

### application.yaml

```yaml
spring:
  application:
    name: jte-login

  # === DATABASE CONFIGURATION ===
  datasource:
    url: jdbc:h2:mem:rolesdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:      # Empty password for development

  h2:
    console:
      enabled: true      # Enable H2 web console
      path: /h2-console  # Console URL path

  jpa:
    hibernate:
      ddl-auto: none     # Liquibase manages schema
    show-sql: true       # Log SQL statements

  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml

  # === OUTPUT CONFIGURATION ===
  output:
    ansi:
      enabled: always    # Colored console output
  banner:
    charset: UTF-8

# === JTE CONFIGURATION ===
gg:
  jte:
    developmentMode: true  # Hot-reload templates

# === LOGGING ===
logging:
  level:
    org.springframework.security: ERROR  # Change to DEBUG for security logs
  charset:
    console: UTF-8

# === SERVER ===
server:
  servlet:
    encoding:
      charset: UTF-8
      force: true
```

### Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `GOOGLE_CLIENT_ID` | Optional | Google OAuth2 Client ID |
| `GOOGLE_CLIENT_SECRET` | Optional | Google OAuth2 Client Secret |
| `GITHUB_CLIENT_ID` | Optional | GitHub OAuth2 Client ID |
| `GITHUB_CLIENT_SECRET` | Optional | GitHub OAuth2 Client Secret |

> **Note**: OAuth2 providers are only enabled when their credentials are set. The application works with form login only if no OAuth2 credentials are configured.

---

## API & Endpoints

### Public Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/` | Home page with login link |
| `GET` | `/login` | Login page (form + OAuth2 buttons) |
| `POST` | `/login` | Form login submission |
| `GET` | `/error` | Error page |

### Protected Endpoints

| Method | Path | Description | Required Auth |
|--------|------|-------------|---------------|
| `GET` | `/dashboard` | User dashboard | Any authenticated user |
| `GET` | `/h2-console/**` | H2 Database Console | `ROLE_ADMIN` or `ROLE_POWER_USER` |

#### H2 Console Access Control

The H2 console has role-based access control and uses a **randomly generated password** for security:

| Role | Access Level | Connection Credentials |
|------|--------------|------------------------|
| `ROLE_ADMIN` | **Full read/write** | Username: `sa`, Password: **(shown in console at startup)** |
| `ROLE_POWER_USER` | **Read-only** | Username: `readonly`, Password: `readonly` |

> **Security Note**: The `sa` user password is randomly generated on each application startup. Check the application console output for the current password:
>
> ```
> ╔════════════════════════════════════════════════════════════════╗
> ║                    H2 DATABASE CREDENTIALS                     ║
> ╠════════════════════════════════════════════════════════════════╣
> ║  JDBC URL:  jdbc:h2:mem:rolesdb;DB_CLOSE_DELAY=-1              ║
> ║  Username:  sa                                                 ║
> ║  Password:  <randomly-generated-password>                      ║
> ╠════════════════════════════════════════════════════════════════╣
> ║  H2 Console: http://localhost:8080/h2-console                  ║
> ║  Note: Password changes on each application restart            ║
> ╚════════════════════════════════════════════════════════════════╝
> ```

> **Important**: The read-only enforcement is done at the database level. POWER_USER must connect using the `readonly` credentials to ensure they cannot modify data.

### OAuth2 Endpoints (Auto-configured)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/oauth2/authorization/google` | Initiate Google OAuth2 flow |
| `GET` | `/oauth2/authorization/github` | Initiate GitHub OAuth2 flow |
| `GET` | `/login/oauth2/code/google` | Google OAuth2 callback |
| `GET` | `/login/oauth2/code/github` | GitHub OAuth2 callback |

---

## Security Configuration

### SecurityConfig.java Key Features

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. URL Authorization Rules
    .authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/", "/login", "/error").permitAll()
        .requestMatchers("/h2-console/**").permitAll()
        .anyRequest().authenticated())

    // 2. Form Login Configuration
    .formLogin(form -> form
        .loginPage("/login")
        .defaultSuccessUrl("/dashboard", true))

    // 3. Logout Configuration
    .logout(logout -> logout
        .logoutSuccessUrl("/"))

    // 4. H2 Console Support
    .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
    .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))

    // 5. OAuth2 Login (conditional)
    if (hasOAuth2Providers()) {
        http.oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .userInfoEndpoint(userInfo -> userInfo
                .oidcUserService(oidcUserService())));
    }
}
```

### Custom OIDC User Service

The `oidcUserService()` method enriches OAuth2 users with database roles:

```java
private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
    return (userRequest) -> {
        OidcUser oidcUser = delegate.loadUser(userRequest);
        
        // Build identifier: "google:user@gmail.com"
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = oidcUser.getAttribute("email");
        String userIdentifier = provider + ":" + email;
        
        // Load roles from database
        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        authorities.addAll(roleService.getRolesForUser(userIdentifier));
        
        // Check for POWER_USER to add custom claim
        boolean isPowerUser = authorities.stream()
            .anyMatch(auth -> "ROLE_POWER_USER".equals(auth.getAuthority()));
        
        if (isPowerUser) {
            // Add custom claim
            claims.put("custom_claim", "Power User Active");
        }
        
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), userInfo);
    };
}
```

---

## Templates & UI

### Template Structure

```
jte/
├── layout/
│   └── default.jte      # Base HTML template with Tailwind CSS
└── pages/
    ├── home.jte         # Welcome page with login button
    ├── login.jte        # Login form + OAuth2 buttons
    └── dashboard.jte    # User info and roles display
```

### Base Layout (default.jte)

```html
@param gg.jte.Content content

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Spring Security Demo</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100">
${content}
</body>
</html>
```

### Login Page Features (login.jte)

- Username/password form with CSRF protection
- Error message display
- Conditional OAuth2 buttons (Google/GitHub)
- Responsive Tailwind CSS styling

### Dashboard Features (dashboard.jte)

- Welcome message with username/email
- Role badges display
- Custom claims display (for power users)
- Logout button with CSRF token

---

## Database Schema

### Entity Relationship Diagram

```
┌─────────────────────────────┐
│           ROLES             │
├─────────────────────────────┤
│ id     : BIGINT (PK)        │
│ name   : VARCHAR(50) (UK)   │  ◄──────┐
└─────────────────────────────┘         │
                                        │ FK
┌─────────────────────────────┐         │
│      ROLE_ASSIGNMENTS       │         │
├─────────────────────────────┤         │
│ id              : BIGINT (PK)         │
│ user_identifier : VARCHAR(255)        │
│ role_id         : BIGINT (FK) ────────┘
└─────────────────────────────┘
```

### Liquibase Changesets

| File | ID | Description |
|------|----|-------------|
| `001-create-roles-schema.yaml` | `create-roles-table` | Creates ROLES table |
| `001-create-roles-schema.yaml` | `create-role-assignments-table` | Creates ROLE_ASSIGNMENTS table with FK and index |
| `002-seed-roles-data.yaml` | `seed-roles` | Inserts ROLE_USER, ROLE_ADMIN, ROLE_POWER_USER |
| `002-seed-roles-data.yaml` | `seed-role-assignments` | Assigns roles to default users |

---

## Development Guide

### Prerequisites

- Java 23 (or compatible JDK)
- Maven 3.x
- IDE with JTE support (IntelliJ recommended)

### Running the Application

```bash
# Clone and navigate to project
git clone <repository-url>
cd spring-boot-oauth-demo

# Run with Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/jte-login-0.0.1-SNAPSHOT.jar
```

### Running with OAuth2

```bash
# Set environment variables
export GOOGLE_CLIENT_ID=your_google_client_id
export GOOGLE_CLIENT_SECRET=your_google_client_secret
export GITHUB_CLIENT_ID=your_github_client_id
export GITHUB_CLIENT_SECRET=your_github_client_secret

# Run application
mvn spring-boot:run
```

### Accessing H2 Console

1. Navigate to `http://localhost:8080/h2-console`
2. Use these settings:
   - **JDBC URL**: `jdbc:h2:mem:rolesdb`
   - **Username**: `sa`
   - **Password**: (leave empty)
3. Click "Connect"

### Useful SQL Queries

```sql
-- View all roles
SELECT * FROM roles;

-- View all role assignments
SELECT ra.id, ra.user_identifier, r.name as role_name
FROM role_assignments ra
JOIN roles r ON ra.role_id = r.id;

-- Find roles for a specific user
SELECT r.name
FROM role_assignments ra
JOIN roles r ON ra.role_id = r.id
WHERE ra.user_identifier = 'form:admin';

-- Add a new role
INSERT INTO roles (name) VALUES ('ROLE_MODERATOR');

-- Assign role to user
INSERT INTO role_assignments (user_identifier, role_id)
VALUES ('github:newuser@example.com', (SELECT id FROM roles WHERE name = 'ROLE_USER'));

-- Remove role assignment
DELETE FROM role_assignments
WHERE user_identifier = 'form:admin'
AND role_id = (SELECT id FROM roles WHERE name = 'ROLE_USER');
```

---

## Troubleshooting

### OAuth2 Issues

**Problem**: OAuth2 buttons don't appear on login page
- **Cause**: Environment variables not set or empty
- **Solution**: Set `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, etc.

**Problem**: Redirect URI mismatch error
- **Cause**: OAuth provider callback URL doesn't match
- **Solution**: Ensure these exact URLs are configured:
  - Google: `http://localhost:8080/login/oauth2/code/google`
  - GitHub: `http://localhost:8080/login/oauth2/code/github`

### Database Issues

**Problem**: Tables not created
- **Cause**: Liquibase not running
- **Solution**: Check `application.yaml` has correct `change-log` path

**Problem**: User has no roles after login
- **Cause**: User identifier not in `role_assignments` table
- **Solution**: Check the exact identifier format matches (case-sensitive)

### General Issues

**Problem**: Login page styles not loading
- **Cause**: Tailwind CSS CDN blocked
- **Solution**: Check network connectivity, or configure local Tailwind build

**Problem**: CSRF token error
- **Cause**: Missing CSRF token in form
- **Solution**: Ensure `${csrfHiddenInput}` is included in all POST forms

### Debug Logging

Enable detailed security logs:

```yaml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.oauth2: DEBUG
```

---

## License

This project is provided as a demonstration. See LICENSE file for details.

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

---

## Acknowledgments

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [JTE Template Engine](https://jte.gg/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Liquibase](https://www.liquibase.org/)
