# Authentication Endpoints (Frontend Reference)

Quick plan:
- List every authentication endpoint exposed by the backend.
- Include exact URL path, HTTP method, request/response DTOs, examples, and errors the frontend should handle.

Status: created `docs/AUTH_ENDPOINTS.md` with full details.

---

# Authentication Endpoints
Base path: `/api/auth`

Notes:
- Replace `http://localhost:8080` with your actual host/port (app runs on 8080 by default in this project).
- All requests use `Content-Type: application/json`.

## 1) Register user
- URL: `POST /api/auth/register`
- Purpose: Create a new user account. The server will hash the password before storing.

Request body (JSON) — `CreateUserRequest`:
- emailAddress (string, required) — valid email, max 255 chars
- firstName (string, optional) — max 100 chars
- lastName (string, optional) — max 100 chars
- phoneNumber (string, optional) — max 20 chars
- password (string, required) — min 8, max 100 chars

Example request:
```json
{
  "emailAddress": "adams@gmail.com",
  "firstName": "Miracle",
  "lastName": "Adams",
  "phoneNumber": "1111111111",
  "password": "password123"
}
```

Successful response:
- HTTP 201 Created
- Body (JSON) — `AuthResponse`:
  - userId (UUID) — created user id
  - role (string) — assigned role (e.g. "USER")

Example response:
```json
{
  "userId": "0c6ff99a-d894-48c7-a8c6-194413146892",
  "role": "USER"
}
```

Error responses to handle:
- 400 Bad Request — validation errors (missing email/password or failing constraints). Body may contain validation errors depending on API error format.
- 409 Conflict — email already exists (may be returned as 400 or 500 depending on server handling). Handle duplicate-email gracefully on the frontend.
- 500 Internal Server Error — unexpected server problem.

Notes for the frontend:
- You should send `Content-Type: application/json` header.
- Do NOT store passwords client-side. Send plain password over HTTPS only.
- After successful register, use the returned `userId` and `role` to show post-registration UI.

---

## 2) Authenticate (login)
- URL: `POST /api/auth/authenticate`
- Purpose: Verify a user's credentials and return their id and role.

Request body (JSON) — `AuthRequest`:
- email (string, required) — user's email
- password (string, required) — raw password

Example request:
```json
{
  "email": "adams@gmail.com",
  "password": "password123"
}
```

Successful response:
- HTTP 200 OK
- Body (JSON) — `AuthResponse`:
  - userId (UUID)
  - role (string)

Example response:
```json
{
  "userId": "0c6ff99a-d894-48c7-a8c6-194413146892",
  "role": "USER"
}
```

Failure responses to handle:
- 401 Unauthorized — invalid credentials or inactive user (empty body by default). The frontend should show a login error message.
- 400 Bad Request — input validation errors.
- 500 Internal Server Error — server-side problem.

Notes for the frontend:
- The backend currently returns just `userId` and `role` (no JWT or token by default) — if you need session management, you must implement a client-side session or request the team to return a token.
- If you want role-based UI, read `role` from the response and store it in your client state (temporary session storage). Keep secure data handling in mind.

---

# DTO definitions (frontend-friendly)

### create account request
```json
{
  "emailAddress": "string (required)",
  "firstName": "string (optional)",
  "lastName": "string (optional)",
  "phoneNumber": "string (optional)",
  "password": "string (required)"
}
```

### AuthRequest
```json
{
  "email": "string (required)",
  "password": "string (required)"
}
```

### AuthResponse
```json
{
  "userId": "uuid string",
  "role": "string"
}
```

---

# Frontend integration notes & examples
- Headers:
  - `Content-Type: application/json`
  - If your backend uses CORS, ensure backend allows your frontend origin (e.g., http://localhost:3000).

- Example fetch (login):
```js
fetch('http://localhost:8080/api/auth/authenticate', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'adams@gmail.com', password: 'password123' })
})
.then(res => {
  if (res.status === 200) return res.json();
  if (res.status === 401) throw new Error('Invalid credentials');
  throw new Error('Unexpected response: ' + res.status);
})
.then(data => {
  // data.userId, data.role
})
.catch(err => console.error(err));
```

- Example fetch (register):
```js
fetch('http://localhost:8080/api/auth/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ emailAddress: 'adams@gmail.com', firstName: 'Miracle', lastName: 'Adams', phoneNumber: '1111111111', password: 'password123' })
})
.then(res => {
  if (res.status === 201) return res.json();
  // handle validation errors and others
})
.then(data => {
  // data.userId, data.role
})
```

---

# Additional backend behaviors the frontend relies on
- Passwords are hashed server-side before persist — the frontend sends plain password only.
- Default role fallback: server returns a `role` (e.g., `USER`) if not set.
- Authentication returns only `userId` and `role` (no JWT);
- CORS: If frontend is served from a different origin, ensure backend's CORS allows it (backend must set Access-Control-Allow-Origin header).

---