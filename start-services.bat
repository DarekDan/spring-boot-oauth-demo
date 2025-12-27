@echo off
REM Start both Authentication and Authorization services
REM Windows startup script

echo ========================================
echo Starting OAuth Demo Services
echo ========================================

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 23 or later
    pause
    exit /b 1
)

echo.
echo Starting Authorization Service on port 8081...
cd authorization-service
start "Authorization Service" cmd /k "mvn spring-boot:run"
cd ..

REM Wait a bit for authorization service to start
timeout /t 10 /nobreak >nul

echo.
echo Starting Authentication Service on port 8080...
cd authentication-service
start "Authentication Service" cmd /k "mvn spring-boot:run"
cd ..

echo.
echo ========================================
echo Services are starting...
echo ========================================
echo.
echo Authorization Service: http://localhost:8081
echo   - H2 Console: http://localhost:8081/h2-console
echo   - Health: http://localhost:8081/actuator/health
echo.
echo Authentication Service: http://localhost:8080
echo   - Home: http://localhost:8080
echo   - Login: http://localhost:8080/login
echo   - Dashboard: http://localhost:8080/dashboard
echo   - H2 Console (proxied): http://localhost:8080/h2-console
echo.
echo Press any key to exit this window...
pause >nul
