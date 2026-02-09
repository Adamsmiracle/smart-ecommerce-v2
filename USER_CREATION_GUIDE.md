# User Creation - Quick Reference

## When to Use Which Service?

### 🟢 **AuthService.register()** - For User Self-Registration
**Endpoint:** `POST /api/auth/register`

**Use when:**
- Users are creating their own accounts
- User needs to be logged in immediately after registration
- Public registration flow (sign-up page)

**What it does:**
1. Validates email doesn't exist
2. Hashes password with BCrypt
3. Creates user in database
4. Generates JWT access token (24h)
5. Generates JWT refresh token (7 days)
6. Returns tokens + user info

**Request:**
```json
{
  "emailAddress": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "password": "SecurePassword123"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "User registered successfully",
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

✅ **Best for:** Sign-up forms, user self-service registration

---

### 🔵 **UserService.createUser()** - For Admin User Management
**Endpoint:** `POST /api/users`

**Use when:**
- Admin is creating user accounts for others
- Batch user creation/imports
- User doesn't need immediate login (admin creates, user logs in later)
- Creating service accounts or test users

**What it does:**
1. Validates email doesn't exist
2. Hashes password with BCrypt
3. Creates user in database
4. Returns user info (no tokens)

**Request:**
```json
{
  "emailAddress": "employee@company.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "phoneNumber": "+9876543210",
  "password": "TempPassword123"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "User created successfully",
  "data": {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "emailAddress": "employee@company.com",
    "firstName": "Jane",
    "lastName": "Smith",
    "fullName": "Jane Smith",
    "phoneNumber": "+9876543210",
    "isActive": true,
    "createdAt": "2026-02-07T10:30:00",
    "updatedAt": "2026-02-07T10:30:00"
  }
}
```

✅ **Best for:** Admin dashboards, user management, bulk imports

---

## Summary

| Aspect | AuthService.register() | UserService.createUser() |
|--------|----------------------|--------------------------|
| **Endpoint** | `/api/auth/register` | `/api/users` |
| **Who creates** | User (self) | Admin |
| **Returns tokens** | ✅ Yes | ❌ No |
| **Immediate login** | ✅ Yes | ❌ No (requires separate login) |
| **Use case** | Sign-up form | Admin panel |
| **Password hashing** | ✅ BCrypt | ✅ BCrypt |

---

## Common Use Cases

### ✅ User Signs Up on Website
```java
// Use AuthService.register()
POST /api/auth/register
// User gets tokens, can immediately use app
```

### ✅ Admin Creates Employee Account
```java
// Use UserService.createUser()
POST /api/users
// Admin creates account, employee logs in later with their credentials
```

### ✅ Mobile App Registration
```java
// Use AuthService.register()
POST /api/auth/register
// User signs up and app stores tokens for authenticated requests
```

### ✅ Import Users from CSV (Admin)
```java
// Use UserService.createUser() in loop
POST /api/users (for each user)
// Bulk create, users login individually later
```

---

## Recommendation

**For 99% of user registration scenarios, use `AuthService.register()`**

It provides a better user experience by:
- Eliminating the extra login step
- Returning tokens immediately
- Following modern authentication best practices

Only use `UserService.createUser()` when you specifically need admin-controlled account creation without automatic authentication.

---

## Security Note

Both methods:
✅ Hash passwords with BCrypt  
✅ Validate email uniqueness  
✅ Set account as active by default  
✅ Never return plain-text passwords  

The main difference is **token generation** - `AuthService.register()` includes it, `UserService.createUser()` doesn't.

