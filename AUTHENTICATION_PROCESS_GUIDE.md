# Authentication Process Guide

This document provides a comprehensive walkthrough of the authentication process in the Smart E-Commerce API V1 system.

## 🔐 Authentication Flow Overview

The system uses a **simplified authentication** approach without JWT tokens. Here's the complete flow:

---

## 1. User Registration Process

### Step 1: Frontend Sends Registration Request

**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "emailAddress": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "password": "securePassword123",
  "role": "CUSTOMER"
}
```

### Step 2: Controller Processing

**File:** `AuthController.java` (Lines 56-61)

```java
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@Valid @RequestBody CreateUserRequest request) {
    var created = userService.createUser(request);
    AuthResponse response = AuthResponse.builder()
            .userId(created.getId())
            .role(created.getRole())
            .build();
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

**What happens:**
1. ✅ **Validation:** `@Valid` triggers Bean Validation
2. ✅ **User Creation:** Delegates to `UserService.createUser()`
3. ✅ **Password Hashing:** Password is automatically hashed during user creation
4. ✅ **Response:** Returns `userId` and `role` immediately

**Response (201 Created):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "CUSTOMER"
}
```

---

## 2. User Login Process

### Step 1: Frontend Sends Login Request

**Endpoint:** `POST /api/auth/authenticate`

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

### Step 2: Controller Processing

**File:** `AuthController.java` (Lines 41-48)

```java
@PostMapping("/authenticate")
public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
    AuthResponse response = authService.authenticate(request.getEmail(), request.getPassword());
    if (response == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(response);
}
```

### Step 3: Service Layer Authentication

**File:** `AuthServiceImpl.java` (Lines 26-78)

#### 3.1 Input Validation
```java
if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
    log.debug("Authentication failed: missing email or password");
    return null;
}
```

#### 3.2 Email Normalization
```java
String normalizedEmail = email.trim();
```

#### 3.3 User Lookup
```java
Optional<User> maybeUser = userRepository.findByEmail(normalizedEmail);
if (maybeUser.isEmpty()) {
    log.debug("Authentication failed: user not found for email={}", normalizedEmail);
    return null;
}
```

#### 3.4 Account Status Check
```java
User user = maybeUser.get();
if (user.getIsActive() != null && !user.getIsActive()) {
    log.debug("Authentication failed: user is inactive id={}", user.getId());
    return null;
}
```

#### 3.5 Password Verification
```java
String storedHash = user.getPasswordHash();
if (storedHash == null) {
    log.debug("Authentication failed: no password hash stored for user id={}", user.getId());
    return null;
}

boolean matches = false;
try {
    matches = passwordEncoder.matches(password, storedHash);
} catch (Exception ex) {
    log.error("Error while checking password for user id={}: {}", user.getId(), ex.getMessage());
    return null;
}

if (!matches) {
    log.debug("Authentication failed: invalid credentials for email={}", normalizedEmail);
    return null;
}
```

#### 3.6 Response Generation
```java
String role = user.getRole();
if (role == null) {
    role = "USER"; // fallback default
}

log.info("User authenticated: id={}, email={}", user.getId(), normalizedEmail);

return AuthResponse.builder()
        .userId(user.getId())
        .role(role)
        .build();
```

### Step 4: Response Handling

**Success Response (200 OK):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "CUSTOMER"
}
```

