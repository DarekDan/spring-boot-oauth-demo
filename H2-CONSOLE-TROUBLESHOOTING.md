# H2 Console Access Testing Guide

## Prerequisites
Both services must be running:
- Authorization Service on port 8081 ✅
- Authentication Service on port 8080 ✅

## Step-by-Step Access

### 1. Login to Authentication Service
1. Navigate to: http://localhost:8080/login
2. Login with:
   - Username: `admin`
   - Password: `admin123`
3. You should be redirected to the dashboard

### 2. Access H2 Console (Proxied)
1. Navigate to: http://localhost:8080/h2-console
2. If you see a white page, check:
   - Browser console (F12) for JavaScript errors
   - Network tab to see if requests are being made
   - Look for any CORS or CSP errors

### 3. H2 Console Login
If the console loads, use these credentials:
- **JDBC URL:** `jdbc:h2:mem:rolesdb`
- **Username:** `sa`
- **Password:** Check authorization service console for the random password

### 4. Current Authorization Service Password
From the logs:
```
Password: 4uKsRKhhTwHWgCUi
```

## Troubleshooting White Page

### Check 1: Is the Gateway Routing?
Open browser developer tools (F12) and check:
- Network tab: Are requests to `/h2-console` being made?
- Console tab: Any JavaScript errors?

### Check 2: Frame Options
The H2 console uses frames. Check if you see:
```
Refused to display in a frame because it set 'X-Frame-Options' to 'deny'.
```

This should be fixed by our securityconfig (line 63: `.frameOptions(frame -> frame.sameOrigin())`).

### Check 3: Gateway is Working
The debug logs show gateway is configured:
```
Yaml Properties matched Operations name: path, args: {patterns=/h2-console/**}
Yaml Properties matched Operations name: addRequestHeader, args: {values=http, name=X-Forwarded-Proto}
```

### Check 4: Direct Access (For Debugging)
Try accessing the H2 console directly on the authorization service:
http://localhost:8081/h2-console

This bypasses the gateway and tests if the issue is Gateway-specific or H2-specific.

## Expected Behavior

### When Gateway Works Correctly:
1. Navigate to http://localhost:8080/h2-console
2. See the H2 login page with connection fields
3. URL stays on localhost:**8080** (no redirect to 8081)
4. After H2 login, can query tables

### What We've Configured:
- ✅ Security allows frames (`sameOrigin`)
- ✅ CSRF disabled for `/h2-console/**`
- ✅ Gateway route configured for `/h2-console/**`
- ✅ Forward headers added (`X-Forwarded-*`)
- ✅ Role-based access (ADMIN or POWER_USER required)

## If Still Getting White Page

The issue is likely one of:

1. **Gateway Proxy Not Working in Spring Boot 4.0**
   - Spring Cloud Gateway Server WebMVC is new in 2025.x
   - May need additional configuration for complex apps like H2 console

2. **Content Security Policy**
   - H2 console might be setting CSP headers that conflict

3. **WebSocket/AJAX Issues**
   - H2 console uses AJAX for queries
   - Gateway might not be proxying these correctly

## Alternative: Direct Backend Access (Dev Only)
For development/debugging, you can access H2 directly:
- URL: http://localhost:8081/h2-console
- Same credentials as above
- **Warning:** This byp

asses authentication service security
