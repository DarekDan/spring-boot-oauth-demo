# Spring Boot OAuth Demo - Microservices Architecture

This project has been refactored into two independent microservices:

1. **Authentication Service** (Frontend) - Handles user authentication via OAuth2 and form login
2. **Authorization Service** (Backend) - Manages user roles and scopes with H2 database

## Architecture Overview

```
┌─────────────────────────────────────────────────┐
│         Authentication Service (8080)           │
│  ┌──────────────────────────────────────────┐  │
│  │  • OAuth2 Login (Google, GitHub)         │  │
│  │  • Form Login                            │  │
│  │  • Spring Cloud Gateway                  │  │
│  │  • JTE Templates                         │  │
│  │  • Proxies H2 Console                    │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
                      │
                      │ REST API
                      ▼
┌─────────────────────────────────────────────────┐
│         Authorization Service (8081)            │
│  ┌──────────────────────────────────────────┐  │
│  │  • H2 In-Memory Database                 │  │
│  │  • Roles & Scopes Management             │  │
│  │  • REST API (/api/authorization/...)     │  │
│  │  • Liquibase Migrations                  │  │
│  │  • Not directly accessible               │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

## Services

### Authentication Service (Port 8080)

**Purpose:** Public-facing service that handles user authentication and provides the UI.

**Features:**
- OAuth2 login with Google and GitHub
- Form-based login (username: admin, password: admin123)
- Home page accessible anonymously
- Authenticated pages require login
- Spring Cloud Gateway to proxy H2 console from backend
- JTE (Java Template Engine) for views

**Endpoints:**
- `/` - Home page (anonymous)
- `/login` - Login page (anonymous)
- `/dashboard` - User dashboard (authenticated)
- `/h2-console/**` - Proxied H2 console (ADMIN or POWER_USER role required)

### Authorization Service (Port 8081)

**Purpose:** Internal service that manages user roles and authorization data.

**Features:**
- H2 in-memory database with roles and role assignments
- Liquibase for database migrations
- REST API for role queries
- H2 Console for database management
- Admin user with random password (shown on startup)
- Read-only user: `readonly`/`readonly`

**API Endpoints:**
- `/api/authorization/roles/{userIdentifier}` - Get roles for a user
- `/api/authorization/health` - Health check
- `/h2-console` - Direct H2 console access (shown on startup)

**Database Schema:**
- `roles` - Available roles (ROLE_USER, ROLE_ADMIN, ROLE_POWER_USER)
- `role_assignments` - User-to-role mappings

## Quick Start

### Option 1: Using Startup Scripts

**Windows:**
```cmd
start-services.bat
```

**macOS/Linux:**
```bash
./start-services.sh
```

This will:
1. Start the Authorization Service on port 8081
2. Wait for it to initialize
3. Start the Authentication Service on port 8080
4. Display service URLs and credentials

### Option 2: Manual Start

**Terminal 1 - Start Authorization Service:**
```bash
cd authorization-service
./mvnw spring-boot:run
```

**Terminal 2 - Start Authentication Service:**
```bash
cd authentication-service
./mvnw spring-boot:run
```

### Option 3: Docker Compose

**Authorization Service only:**
```bash
cd authorization-service
docker-compose up --build
```

**Both services:**
```bash
cd authentication-service
docker-compose up --build
```

This starts both services with proper dependency management.

## Configuration

### OAuth2 Providers (Optional)

Set environment variables to enable OAuth2 login:

**Google:**
```bash
export GOOGLE_CLIENT_ID=your-client-id
export GOOGLE_CLIENT_SECRET=your-client-secret
```

**GitHub:**
```bash
export GITHUB_CLIENT_ID=your-client-id
export GITHUB_CLIENT_SECRET=your-client-secret
```

Without these, only form login will be available.

### Authorization Service URL

The authentication service connects to the authorization service via:
```yaml
authorization:
  service:
    url: http://localhost:8081
```

For Docker deployments, this is automatically set to `http://authorization-service:8081`.

## User Credentials

### Form Login
- **Username:** admin
- **Password:** admin123
- **Roles:** ROLE_ADMIN (defined in database)

### H2 Database Console

**Direct Access (Authorization Service - localhost:8081/h2-console):**
- **JDBC URL:** jdbc:h2:mem:rolesdb
- **Username:** sa
- **Password:** (randomly generated, shown in console output)

**Alternative (read-only):**
- **Username:** readonly
- **Password:** readonly

**Proxied Access (Authentication Service - localhost:8080/h2-console):**
Same credentials as above, but accessed through the authentication service (requires ADMIN or POWER_USER role).

## Database Schema

### Roles Table
```sql
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL
);
```

### Role Assignments Table
```sql
CREATE TABLE role_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_identifier VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### User Identifiers Format
- Form users: `form:username` (e.g., `form:admin`)
- OAuth2 users: `provider:email` (e.g., `google:user@gmail.com`)

## Adding New Users

To add role assignments for new users, create a new Liquibase changeset in `authorization-service/src/main/resources/db/changelog/`:

```yaml
databaseChangeLog:
  - changeSet:
      id: add-new-user-role
      author: your-name
      changes:
        - insert:
            tableName: role_assignments
            columns:
              - column:
                  name: user_identifier
                  value: "google:newuser@gmail.com"
              - column:
                  name: role_id
                  valueComputed: "(SELECT id FROM roles WHERE name = 'ROLE_USER')"
```

## Development

### Building

**Authorization Service:**
```bash
cd authorization-service
./mvnw clean package
```

**Authentication Service:**
```bash
cd authentication-service
./mvnw clean package
```

### Testing

Each service has its own test suite:
```bash
./mvnw test
```

## Production Deployment

1. Build both services
2. Deploy authorization service first (internal, not publicly accessible)
3. Configure authentication service to point to authorization service
4. Deploy authentication service (public-facing)
5. Set up proper network security:
   - Authorization service should NOT be accessible from the internet
   - Only authentication service should be publicly accessible
   - Use internal networking for service-to-service communication

## Architecture Decisions

### Why Two Services?

1. **Separation of Concerns:**
   - Authentication logic is separate from authorization data
   - Can scale services independently
   - Easier to maintain and test

2. **Security:**
   - Authorization database is not directly exposed
   - H2 console access controlled through authentication service
   - Backend service can be in a private network

3. **Flexibility:**
   - Can replace H2 with a production database easily
   - Can add multiple frontend services using the same authorization backend
   - Can implement caching strategies independently

### Spring Cloud Gateway

Used in authentication service to proxy `/h2-console` from the authorization service:
- Provides secure access to database console
- Maintains role-based access control
- Single entry point for users

## Technology Stack

### Authentication Service
- Spring Boot 3.5.9
- Spring Security with OAuth2 Client
- Spring Cloud Gateway MVC
- JTE (Java Template Engine)
- Spring WebFlux (for gateway)

### Authorization Service
- Spring Boot 3.5.9
- Spring Data JPA
- H2 Database
- Liquibase
- Spring Boot Actuator

## Monitoring

Both services expose actuator endpoints:

**Health checks:**
- http://localhost:8080/actuator/health (Authentication)
- http://localhost:8081/actuator/health (Authorization)

## Troubleshooting

### Authorization Service Won't Start
- Check if port 8081 is already in use
- Verify Java 23 is installed
- Check console for Liquibase migration errors

### Authentication Service Can't Connect to Authorization Service
- Ensure authorization service is running on port 8081
- Check `authorization.service.url` in application.yaml
- Verify services can communicate (firewall, network)

### OAuth2 Login Not Available
- Verify environment variables are set correctly
- Check console logs for OAuth2 configuration warnings
- Ensure redirect URIs match your OAuth2 app settings

### H2 Console Access Denied
- Verify you're logged in with ADMIN or POWER_USER role
- Check the random password in the console output
- Ensure JDBC URL is exactly: `jdbc:h2:mem:rolesdb`

## Contributing

When contributing to either service:
1. Maintain backward compatibility in the REST API
2. Add Liquibase migrations for database changes
3. Update tests for new features
4. Document API changes in this README

## License

[Add your license here]
