# SimpleAuthFilter Testing Guide

This guide demonstrates how to test and verify that the `SimpleAuthFilter` is now properly applied and working.

## 🚀 What Was Applied

### 1. Filter Registration
Created `FilterConfig.java` to register the `SimpleAuthFilter`:
```java
@Bean
public FilterRegistrationBean<SimpleAuthFilter> simpleAuthFilterRegistration(SimpleAuthFilter simpleAuthFilter) {
    FilterRegistrationBean<SimpleAuthFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(simpleAuthFilter);
    registration.addUrlPatterns("/api/*");  // Applied to all API endpoints
    registration.setOrder(1);  // High priority
    registration.setName("simpleAuthFilter");
    registration.setEnabled(true);
    return registration;
}
```

### 2. Enhanced Filter Logic
Updated `SimpleAuthFilter.java` with:
- ✅ **Debug logging** to track filter execution
- ✅ **Request URI logging** for context
- ✅ **User validation** with proper error handling
- ✅ **MDC cleanup** to prevent context leakage

## 🧪 Testing the Filter

### Test 1: Verify Filter is Active

**Start the application** and check logs for filter registration:
```bash
mvn spring-boot:run
```

**Look for these log entries:**
```
SimpleAuthFilter processing request: /api/products with X-User-Id: null
No X-User-Id header found in request: /api/products
MDC context cleared for request: /api/products
```

### Test 2: Test with X-User-Id Header

**Using curl:**
```bash
# Test with valid user ID (replace with actual user ID from your database)
curl -X GET http://localhost:8080/api/products \
  -H "X-User-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "Content-Type: application/json"
```

**Expected logs:**
```
SimpleAuthFilter processing request: /api/products with X-User-Id: 550e8400-e29b-41d4-a716-446655440000
User context added to MDC: userId=550e8400-e29b-41d4-a716-446655440000, role=CUSTOMER
MDC context cleared for request: /api/products
```

### Test 3: Test with Invalid User ID

**Using curl:**
```bash
curl -X GET http://localhost:8080/api/products \
  -H "X-User-Id: invalid-uuid" \
  -H "Content-Type: application/json"
```

**Expected logs:**
```
SimpleAuthFilter processing request: /api/products with X-User-Id: invalid-uuid
X-User-Id header contained invalid UUID: invalid-uuid
MDC context cleared for request: /api/products
```

### Test 4: Test with Non-existent User ID

**Using curl:**
```bash
curl -X GET http://localhost:8080/api/products \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000000" \
  -H "Content-Type: application/json"
```

**Expected logs:**
```
SimpleAuthFilter processing request: /api/products with X-User-Id: 00000000-0000-0000-0000-000000000000
X-User-Id header contained unknown userId: 00000000-0000-0000-0000-000000000000
MDC context cleared for request: /api/products
```

## 🔍 Debug Logging Configuration

To see the filter logs, ensure debug logging is enabled:

### In application-dev.yaml:
```yaml
logging:
  level:
    com.miracle.smart_ecommerce_api_v1.domain.auth.filter.SimpleAuthFilter: DEBUG
    root: INFO
```

### Or via command line:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.miracle.smart_ecommerce_api_v1.domain.auth.filter.SimpleAuthFilter=DEBUG"
```

## 📊 Filter Behavior Analysis

### What the Filter Does:

1. **Intercepts all `/api/*` requests**
2. **Reads `X-User-Id` header** (if present)
3. **Validates UUID format**
4. **Looks up user in database**
5. **Sets MDC context** for logging:
   - `userId` - User's UUID
   - `userRole` - User's role
6. **Cleans up MDC** after request

### Security Considerations:

⚠️ **Important Security Notes:**
- **This is NOT a security filter** - it only provides logging context
- **Anyone can send any `X-User-Id` header**
- **No authentication/authorization** is performed
- **Trusts client-provided user ID** (not secure for production)

### Production Recommendations:

1. **Replace with proper JWT authentication**
2. **Add real security filters**
3. **Implement proper authorization**
4. **Remove trust in client headers**

## 🎯 Integration with Existing Authentication

### Current Flow:
```
Frontend → POST /api/auth/authenticate → Returns userId + role
Frontend stores userId + role in localStorage
Frontend includes X-User-Id header in subsequent requests
SimpleAuthFilter reads X-User-Id and sets MDC context
```

### Example Frontend Implementation:
```javascript
// After authentication
const { userId, role } = await authenticate(email, password);
localStorage.setItem('currentUser', JSON.stringify({ userId, role }));

// Subsequent requests
const makeAuthenticatedRequest = async (url, options = {}) => {
  const { userId } = JSON.parse(localStorage.getItem('currentUser') || '{}');
  
  return fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'X-User-Id': userId,  // Filter will read this
      'Content-Type': 'application/json'
    }
  });
};

// Usage
const products = await makeAuthenticatedRequest('/api/products');
```

## 🐛 Troubleshooting

### Filter Not Working?

1. **Check Filter Registration:**
   - Look for "simpleAuthFilter" in startup logs
   - Verify `FilterConfig.java` is being loaded

2. **Check URL Pattern:**
   - Filter only applies to `/api/*` paths
   - Test with `/api/products`, not `/products`

3. **Check Logging Level:**
   - Enable DEBUG level for the filter
   - Check if filter logs appear

4. **Check Component Scanning:**
   - Verify `FilterConfig.java` is in a scanned package
   - Ensure `@Configuration` annotation is present

### Common Issues:

| Issue | Cause | Solution |
|--------|--------|----------|
| No filter logs | Filter not registered | Check `FilterConfig.java` |
| Filter logs but no MDC | User not found in DB | Verify user ID exists |
| Invalid UUID warning | Bad UUID format | Use proper UUID format |
| Context not cleared | Exception in filter | Check finally block execution |

## ✅ Verification Checklist

- [ ] Application starts without errors
- [ ] Filter registration logs appear
- [ ] Requests to `/api/*` trigger filter logs
- [ ] Valid `X-User-Id` sets MDC context
- [ ] Invalid `X-User-Id` shows warning logs
- [ ] MDC context is cleared after each request
- [ ] No memory leaks from MDC buildup

## 📝 Summary

The `SimpleAuthFilter` is now:
- ✅ **Properly registered** in Spring filter chain
- ✅ **Applied to all API endpoints** (`/api/*`)
- ✅ **Enhanced with logging** for debugging
- ✅ **Safe MDC handling** with proper cleanup
- ✅ **Ready for testing** with the provided examples

**Remember:** This filter provides **logging context only**, not real security. For production use, implement proper JWT authentication and authorization.
