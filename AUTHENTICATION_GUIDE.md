# Authentication Guide for Frontend

This guide provides comprehensive information about authentication data and endpoints needed by frontend applications.

## Table of Contents
1. [Authentication Overview](#authentication-overview)
2. [Authentication Endpoints](#authentication-endpoints)
3. [Data Structures](#data-structures)
4. [Authentication Flow](#authentication-flow)
5. [Frontend Implementation](#frontend-implementation)
6. [Error Handling](#error-handling)
7. [Security Considerations](#security-considerations)
8. [Testing](#testing)

## Authentication Overview

**Important Note:** This application currently uses a simplified authentication system without JWT tokens. The authentication endpoints return user ID and role, but no token-based authentication is implemented.

### Current Authentication State
- ❌ **JWT Tokens**: Disabled (JWT functionality removed)
- ❌ **Spring Security**: Disabled
- ✅ **Basic Authentication**: Email/password validation
- ✅ **User Roles**: Supported (USER, ADMIN, CUSTOMER)
- ✅ **Account Status**: Active/inactive user validation

### Authentication Methods Available
1. **Login** - Authenticate with email and password
2. **Registration** - Create new user account
3. **Role-based Access** - User roles for authorization

## Authentication Endpoints

### Base URL
```
http://localhost:8080/api/auth
```

### Available Endpoints

| Method | Endpoint | Description | Returns |
|--------|-----------|-------------|----------|
| `POST` | `/api/auth/authenticate` | Login user | User ID and role |
| `POST` | `/api/auth/register` | Register new user | User ID and role |

### 1. User Login

**Endpoint:** `POST /api/auth/authenticate`

**Request:**
```json
{
  "email": "user@example.com",
  "password": "userPassword123"
}
```

**Response (Success - 200 OK):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "CUSTOMER"
}
```

**Response (Error - 401 Unauthorized):**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication failed",
  "path": "/api/auth/authenticate"
}
```

### 2. User Registration

**Endpoint:** `POST /api/auth/register`

**Request:**
```json
{
  "emailAddress": "newuser@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "password": "securePassword123",
  "role": "CUSTOMER"
}
```

**Response (Success - 201 Created):**
```json
{
  "userId": "660e8400-e29b-41d4-a716-446655440000",
  "role": "CUSTOMER"
}
```

**Response (Error - 400 Bad Request):**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Email address is required",
  "path": "/api/auth/register"
}
```

## Data Structures

### AuthRequest (Login)
```typescript
interface AuthRequest {
  email: string;      // Required, valid email format
  password: string;   // Required, any string
}
```

### CreateUserRequest (Registration)
```typescript
interface CreateUserRequest {
  emailAddress: string;    // Required, valid email, max 255 chars
  firstName?: string;      // Optional, max 100 chars
  lastName?: string;       // Optional, max 100 chars
  phoneNumber?: string;    // Optional, max 20 chars
  password: string;       // Required, 8-100 chars
  role?: string;          // Optional, defaults to "CUSTOMER"
}
```

### AuthResponse (Both Login & Registration)
```typescript
interface AuthResponse {
  userId: string;    // UUID of authenticated/created user
  role: string;      // User role (USER, ADMIN, CUSTOMER)
}
```

### Error Response
```typescript
interface ErrorResponse {
  timestamp: string;    // ISO-8601 timestamp
  status: number;      // HTTP status code
  error: string;       // Error type
  message: string;      // Detailed error message
  path: string;        // Request path
}
```

## Authentication Flow

### Current Flow (No Tokens)
```
1. User enters email/password
2. Frontend sends POST to /api/auth/authenticate
3. Backend validates credentials
4. Backend returns userId and role
5. Frontend stores userId and role locally
6. Frontend includes userId in subsequent requests
```

### Recommended Flow (If You Implement Tokens)
```
1. User enters email/password
2. Frontend sends POST to /api/auth/authenticate
3. Backend validates credentials
4. Backend generates JWT token with userId and role
5. Backend returns token, userId, and role
6. Frontend stores token securely
7. Frontend includes token in Authorization header for all requests
```

## Frontend Implementation

### Basic Authentication Service (Current System)

```typescript
class AuthService {
  private baseUrl: string;
  private currentUser: { userId: string; role: string } | null = null;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
    this.loadUserFromStorage();
  }

  // Login user
  async login(email: string, password: string): Promise<{ userId: string; role: string }> {
    const response = await fetch(`${this.baseUrl}/api/auth/authenticate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ email, password })
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Login failed');
    }

    const user = await response.json();
    this.currentUser = user;
    this.storeUser(user);
    return user;
  }

  // Register new user
  async register(userData: CreateUserRequest): Promise<{ userId: string; role: string }> {
    const response = await fetch(`${this.baseUrl}/api/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(userData)
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Registration failed');
    }

    const user = await response.json();
    this.currentUser = user;
    this.storeUser(user);
    return user;
  }

  // Get current user
  getCurrentUser() {
    return this.currentUser;
  }

  // Check if user is authenticated
  isAuthenticated(): boolean {
    return this.currentUser !== null;
  }

  // Check user role
  hasRole(role: string): boolean {
    return this.currentUser?.role === role;
  }

  // Check if user is admin
  isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  // Logout user
  logout() {
    this.currentUser = null;
    localStorage.removeItem('currentUser');
    sessionStorage.removeItem('currentUser');
  }

  // Store user data
  private storeUser(user: { userId: string; role: string }) {
    localStorage.setItem('currentUser', JSON.stringify(user));
    // Alternative: sessionStorage.setItem('currentUser', JSON.stringify(user));
  }

  // Load user from storage
  private loadUserFromStorage() {
    const stored = localStorage.getItem('currentUser');
    if (stored) {
      try {
        this.currentUser = JSON.parse(stored);
      } catch (error) {
        console.error('Failed to parse stored user data:', error);
        this.logout();
      }
    }
  }
}

// Usage
const authService = new AuthService('http://localhost:8080');

// Login
try {
  const user = await authService.login('user@example.com', 'password123');
  console.log('Logged in:', user);
  console.log('Is admin:', authService.isAdmin());
} catch (error) {
  console.error('Login failed:', error);
}

// Register
try {
  const newUser = await authService.register({
    emailAddress: 'newuser@example.com',
    firstName: 'John',
    lastName: 'Doe',
    password: 'securePassword123',
    role: 'CUSTOMER'
  });
  console.log('Registered:', newUser);
} catch (error) {
  console.error('Registration failed:', error);
}

// Check authentication status
if (authService.isAuthenticated()) {
  console.log('User is logged in:', authService.getCurrentUser());
}

// Logout
authService.logout();
```

### React Hook Implementation

```typescript
// hooks/useAuth.ts
import { useState, useEffect, createContext, useContext, ReactNode } from 'react';

interface User {
  userId: string;
  role: string;
}

interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => Promise<void>;
  register: (userData: CreateUserRequest) => Promise<void>;
  logout: () => void;
  isAuthenticated: () => boolean;
  isAdmin: () => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (error) {
        console.error('Failed to parse stored user:', error);
        localStorage.removeItem('currentUser');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    const response = await fetch('/api/auth/authenticate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Login failed');
    }

    const userData = await response.json();
    setUser(userData);
    localStorage.setItem('currentUser', JSON.stringify(userData));
  };

  const register = async (userData: CreateUserRequest) => {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData)
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Registration failed');
    }

    const newUser = await response.json();
    setUser(newUser);
    localStorage.setItem('currentUser', JSON.stringify(newUser));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('currentUser');
  };

  const isAuthenticated = () => user !== null;

  const isAdmin = () => user?.role === 'ADMIN';

  const value: AuthContextType = {
    user,
    login,
    register,
    logout,
    isAuthenticated,
    isAdmin
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
```

### Vue.js Composition API

```typescript
// composables/useAuth.ts
import { ref, computed } from 'vue';

interface User {
  userId: string;
  role: string;
}

const user = ref<User | null>(null);

export function useAuth() {
  const isAuthenticated = computed(() => user.value !== null);
  const isAdmin = computed(() => user.value?.role === 'ADMIN');

  const login = async (email: string, password: string) => {
    const response = await fetch('/api/auth/authenticate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Login failed');
    }

    const userData = await response.json();
    user.value = userData;
    localStorage.setItem('currentUser', JSON.stringify(userData));
  };

  const register = async (userData: CreateUserRequest) => {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData)
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Registration failed');
    }

    const newUser = await response.json();
    user.value = newUser;
    localStorage.setItem('currentUser', JSON.stringify(newUser));
  };

  const logout = () => {
    user.value = null;
    localStorage.removeItem('currentUser');
  };

  const loadUser = () => {
    const stored = localStorage.getItem('currentUser');
    if (stored) {
      try {
        user.value = JSON.parse(stored);
      } catch (error) {
        console.error('Failed to parse stored user:', error);
        localStorage.removeItem('currentUser');
      }
    }
  };

  return {
    user: readonly(user),
    isAuthenticated,
    isAdmin,
    login,
    register,
    logout,
    loadUser
  };
}
```

## Error Handling

### Common Authentication Errors

| Error | Cause | Solution |
|-------|--------|----------|
| `Email is required` | Missing email field | Include email in request |
| `Password is required` | Missing password field | Include password in request |
| `Email should be valid` | Invalid email format | Use valid email format |
| `Password must be between 8 and 100 characters` | Password length invalid | Use password 8-100 chars |
| `Authentication failed` | Invalid credentials | Check email/password |
| `User not found` | Email not registered | Use registered email |
| `User is inactive` | Account disabled | Contact administrator |

### Error Handling Implementation

```typescript
class AuthError extends Error {
  constructor(
    message: string,
    public status?: number,
    public code?: string
  ) {
    super(message);
    this.name = 'AuthError';
  }
}

