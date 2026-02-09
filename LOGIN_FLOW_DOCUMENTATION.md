# User Login Flow Documentation

## Overview
The application uses **JWT (JSON Web Token) based authentication** with Spring Security. Users authenticate using their email and password, and receive JWT tokens for subsequent API requests.

---

## Login Process - Step by Step

### 1. **User Sends Login Request**

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "userPassword123"
}
```

**Controller:** `AuthController.login()`
```java
@PostMapping("/login")
public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse authResponse = authService.login(request);
    return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
}
```

---

### 2. **Authentication Service Validates Credentials**

**Service:** `AuthServiceImpl.login()` performs the following steps:

#### Step 2a: Find User by Email
```java
User user = userRepository.findByEmail(request.getEmail())
    .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
```
- Queries database for user with provided email
- If not found, throws `BadCredentialsException`

#### Step 2b: Check if Account is Active
```java
if (!Boolean.TRUE.equals(user.getIsActive())) {
    throw new BadCredentialsException("Account is deactivated. Please contact support.");
}
```
- Verifies the user account is active
- Deactivated accounts cannot log in

#### Step 2c: Verify Password
```java
if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
    log.warn("Invalid password attempt for user: {}", request.getEmail());
    throw new BadCredentialsException("Invalid email or password");
}
```
- Uses BCrypt password encoder to compare plain-text password with hashed password
- The `passwordEncoder.matches()` method securely verifies the password
- If password doesn't match, throws `BadCredentialsException`

---

### 3. **Generate JWT Tokens**

Once authentication succeeds, the system generates two tokens:

#### Access Token (Short-lived)
```java
String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmailAddress());
```
- **Default expiration:** 24 hours (86400000 ms)
- **Purpose:** Used for API authentication
- **Contains:** User ID and email

#### Refresh Token (Long-lived)
```java
String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
```
- **Default expiration:** 7 days (604800000 ms)
- **Purpose:** Used to obtain new access tokens without re-login
- **Contains:** User ID and special "refresh" type claim

**Token Generation Details (`JwtTokenProvider`):**
```java
public String generateToken(UUID userId, String email) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpiration);
    
    return Jwts.builder()
        .subject(userId.toString())
        .claim("email", email)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey())
        .compact();
}
```

---

### 4. **Return Authentication Response**

**Response Structure:**
```json
{
  "status": "success",
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

---

## Using JWT Token for Authenticated Requests

### 1. **Client Stores Token**
After successful login, the client (web/mobile app) stores the `accessToken`.

### 2. **Include Token in Requests**
For subsequent API calls, include the token in the Authorization header:

```http
GET /api/users/me
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### 3. **JWT Authentication Filter Validates Token**

**Filter:** `JwtAuthenticationFilter.doFilterInternal()`

The filter runs on **every request** and:

#### Step 3a: Extract Token from Header
```java
String jwt = getJwtFromRequest(request);
// Extracts token from "Authorization: Bearer <token>" header
```

#### Step 3b: Validate Token
```java
if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
    // Token is valid
}
```

**Token validation checks:**
- Token signature is valid (signed with correct secret key)
- Token is not expired
- Token format is correct

#### Step 3c: Load User and Set Authentication
```java
UUID userId = tokenProvider.getUserIdFromToken(jwt);
User user = userRepository.findById(userId).orElse(null);

if (user != null && Boolean.TRUE.equals(user.getIsActive())) {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            user,
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    SecurityContextHolder.getContext().setAuthentication(authentication);
}
```

- Extracts user ID from token
- Loads full user from database
- Verifies user is still active
- Sets authentication in Spring Security context
- Request proceeds with authenticated user

---

## Token Refresh Flow

When the access token expires, use the refresh token to get a new one:

**Endpoint:** `POST /api/auth/refresh`

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Process:**
1. Validates refresh token (signature, expiration, type)
2. Extracts user ID from token
3. Verifies user still exists and is active
4. Generates new access token and refresh token
5. Returns new tokens in same format as login

---

## Password Security

### Password Hashing (Registration/Update)
```java
String hashedPassword = passwordEncoder.encode(plainTextPassword);
```
- Uses **BCrypt** hashing algorithm
- Automatically generates salt
- Stored as `password_hash` in database

### Password Verification (Login)
```java
boolean matches = passwordEncoder.matches(plainTextPassword, hashedPassword);
```
- Compares plain-text password with BCrypt hash
- BCrypt handles salt extraction and comparison
- **Never** stores or logs plain-text passwords

---

## Security Configuration

**Current Configuration (`SecurityConfig`):**
```java
.authorizeHttpRequests(auth -> auth
    .anyRequest().permitAll()  // Development mode - ALL requests allowed
)
```

⚠️ **Note:** The application is currently in **development mode** with all endpoints publicly accessible. In production, you should:

1. Require authentication for protected endpoints:
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
    .anyRequest().authenticated();
)
```

2. Add role-based access control:
```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.requestMatchers("/api/users/**").hasRole("USER")
```

---

## Complete Login Example

### Successful Login (200 OK)
```bash
curl -X POST http://localhost:9090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123"
  }'
```

**Response:**
```json
{
  "status": "success",
  "message": "Login successful",
  "timestamp": "2026-02-07T10:30:00",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

### Failed Login - Invalid Credentials (401 Unauthorized)
```json
{
  "status": "error",
  "message": "Invalid email or password",
  "timestamp": "2026-02-07T10:30:00",
  "code": 401
}
```

### Failed Login - Deactivated Account (401 Unauthorized)
```json
{
  "status": "error",
  "message": "Account is deactivated. Please contact support.",
  "timestamp": "2026-02-07T10:30:00",
  "code": 401
}
```

---

## Related Endpoints

### 1. **Register New User**
`POST /api/auth/register`
- Creates new user account
- Automatically logs in and returns tokens

### 2. **Get Current User**
`GET /api/auth/me`
- Returns authenticated user's information
- Requires valid access token in Authorization header

### 3. **Logout**
`POST /api/auth/logout`
- Currently stateless (client-side token removal)
- No server-side session invalidation

---

## Configuration Properties

Configure JWT settings in `application.yaml`:

```yaml
jwt:
  secret: mySecretKeyForJwtTokenGenerationThatIsLongEnoughForHS512Algorithm12345
  expiration: 86400000      # 24 hours in milliseconds
  refresh-expiration: 604800000  # 7 days in milliseconds
```

---

## Security Best Practices

1. ✅ **Passwords are hashed** with BCrypt
2. ✅ **JWT tokens expire** (access: 24h, refresh: 7 days)
3. ✅ **Tokens are signed** with HS512 algorithm
4. ✅ **Account status checked** before authentication
5. ⚠️ **All endpoints publicly accessible** (development mode)
6. ⚠️ **No token blacklist** (logout is client-side only)

---

## Troubleshooting

### "Invalid email or password"
- Email not found in database
- Password doesn't match
- Check database for user existence and password hash

### "Account is deactivated"
- User's `is_active` flag is false
- Admin needs to activate account via `PUT /api/users/{id}/activate`

### "Invalid or expired token"
- Token has expired (after 24 hours for access token)
- Use refresh token to get new access token
- Token signature is invalid

### "401 Unauthorized" on authenticated endpoint
- No token provided in Authorization header
- Token format is incorrect (should be "Bearer <token>")
- Token is expired or invalid

