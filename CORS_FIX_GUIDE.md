# CORS and 403 Error Fix Guide

## Problem Analysis

You were getting a **403 Forbidden** error with this request:
```
POST http://localhost:3002/api/auth/register 403 (Forbidden)
```

## Root Cause

The frontend is sending the request to **`http://localhost:3002`** (its own origin) instead of **`http://localhost:8080`** (the backend API server).

---

## Backend Fixes Applied

### 1. Updated CORS Configuration (`ApiCorsConfig.java`)

**What was wrong:**
- Used wildcard `*` for `allowedOrigins` with `allowCredentials(false)`
- This prevents proper CORS handling for authenticated requests

**What I fixed:**
```java
// BEFORE
.allowedOrigins("*")
.allowCredentials(false)

// AFTER
.allowedOrigins(
    "http://localhost:3000",
    "http://localhost:3001",
    "http://localhost:3002",  // Added your frontend
    "http://localhost:4200",
    "http://localhost:5173",
    "http://localhost:8080"
)
.allowCredentials(true)
.exposedHeaders("Content-Disposition", "Authorization", "X-Correlation-Id")
```

**Why this matters:**
- When `allowCredentials` is `true` (needed for JWT tokens), you CANNOT use `*` as origin
- Must specify exact origins that are allowed
- Added `localhost:3002` to the allowed origins list

### 2. Updated Security Configuration (`SecurityConfig.java`)

**What I fixed:**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configure(http)) // ✅ Enable CORS
        .csrf(csrf -> csrf.disable()) // ✅ Disable CSRF for REST API
        .authorizeHttpRequests(auth -> auth
            // ✅ Explicitly allow auth endpoints
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/", "/health", "/actuator/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/graphql", "/graphiql").permitAll()
            .requestMatchers("/error").permitAll()
            .anyRequest().permitAll() // For development
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

**Key changes:**
- Added `.cors(cors -> cors.configure(http))` to enable CORS
- Explicitly permitted `/api/auth/**` endpoints (register, login, etc.)
- Added request matchers for other public endpoints

---

## Frontend Fix Required ⚠️

### The Main Problem

Your frontend is making requests to the wrong URL!

**Current (WRONG):**
```javascript
POST http://localhost:3002/api/auth/register
```

**Should be:**
```javascript
POST http://localhost:8080/api/auth/register
```

### Fix Your Frontend Configuration

#### Option 1: Update API Base URL (Recommended)

In your `auth.service.js` (or wherever you configure API calls):

**BEFORE:**
```javascript
const API_URL = '/api/auth/'; // ❌ Relative URL
```

**AFTER:**
```javascript
const API_URL = 'http://localhost:8080/api/auth/'; // ✅ Full URL to backend
```

#### Option 2: Use Environment Variables

Create a `.env` file in your frontend project:
```env
REACT_APP_API_BASE_URL=http://localhost:8080
# or for Vue/Vite
VITE_API_BASE_URL=http://localhost:8080
```

Then in your service:
```javascript
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
const API_URL = `${API_BASE_URL}/api/auth/`;
```

#### Option 3: Use Axios with Base URL

If using Axios:
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true, // Important for CORS with credentials
  headers: {
    'Content-Type': 'application/json'
  }
});

// Usage
api.post('/api/auth/register', userData)
  .then(response => console.log(response.data))
  .catch(error => console.error(error));
```

#### Option 4: Use Fetch with Full URL

```javascript
fetch('http://localhost:8080/api/auth/register', {
  method: 'POST',
  credentials: 'include', // Important for CORS with credentials
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    emailAddress: 'user@example.com',
    firstName: 'John',
    lastName: 'Doe',
    password: 'password123'
  })
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error(error));
```

---

## Complete Example - Frontend API Service

### auth.service.js (Fixed)

```javascript
const API_BASE_URL = 'http://localhost:8080';

