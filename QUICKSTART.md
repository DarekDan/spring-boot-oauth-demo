# Quick Start Guide

## Prerequisites
- Java 23 or later
- Maven (included via wrapper)
- (Optional) Docker and Docker Compose

## Starting the Services

### Windows
```cmd
start-services.bat
```

### macOS/Linux
```bash
chmod +x start-services.sh
./start-services.sh
```

### Individual Services

**Authorization Service (Backend):**
```bash
cd authorization-service
./mvnw spring-boot:run
```
Wait for startup, note the H2 SA password in console.

**Authentication Service (Frontend):**
```bash
cd authentication-service
./mvnw spring-boot:run
```

### Using Docker

**Both services:**
```bash
cd authentication-service
docker-compose up --build
```

## Accessing the Application

1. **Home Page (Anonymous):** http://localhost:8080
2. **Login Page:** http://localhost:8080/login
   - Form Login: `admin` / `admin123`
   - OAuth2: Click Google or GitHub (if configured)
3. **Dashboard (After Login):** http://localhost:8080/dashboard
4. **H2 Console (ADMIN/POWER_USER):** http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:rolesdb`
   - Username: `sa`
   - Password: Check authorization-service console output

## Adding OAuth2 Providers

Create `.env` file (copy from `.env.example`):
```bash
cp .env.example .env
```

Edit `.env` and add your credentials:
```env
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-secret

GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-secret
```

Restart the services.

## Stopping the Services

### Scripts
- **Windows:** Close the command windows
- **macOS/Linux:** Press `Ctrl+C`

### Docker
```bash
docker-compose down
```

## Troubleshooting

**Port conflicts:**
```bash
# Check if ports are in use
netstat -ano | findstr :8080
netstat -ano | findstr :8081
```

**Build failures:**
```bash
# Clean and rebuild
cd authorization-service
./mvnw clean install

cd ../authentication-service
./mvnw clean install
```

**Can't connect to authorization service:**
- Ensure authorization service is running on port 8081
- Check `application.yaml` in authentication-service
- Verify no firewall blocking localhost connections

## Next Steps

- Read [README-MICROSERVICES.md](README-MICROSERVICES.md) for detailed documentation
- Read [REFACTORING-SUMMARY.md](REFACTORING-SUMMARY.md) for technical details
- Configure OAuth2 providers for Google/GitHub login
- Add new users in authorization service database
