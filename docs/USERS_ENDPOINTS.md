# Users API — Frontend Reference

Base URL: `http://localhost:8080` (adjust to your environment)

Notes:
- All endpoints accept and produce JSON.
- Date/time fields use ISO-8601 OffsetDateTime (e.g. 2026-02-12T08:37:45.123Z).
- UUIDs are used for identifiers (example format: `0c6ff99a-d894-48c7-a8c6-194413146892`).
- The server wraps responses in `ApiResponse<T>`; examples below show the `data` payload directly for clarity.
- If your backend requires authentication, include `Authorization: Bearer <token>` header; otherwise no auth header is needed.

---

## DTOs

### CreateUserRequest (request body for create & update)
```json
{
  "emailAddress": "string (required, email)",
  "firstName": "string (required)",
  "lastName": "string (required)",
  "phoneNumber": "string (required)",
  "password": "string (required, min 8, max 100)"
}
```

### UserResponse (returned user object)
```json
{
  "id": "uuid",
  "emailAddress": "string",
  "firstName": "string",
  "lastName": "string",
  "phoneNumber": "string",
  "isActive": true,
  "createdAt": "2026-02-12T08:37:45.123Z",
  "updatedAt": "2026-02-12T08:37:45.123Z",
  "role": "USER" // role is a single string
}
```

---

## Endpoints

### 1) Create user
- URL: `POST /api/users`
- Request: `CreateUserRequest`
- Success: 201 Created
- Response body: ApiResponse<UserResponse> (data contains the created user)

Sample fetch:
```js
fetch('http://localhost:8080/api/users', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    emailAddress: 'test@example.com',
    firstName: 'Miracle',
    lastName: 'Adams',
    phoneNumber: '1111111111',
    password: 'password123'
  })
})
.then(r => r.json())
.then(res => console.log(res.data));
```

Errors to handle:
- 400 Bad Request — validation errors (missing/invalid fields)
- 409 Conflict (or 400) — email already exists
- 500 Internal Server Error

---

### 2) Get user by id
- URL: `GET /api/users/{id}`
- Success: 200 OK
- Response body: ApiResponse<UserResponse>

Sample fetch:
```js
fetch('http://localhost:8080/api/users/0c6ff99a-d894-48c7-a8c6-194413146892')
  .then(r => r.json())
  .then(res => console.log(res.data));
```

Errors:
- 404 Not Found — user not found

---

### 3) Get user by email
- URL: `GET /api/users/email/{email}`
- Success: 200 OK
- Response body: ApiResponse<UserResponse>

Note: email must be URL-encoded when calling.

Sample fetch:
```js
fetch('http://localhost:8080/api/users/email/test%40example.com')
  .then(r => r.json())
  .then(res => console.log(res.data));
```

---

### 4) Get all users (paginated)
- URL: `GET /api/users?page={page}&size={size}`
- Defaults: `page=0`, `size=10`
- Success: 200 OK
- Response: ApiResponse<PageResponse<UserResponse>> — `PageResponse` contains items and pagination metadata

PageResponse format (example `data`):
```json
{
  "items": [ /* array of UserResponse */ ],
  "page": 0,
  "size": 10,
  "total": 123
}
```

Sample fetch:
```js
fetch('http://localhost:8080/api/users?page=0&size=10')
  .then(r => r.json())
  .then(res => console.log(res.data.items, res.data.total));
```

---

### 5) Search users (paginated)
- URL: `GET /api/users/search?keyword={keyword}&page={page}&size={size}`
- Success: 200 OK
- Response: ApiResponse<PageResponse<UserResponse>>

Sample fetch:
```js
fetch('http://localhost:8080/api/users/search?keyword=adams&page=0&size=10')
  .then(r => r.json())
  .then(res => console.log(res.data.items));
```

---

### 6) Update user
- URL: `PUT /api/users/{id}`
- Request body: `CreateUserRequest` (same as create)
- Success: 200 OK
- Response: ApiResponse<UserResponse>

Notes: If email changes, the API will check uniqueness.

Sample fetch:
```js
fetch('http://localhost:8080/api/users/0c6f...892', {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ emailAddress: 'new@example.com', password: 'newpass123' })
}).then(r => r.json()).then(res => console.log(res.data));
```

Errors:
- 400 validation problems
- 404 not found
- 409 duplicate email

---

### 7) Delete user
- URL: `DELETE /api/users/{id}`
- Success: 200 OK
- Response: ApiResponse<Void> (message only)

Sample fetch:
```js
fetch('http://localhost:8080/api/users/0c6f...892', { method: 'DELETE' })
  .then(r => r.json())
  .then(res => console.log(res.message));
```

---

### 8) Activate / Deactivate user
- Activate: `POST /api/users/{id}/activate` — 200 OK
- Deactivate: `POST /api/users/{id}/deactivate` — 200 OK

Response: ApiResponse<Void> with message.

Sample fetch:
```js
fetch('http://localhost:8080/api/users/0c6f...892/activate', { method: 'POST' });
```

---

## Frontend implementation notes
- Always send `Content-Type: application/json` for JSON requests.
- Date fields are `OffsetDateTime` strings in ISO-8601 (UTC `Z` or with offset). Use native JS Date or a library (dayjs/luxon/moment) to parse.
- When showing user lists, use `page` and `size` query params; display `total` from `PageResponse`.
- On register/create, do not store raw password on client beyond sending it to backend.
- Role is a single string (e.g., "USER" or "ADMIN"). Use it to conditionally show admin UI.
- Handle errors: show validation messages for 400 (server returns field errors in `ApiResponse.errors`), 401/403 as authorization errors, 404 for missing resources, and generic message for 500.

## CORS note
If your frontend runs on a different origin (e.g. `http://localhost:3000`), ensure the backend sets CORS headers to allow that origin. Example required header: `Access-Control-Allow-Origin: http://localhost:3000`.

---

If you want, I can also generate a ready-to-use Postman collection or React service helper functions for these endpoints. Which would you prefer next?