const handleAuthError = async (response: Response) => {
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    
    switch (response.status) {
      case 400:
        throw new AuthError(error.message || 'Invalid request', 400, 'BAD_REQUEST');
      case 401:
        throw new AuthError(error.message || 'Authentication failed', 401, 'UNAUTHORIZED');
      case 404:
        throw new AuthError(error.message || 'User not found', 404, 'NOT_FOUND');
      case 500:
        throw new AuthError(error.message || 'Server error', 500, 'SERVER_ERROR');
      default:
        throw new AuthError(error.message || `HTTP ${response.status}`, response.status);
    }
  }
  
  return response.json();
};

// Usage in auth service
const login = async (email: string, password: string) => {
  try {
    const response = await fetch('/api/auth/authenticate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    
    return await handleAuthError(response);
  } catch (error) {
    if (error instanceof AuthError) {
      // Handle specific auth errors
      switch (error.code) {
        case 'UNAUTHORIZED':
          throw new Error('Invalid email or password');
        case 'BAD_REQUEST':
          throw new Error('Please check your input');
        default:
          throw error;
      }
    }
    throw error;
  }
};
```

## Security Considerations

### Current Limitations
1. **No Token-Based Auth**: No JWT or session tokens
2. **No Password Reset**: No forgot password functionality
3. **No Account Verification**: No email verification
4. **No Rate Limiting**: No brute force protection
5. **Client-Side Storage**: User data stored in localStorage

### Recommendations for Production

#### 1. Implement JWT Tokens
```java
// Backend: Add JWT token generation
@Service
public class JwtTokenService {
    public String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getId().toString())
            .claim("role", user.getRole())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SIGNATURE_ALGORITHM, SECRET_KEY)
            .compact();
    }
}
```

#### 2. Add Token Validation
```java
// Backend: Add JWT filter
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) {
        String token = extractToken(request);
        if (token != null && validateToken(token)) {
            // Set authentication in security context
        }
        filterChain.doFilter(request, response);
    }
}
```

#### 3. Frontend Token Management
```typescript
class TokenAuthService {
  private token: string | null = null;

