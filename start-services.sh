#!/bin/bash
# Start both Authentication and Authorization services
# macOS/Linux startup script

echo "========================================"
echo "Starting OAuth Demo Services"
echo "========================================"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 23 or later"
    exit 1
fi

echo ""
echo "Starting Authorization Service on port 8081..."
cd authorization-service || exit
./mvnw spring-boot:run &
AUTHZ_PID=$!
cd ..

# Wait for authorization service to start
echo "Waiting for Authorization Service to start..."
sleep 15

echo ""
echo "Starting Authentication Service on port 8080..."
cd authentication-service || exit
./mvnw spring-boot:run &
AUTH_PID=$!
cd ..

echo ""
echo "========================================"
echo "Services are starting..."
echo "========================================"
echo ""
echo "Authorization Service: http://localhost:8081"
echo "  - H2 Console: http://localhost:8081/h2-console"
echo "  - Health: http://localhost:8081/actuator/health"
echo ""
echo "Authentication Service: http://localhost:8080"
echo "  - Home: http://localhost:8080"
echo "  - Login: http://localhost:8080/login"
echo "  - Dashboard: http://localhost:8080/dashboard"
echo "  - H2 Console (proxied): http://localhost:8080/h2-console"
echo ""
echo "Press Ctrl+C to stop all services"

# Trap Ctrl+C and kill both processes
trap "echo 'Stopping services...'; kill $AUTHZ_PID $AUTH_PID 2>/dev/null; exit" INT

# Wait for both processes
wait