**Failure Response (401 Unauthorized):**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication failed",
  "path": "/api/auth/authenticate"
}
```

---

## 3. Post-Authentication State Management

### Current System Behavior

Since **JWT tokens are disabled** in your system:

1. **No Token Generation:** No JWT or session tokens are created
2. **Client-Side Storage:** Frontend must store `userId` and `role`
3. **Stateless API:** Each request must include user identification manually

### Frontend Implementation Example

```javascript
// After successful login
const login = async (email, password) => {
  const response = await fetch('/api/auth/authenticate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  
  if (response.ok) {
    const { userId, role } = await response.json();
    
    // Store authentication state
    localStorage.setItem('currentUser', JSON.stringify({ userId, role }));
    
    return { userId, role };
  } else {
    throw new Error('Authentication failed');
  }
};

// Using authentication for subsequent requests
const makeAuthenticatedRequest = async (url, options = {}) => {
  const currentUser = JSON.parse(localStorage.getItem('currentUser'));
  
  if (!currentUser) {
    throw new Error('User not authenticated');
  }
  
  // Include userId in request (since no JWT)
  const authOptions = {
    ...options,
    headers: {
      ...options.headers,
      'X-User-ID': currentUser.userId,
      'X-User-Role': currentUser.role
    }
  };
  
  return fetch(url, authOptions);
};
```

---

## 4. Security Features

### Password Security
- ✅ **BCrypt Hashing:** Passwords are hashed using `PasswordEncoder`
- ✅ **No Plain Text:** Passwords never stored in plain text
- ✅ **Salted Hashes:** BCrypt automatically includes salt

### Account Security
- ✅ **Account Status:** Inactive users cannot authenticate
- ✅ **Input Validation:** Email format and password requirements
- ✅ **Error Logging:** Failed attempts are logged for monitoring

### Input Validation
```java
// AuthRequest.java
@Data
public class AuthRequest {
    @Email
    @NotNull(message = "email is required")
    private String email;

    @NotNull(message = "password is required")
    private String password;
}
```

---

## 5. Authentication Flow Diagram

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   AuthController│    │  AuthService    │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │ POST /api/auth/       │                       │
         │ authenticate          │                       │
         ├─────────────────────→│                       │
         │                       │                       │
         │                       │ authService.          │
         │                       │ authenticate()        │
         │                       ├─────────────────────→│
         │                       │                       │
         │                       │                       │ userRepository.
         │                       │                       │ findByEmail()
         │                       │                       ├─────────────────────→
         │                       │                       │
         │                       │                       │ User lookup & 
         │                       │                       │ validation
         │                       │                       │◄─────────────────────
         │                       │                       │
         │                       │                       │ passwordEncoder.
         │                       │                       │ matches()
         │                       │                       ├─────────────────────→
         │                       │                       │
         │                       │                       │ Password verification
         │                       │                       │◄─────────────────────
         │                       │                       │
         │                       │ AuthResponse          │
         │                       │◄─────────────────────│
         │                       │                       │
         │ 200 OK + userId,role │                       │
         │◄─────────────────────│                       │
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   AuthController│    │  AuthService    │
│   Stores        │    │                 │    │                 │
│   userId, role  │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## 6. Error Scenarios

### Common Authentication Failures

| Scenario | Response | Reason |
|----------|----------|--------|
| Missing email/password | 400 Bad Request | Validation failed |
| Invalid email format | 400 Bad Request | @Email validation |
| User not found | 401 Unauthorized | No matching email |
| Wrong password | 401 Unauthorized | Password doesn't match |
| Inactive account | 401 Unauthorized | User deactivated |
| Database error | 500 Internal Server Error | System failure |

### Logging Examples

```java
// Successful authentication
log.info("User authenticated: id={}, email={}", user.getId(), normalizedEmail);

// Failed authentication attempts
log.debug("Authentication failed: user not found for email={}", normalizedEmail);
log.debug("Authentication failed: invalid credentials for email={}", normalizedEmail);
log.debug("Authentication failed: user is inactive id={}", user.getId());
```

---

## 7. Current Limitations & Recommendations

### ⚠️ Current Limitations

1. **No JWT Tokens:** No stateless authentication
2. **No Session Management:** No server-side session
3. **No Token Refresh:** No automatic re-authentication
4. **No Password Reset:** No forgot password functionality
5. **No Account Lockout:** No brute force protection

### 🔧 Recommended Enhancements

1. **Enable JWT Tokens:**
```java
// In AuthServiceImpl
public AuthResponse authenticate(String email, String password) {
    // ... existing validation ...
    
    String token = jwtTokenProvider.generateToken(user);
    
    return AuthResponse.builder()
            .userId(user.getId())
            .role(role)
            .token(token)  // Add token
            .build();
}
```

2. **Add Password Reset:**
```java
@PostMapping("/forgot-password")
public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
    userService.initiatePasswordReset(email);
    return ResponseEntity.ok().build();
}
```

3. **Implement Rate Limiting:**
```java
@RateLimiter(name = "auth", fallbackMethod = "authFallback")
public AuthResponse authenticate(String email, String password) {
    // ... existing logic ...
}
```

---

## 8. Testing the Authentication Process

### Test with curl

```bash
# Test successful login
curl -X POST http://localhost:8080/api/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Test failed login
curl -X POST http://localhost:8080/api/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"wrongpassword"}'

# Test registration
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "emailAddress":"newuser@example.com",
    "firstName":"John",
    "lastName":"Doe",
    "password":"securePassword123",
    "role":"CUSTOMER"
  }'
```

---

## 9. Key Files in Authentication System

### Core Authentication Files

1. **Controller Layer:**
   - `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/auth/controller/AuthController.java`

2. **Service Layer:**
   - `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/auth/service/AuthService.java`
   - `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/auth/service/impl/AuthServiceImpl.java`

3. **DTOs:**
   - `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/auth/dto/AuthRequest.java`
   - `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/auth/dto/AuthResponse.java`

4. **User Management:**
   - `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/user/service/UserService.java`
   - `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/user/repository/UserRepository.java`

5. **Security Configuration:**
   - `src/main/java/com/miracle/smart_ecommerce_api_v1/security/JwtTokenProvider.java` (Currently disabled)
   - `src/main/java/com/miracle/smart_ecommerce_api_v1/security/JwtAuthenticationFilter.java` (Currently disabled)

---

## 10. Authentication Data Flow Summary

### Registration Flow
1. **Frontend** → POST `/api/auth/register` → **AuthController**
2. **AuthController** → `userService.createUser()` → **UserService**
3. **UserService** → Password hashing + User creation → **UserRepository**
4. **UserRepository** → Database save → **User entity**
5. **UserService** → Return created user → **AuthController**
6. **AuthController** → Return `userId` and `role` → **Frontend**

### Login Flow
1. **Frontend** → POST `/api/auth/authenticate` → **AuthController**
2. **AuthController** → `authService.authenticate()` → **AuthService**
3. **AuthService** → User lookup → **UserRepository**
4. **UserRepository** → Return user (if found) → **AuthService**
5. **AuthService** → Password verification → **PasswordEncoder**
6. **PasswordEncoder** → Match result → **AuthService**
7. **AuthService** → Return `AuthResponse` → **AuthController**
8. **AuthController** → Return `userId` and `role` → **Frontend**

---

## Summary

Your authentication system is **functionally complete** with:

✅ **Secure password hashing** with BCrypt  
✅ **Input validation** with Bean Validation  
✅ **Account status checking**  
✅ **Comprehensive error handling**  
✅ **Proper logging** for security monitoring  
✅ **Clean separation of concerns**  

The main difference from typical systems is the **absence of JWT tokens**, making it a **simplified authentication** approach suitable for development or specific use cases.

For production deployment, consider implementing JWT tokens and additional security features like rate limiting and account lockout.
