# Frontend Integration & Admin Panel Guide

This file collects every server-side change relevant to the frontend and provides concrete instructions, examples and TypeScript interfaces so you can implement the admin panel and update the client quickly.

---

## 1) Short summary (what changed server-side)
- `app_user` table: new column `roles JSONB DEFAULT '["ROLE_USER"]'` (see `src/main/resources/schema.sql`).
- `User` model: `roles: Set<String>` added. (`src/.../domain/user/entity/User.java`)
- `UserMapper`: reads `roles` JSONB into `Set<String>` (Jackson). (`src/.../domain/user/mapper/UserMapper.java`)
- `UserRepositoryImpl`: INSERT/UPDATE now include `roles` column (serialized JSON). (`src/.../domain/user/repository/UserRepositoryImpl.java`)
- `JwtTokenProvider`: tokens may include `roles` claim. (`src/.../security/JwtTokenProvider.java`)
- `AuthServiceImpl`: register/login/refresh responses include `roles` and tokens are generated with roles. (`src/.../domain/user/service/impl/AuthServiceImpl.java`)
- `AuthResponse`: now includes `roles: List<String>`. (`src/.../domain/user/dto/response/AuthResponse.java`)
- `JwtAuthenticationFilter`: builds authorities from the DB `user.roles` for each request (server-side source of truth). (`src/.../security/JwtAuthenticationFilter.java`)
- `SecurityConfig`: authorization rules updated (public product endpoints, admin-only endpoints, user endpoints). (`src/.../security/SecurityConfig.java`)

---

## 2) What the frontend must do (quick checklist)
- [ ] Use `POST /api/auth/login` and `POST /api/auth/register` to authenticate users.
- [ ] Store `accessToken` (JWT) and `refreshToken` returned in `AuthResponse.data`.
- [ ] Read `AuthResponse.roles` to determine whether to show admin UI (client-side UI only).
- [ ] Always include `Authorization: Bearer <accessToken>` header on protected requests.
- [ ] Handle `401` (redirect to login) and `403` (show not-authorized / hide admin UI) responses.
- [ ] Implement token refresh using `/api/auth/refresh` before expiry.

---

## 3) Auth payloads and example responses
- After successful login/register/refresh the API returns `AuthResponse` (wrapped in ApiResponse). Important fields:
  - `accessToken` (string)
  - `refreshToken` (string)
  - `expiresIn` (number)
  - `userId`, `email`, `firstName`, `lastName`
  - `roles`: string[] (e.g. `["ROLE_USER"]` or `["ROLE_ADMIN","ROLE_USER"]`)

Example (abbreviated) JSON response body:

```json
{
  "status": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "...",
    "expiresIn": 86400000,
    "userId": "0c6ff99a-d894-48c7-a8c6-194413146892",
    "email": "admin@example.com",
    "firstName": "Admin",
    "lastName": "User",
    "roles": ["ROLE_ADMIN","ROLE_USER"]
  }
}
```

Notes:
- The server also stores roles in the JWT's `roles` claim, but the server's `JwtAuthenticationFilter` reads roles from the DB for each request — this allows immediate role revocation/promotion server-side.

---

## 4) Protected endpoints (current config)
Use these as a reference for the admin panel. Confirm controller routes match these patterns in your code.

- Public (no token required):
  - `POST /api/auth/**` (login/register/refresh)
  - `GET /api/products/**` (product listings/details)

- Admin-only (requires `ROLE_ADMIN`):
  - `POST /api/products`
  - `PUT /api/products/{id}`
  - `DELETE /api/products/{id}`
  - `/api/orders/all` (list all orders; check actual controller route)
  - `/api/users/all` (list all users; check actual controller route)

- User or Admin (requires `ROLE_USER` or `ROLE_ADMIN`):
  - `/api/cart/**`
  - `/api/orders/my-orders`

- All other requests require authentication by default.

Recommendation: use method-level security (`@PreAuthorize`) on controller/service methods for precise control (less brittle than URL patterns).

---

## 5) Admin panel features & endpoints (frontend mapping)
Minimum features to implement on the frontend admin panel and the endpoints to call:

- Users management
  - List users (admin): `GET /api/users` (or `/api/users/all`) — confirm route
  - View user details: `GET /api/users/{id}`
  - Activate/deactivate user: `POST /api/users/{id}/activate` and `/deactivate`

- Products management
  - List products: `GET /api/products` (public)
  - Create product: `POST /api/products` (admin)
  - Update product: `PUT /api/products/{id}` (admin)
  - Delete product: `DELETE /api/products/{id}` (admin)

- Orders management
  - List all orders (admin): `GET /api/orders/all` (confirm route)
  - Update order status/payment: existing endpoints (check controller)

---

## 6) Example client flows & snippets

Login, store token and roles (vanilla JS):

```javascript
async function login(email, password) {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });

  const body = await res.json(); // ApiResponse wrapper
  const auth = body.data; // AuthResponse
  localStorage.setItem('accessToken', auth.accessToken);
  localStorage.setItem('roles', JSON.stringify(auth.roles || []));
  const isAdmin = (auth.roles || []).includes('ROLE_ADMIN');
  return { isAdmin, auth };
}
```

Guard route example (React-like pseudo-code):
```javascript
function AdminRoute({ children }) {
  const roles = JSON.parse(localStorage.getItem('roles') || '[]');
  if (!roles.includes('ROLE_ADMIN')) {
    return <Redirect to="/not-authorized" />;
  }
  return children;
}
```

Call admin-only endpoint (create product):
```javascript
const token = localStorage.getItem('accessToken');
await fetch('/api/products', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({ name: 'New product', price: 9.99, categoryId: '...' })
});
```

Handle 403 responses by hiding admin UI and optionally notifying the user.

---

## 7) TypeScript interfaces (copy/paste)

`AuthResponse.ts`
```ts
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userId: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  roles?: string[]; // e.g. ['ROLE_USER'] or ['ROLE_ADMIN','ROLE_USER']
}
```

`UserResponse.ts` (partial)
```ts
export interface UserResponse {
  id: string;
  emailAddress: string;
  firstName?: string;
  lastName?: string;
  isActive?: boolean;
  roles?: string[];
  createdAt?: string;
  updatedAt?: string;
}
```

B) Seeder (recommended for local dev): I can add an `AdminSeeder` ApplicationRunner to create an admin user at app startup (password will be hashed and credentials taken from env vars). Tell me if you want me to implement this and I will add it.

---

## 9) Important notes & best practices
- Frontend role check is UI-only: the server is authoritative and checks DB roles on each request. If a user is demoted in DB, server will stop authorizing admin endpoints immediately.
- Do not rely solely on token claims for authorization in UI; use them to toggle UI.*
- Use method-level security (`@PreAuthorize`) for stable protection across URL changes.
- In production, store secrets (admin seeder password, JWT secret) in environment variables or a secrets manager — not in source.
- Consider token revocation/blacklist if you need immediate invalidation of tokens beyond DB role changes.

---

## 10) Next actions I can take for you (pick any)
- Implement `PATCH /api/users/{id}/roles` (admin-only) so frontend can promote/demote users from the admin panel.
- Add `AdminSeeder` ApplicationRunner to create a dev admin at startup (config via env vars).
- Add method-level `@PreAuthorize` annotations to controllers to make security robust vs route changes.
- Create a Postman collection for the admin flow.

Tell me which of the next actions you'd like and I'll implement them immediately.

---

*File created by the dev assistant — refer to `src/main/java/.../domain/user/dto/response/AuthResponse.java` for the DTO definition.*