export const authService = {
  async register(userData) {
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
        method: 'POST',
        credentials: 'include', // Important!
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Registration failed');
      }

      const data = await response.json();
      
      // Save JWT token
      if (data.data && data.data.accessToken) {
        localStorage.setItem('accessToken', data.data.accessToken);
        localStorage.setItem('refreshToken', data.data.refreshToken);
      }

      return data;
    } catch (error) {
      console.error('Registration error:', error);
      throw error;
    }
  },

  async login(credentials) {
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Login failed');
      }

      const data = await response.json();
      
      // Save JWT token
      if (data.data && data.data.accessToken) {
        localStorage.setItem('accessToken', data.data.accessToken);
        localStorage.setItem('refreshToken', data.data.refreshToken);
      }

      return data;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  },

  logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  },

  getToken() {
    return localStorage.getItem('accessToken');
  }
};
```

### Using Authentication Token in Requests

For authenticated endpoints, include the JWT token:

```javascript
const token = localStorage.getItem('accessToken');

fetch('http://localhost:8080/api/products', {
  method: 'GET',
  credentials: 'include',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}` // ✅ Include JWT token
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

---

## Verification Steps

### 1. Check Backend is Running
```bash
# Backend should be running on port 8080
curl http://localhost:8080/
```

Expected response:
```json
{
  "status": true,
  "message": "Welcome to Smart E-Commerce API",
  "data": {
    "name": "Smart E-Commerce API",
    "version": "1.0.0"
  }
}
```

### 2. Test CORS Headers
```bash
# Test preflight request
curl -X OPTIONS http://localhost:8080/api/auth/register \
  -H "Origin: http://localhost:3002" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v
```

Expected headers in response:
```
Access-Control-Allow-Origin: http://localhost:3002
Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
Access-Control-Allow-Credentials: true
```

### 3. Test Registration Endpoint
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:3002" \
  -d '{
    "emailAddress": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "password": "password123"
  }' \
  -v
```

Expected response (201 Created):
```json
{
  "status": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "...",
      "emailAddress": "test@example.com",
      "firstName": "Test",
      "lastName": "User"
    }
  },
  "statusCode": 201
}
```

---

## Troubleshooting

### Still Getting 403?

1. **Clear browser cache and cookies**
   - Browser might be caching old CORS preflight responses

2. **Check browser console for CORS errors**
   - Look for messages about `Access-Control-Allow-Origin`

3. **Verify backend is running on port 8080**
   ```bash
   netstat -ano | findstr :8080
   ```

4. **Check if firewall is blocking requests**
   - Temporarily disable firewall to test

5. **Try in Incognito/Private browsing mode**
   - Eliminates browser extension interference

### Still Getting CORS Error?

1. **Add more detailed logging to backend**
   - Check `application-dev.yaml` and set:
     ```yaml
     logging:
       level:
         org.springframework.web.cors: DEBUG
         org.springframework.security: DEBUG
     ```

2. **Restart backend after changes**
   ```bash
   mvn spring-boot:run
   ```

3. **Check CORS configuration is loaded**
   - Look for log lines showing CORS mappings on startup

---

## Summary of Changes

### Backend Changes Made ✅

1. **ApiCorsConfig.java**
   - Changed from wildcard `*` to specific origins
   - Added `http://localhost:3002` to allowed origins
   - Changed `allowCredentials(false)` to `allowCredentials(true)`
   - Added `Authorization` to exposed headers

2. **SecurityConfig.java**
   - Added `.cors(cors -> cors.configure(http))` to enable CORS
   - Explicitly permitted `/api/auth/**` endpoints
   - Added request matchers for public endpoints

### Frontend Changes Needed ⚠️

1. **Update API_BASE_URL**
   - Change from relative URLs to `http://localhost:8080`

2. **Add credentials: 'include'**
   - Required for CORS with credentials

3. **Add Authorization header**
   - Include JWT token in authenticated requests

---

## Production Considerations

For production deployment:

1. **Update allowed origins** in `ApiCorsConfig.java`:
   ```java
   .allowedOrigins(
       "https://yourdomain.com",
       "https://www.yourdomain.com"
   )
   ```

2. **Change security to require authentication**:
   ```java
   .anyRequest().authenticated() // Change from permitAll()
   ```

3. **Use environment variables**:
   ```java
   @Value("${cors.allowed-origins}")
   private String[] allowedOrigins;
   ```

4. **Enable HTTPS only**:
   ```yaml
   server:
     ssl:
       enabled: true
   ```

---

**Last Updated:** February 8, 2026  
**Backend Server:** http://localhost:8080  
**Frontend Server:** http://localhost:3002  
**Status:** CORS Fixed ✅ | Frontend URL Fix Required ⚠️