  async login(email: string, password: string) {
    const response = await fetch('/api/auth/authenticate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    const { token, userId, role } = await response.json();
    this.token = token;
    localStorage.setItem('authToken', token);
    localStorage.setItem('currentUser', JSON.stringify({ userId, role }));
  }

  getAuthHeaders() {
    return this.token ? { 'Authorization': `Bearer ${this.token}` } : {};
  }

  async refreshToken() {
    // Implement token refresh logic
  }
}
```

#### 4. Secure Storage
```typescript
// Use httpOnly cookies for tokens
// Implement secure storage mechanisms
// Consider using secure storage libraries

// Example with secure cookie storage
const setSecureCookie = (name: string, value: string, days: number) => {
  const expires = new Date();
  expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
  
  document.cookie = `${name}=${value}; expires=${expires.toUTCString()}; path=/; secure; HttpOnly; SameSite=Strict`;
};
```

## Testing

### Manual Testing with curl

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

# Test validation errors
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"emailAddress":"invalid-email","password":"123"}'
```

### Automated Testing with Jest

```typescript
// auth.test.ts
describe('Authentication API', () => {
  const baseUrl = 'http://localhost:8080/api/auth';

  test('should login with valid credentials', async () => {
    const response = await fetch(`${baseUrl}/authenticate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: 'test@example.com',
        password: 'testpassword123'
      })
    });

    expect(response.ok).toBe(true);
    
    const data = await response.json();
    expect(data).toHaveProperty('userId');
    expect(data).toHaveProperty('role');
    expect(typeof data.userId).toBe('string');
    expect(typeof data.role).toBe('string');
  });

  test('should reject invalid credentials', async () => {
    const response = await fetch(`${baseUrl}/authenticate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: 'test@example.com',
        password: 'wrongpassword'
      })
    });

    expect(response.status).toBe(401);
  });

  test('should register new user', async () => {
    const response = await fetch(`${baseUrl}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        emailAddress: 'newuser@example.com',
        firstName: 'Test',
        lastName: 'User',
        password: 'newpassword123',
        role: 'CUSTOMER'
      })
    });

    expect(response.status).toBe(201);
    
    const data = await response.json();
    expect(data).toHaveProperty('userId');
    expect(data).toHaveProperty('role');
    expect(data.role).toBe('CUSTOMER');
  });

  test('should validate registration data', async () => {
    const response = await fetch(`${baseUrl}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        emailAddress: 'invalid-email',
        password: '123' // Too short
      })
    });

    expect(response.status).toBe(400);
    
    const error = await response.json();
    expect(error).toHaveProperty('message');
  });
});
```

## Integration with Other APIs

### Using User ID in Cart Operations

```typescript
// After authentication, use userId for cart operations
const cartService = new CartService(baseUrl);

const addToCart = async (productId: string, quantity: number) => {
  const currentUser = authService.getCurrentUser();
  if (!currentUser) {
    throw new Error('User not authenticated');
  }

  return cartService.addToCart(currentUser.userId, productId, quantity);
};
```

### Using User Role for Authorization

```typescript
// Role-based access control
const canAccessAdminPanel = (): boolean => {
  return authService.isAdmin();
};

const requireAuth = (): void => {
  if (!authService.isAuthenticated()) {
    throw new Error('Authentication required');
  }
};

// In API calls
const getUserProfile = async (userId: string) => {
  requireAuth();
  
  // Only allow users to view their own profile unless admin
  const currentUser = authService.getCurrentUser();
  if (!authService.isAdmin() && currentUser.userId !== userId) {
    throw new Error('Access denied');
  }
  
  // Proceed with API call
};
```

## Summary

### Current Authentication System
- ✅ **Simple email/password authentication**
- ✅ **User registration with validation**
- ✅ **Role-based access control**
- ✅ **Account status validation**
- ❌ **JWT tokens (disabled)**
- ❌ **Session management**
- ❌ **Password reset**
- ❌ **Email verification**

### Required Frontend Data
1. **Login**: `email` (string), `password` (string)
2. **Registration**: `emailAddress`, `firstName`, `lastName`, `phoneNumber`, `password`, `role`
3. **Response**: `userId` (UUID), `role` (string)

### Recommended Implementation Steps
1. Implement authentication service with current endpoints
2. Store user data securely (localStorage/sessionStorage)
3. Include user ID in subsequent API calls
4. Implement role-based access control
5. Add proper error handling and validation
6. Consider implementing JWT tokens for production

### Important Notes
- **No JWT tokens** are currently generated or validated
- **User ID and role** are returned for client-side storage
- **Authentication state** must be managed by frontend
- **Authorization headers** are not currently required (but recommended for future)
- **Account security** relies on client-side storage (consider upgrading for production)

This guide provides all necessary information for implementing authentication with the current system while noting limitations and recommendations for production deployment.
