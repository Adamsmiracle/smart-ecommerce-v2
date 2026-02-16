# Categories API Reference

Base URL: `http://localhost:8080` (replace with your deployment host/port)

All endpoints use `Content-Type: application/json`.

This document reflects the current database schema for `product_category` and the DTOs the frontend should send/expect.

---

## Database reference (product_category)
- `id` UUID PRIMARY KEY
- `category_name` VARCHAR(100) NOT NULL UNIQUE
- `created_at` TIMESTAMP NOT NULL DEFAULT NOW()

Note: The current schema does not include `parent_category_id`. If you plan to introduce nested categories, the DTOs and endpoints must be extended.

---

# Endpoints

All endpoints are under the prefix `/api/categories`.

### 1) Create category
- Method: POST
- URL: `/api/categories`
- Purpose: Create a new product category.

Request DTO: `CreateCategoryRequest`
- Fields:
  - `categoryName` (string, required)

Example request body:
```json
{
  "categoryName": "Electronics"
}
```

Success response:
- Status: 201 Created
- Body: `CategoryResponse`

Errors:
- 400 Bad Request — validation errors (missing/invalid name)
- 409 Conflict — category name already exists

---

### 2) Get category by ID
- Method: GET
- URL: `/api/categories/{id}`
- Params: Path `id` (UUID)
- Success: 200 OK with `CategoryResponse`
- Errors: 404 if not found

Example:
```
GET http://localhost:8080/api/categories/11111111-1111-1111-1111-111111111111
```

---

### 3) Get all categories (paginated)
- Method: GET
- URL: `/api/categories`
- Query params:
  - `page` (int, default 0)
  - `size` (int, default 10)
- Success: 200 OK with `PageResponse<CategoryResponse>`

The returned page contains a `content` array of `CategoryResponse` objects and paging metadata.

---

### 4) Get category count (total number of categories)
- Method: GET
- URL: `/api/categories/count`
- Purpose: Return the total number of categories
- Success: 200 OK
- Body example:
  ```json
  { "count": 42 }
  ```

Notes on how count is computed (backend):
- The simplest implementation uses SQL: `SELECT COUNT(*) FROM product_category;` and returns the integer.
- If you need the number of categories plus product counts, use a JOIN/AGGREGATE (e.g., left join product and group).

---

### 5) Update category
- Method: PUT
- URL: `/api/categories/{id}`
- Params: Path `id` (UUID)
- Request DTO: `UpdateCategoryRequest`
  - `categoryName` (string, required)
- Success: 200 OK with updated `CategoryResponse`
- Errors:
  - 400 Bad Request — validation
  - 404 Not Found — no category with given id
  - 409 Conflict — category name conflicts with another

Example request:
```json
{
  "categoryName": "Car Accessories"
}
```

---

### 6) Delete category
- Method: DELETE
- URL: `/api/categories/{id}`
- Success: 200 OK (or 204 No Content)
- Effect: Removes the category. Be careful: if products reference this category and DB uses `ON DELETE RESTRICT` or similar, delete will fail. Backend should either prevent deletion when products exist, or reassign products before deleting.

Errors:
- 404 Not Found
- 409 Conflict or 400 if category cannot be deleted due to existing references (recommend return 400/409 with message explaining reason)

---

## Error responses (what the frontend should expect)

All error responses follow the project's standard wrapper `ApiResponse<T>` (see `com.miracle.smart_ecommerce_api_v1.common.response.ApiResponse`). Successful responses have `status: true`; error responses use `status: false` and either include `data` with an `ApiError` or an `errors` list for field validation failures.

Relevant server-side handlers (from `GlobalExceptionHandler`) and how they map to HTTP status codes and response payloads for the category endpoints:

- 400 Bad Request
  - Thrown for: validation issues, malformed JSON, missing parameters, type mismatches, IllegalArgumentException, BadRequestException, MethodArgumentNotValidException, HttpMessageNotReadableException, ConstraintViolationException.
  - Response shape (validation fields): ApiResponse with `errors` array of field errors OR `data` containing `ApiError`.
  - Example (field validation errors):

```json
{
  "status": false,
  "message": "Validation Failed",
  "errors": [
    { "field": "categoryName", "message": "must not be blank", "rejectedValue": "" }
  ],
  "statusCode": 400,
  "timestamp": "2026-02-12T09:00:00Z"
}
```

  - Example (ApiError form):

```json
{
  "status": false,
  "message": "Malformed JSON or invalid field type",
  "data": {
    "errorCode": "BAD_REQUEST",
    "message": "Malformed JSON or invalid field type",
    "detail": "Cannot deserialize value of type `java.util.UUID` from String \"abc\"",
    "path": "/api/categories",
    "correlationId": "<cid>",
    "clientIp": "127.0.0.1",
    "timestamp": "2026-02-12T09:00:00Z"
  },
  "statusCode": 400
}
```

- 404 Not Found
  - Thrown for: ResourceNotFoundException when a category id is not present.
  - Response shape: ApiResponse with `data` containing `ApiError`.
  - Example:

```json
{
  "status": false,
  "message": "Not Found",
  "data": {
    "errorCode": "RESOURCE_NOT_FOUND",
    "message": "Not Found",
    "detail": "Category with id '1111-...' not found",
    "path": "/api/categories/1111-...",
    "correlationId": "<cid>",
    "clientIp": "127.0.0.1",
    "timestamp": "2026-02-12T09:00:00Z"
  },
  "statusCode": 404
}
```

- 409 Conflict
  - Thrown for: DuplicateResourceException or DataIntegrityViolationException (e.g., unique constraint on category_name), or when deletion fails due to FK constraints.
  - Example:

```json
{
  "status": false,
  "message": "Conflict",
  "data": {
    "errorCode": "CONFLICT",
    "message": "Conflict",
    "detail": "Category name 'Electronics' already exists",
    "path": "/api/categories",
    "correlationId": "<cid>",
    "clientIp": "127.0.0.1",
    "timestamp": "2026-02-12T09:00:00Z"
  },
  "statusCode": 409
}
```

- 401 Unauthorized / 403 Forbidden
  - Thrown for: UnauthorizedException, AccessDeniedException, authentication/authorization failures.
  - Example (401):

```json
{
  "status": false,
  "message": "Unauthorized",
  "data": {
    "errorCode": "UNAUTHORIZED",
    "message": "Unauthorized",
    "detail": "Authentication required",
    "path": "/api/categories",
    "correlationId": "<cid>",
    "clientIp": "127.0.0.1",
    "timestamp": "2026-02-12T09:00:00Z"
  },
  "statusCode": 401
}
```

- 500 Internal Server Error
  - Thrown for uncaught exceptions; the handler wraps with `ApiError` and returns 500.
  - Example:

```json
{
  "status": false,
  "message": "Internal Server Error",
  "data": {
    "errorCode": "INTERNAL_ERROR",
    "message": "Internal Server Error",
    "detail": "NullPointerException at ...",
    "path": "/api/categories",
    "correlationId": "<cid>",
    "clientIp": "127.0.0.1",
    "timestamp": "2026-02-12T09:00:00Z"
  },
  "statusCode": 500
}
```

Notes for the frontend
- Always inspect `status` (boolean) first. When false, examine `errors` (validation) or `data` (ApiError).
- For field validation errors prefer `errors` list (each item: `field`, `message`, `rejectedValue`). Use it to surface inline validation messages.
- For business/fatal errors read `data.errorCode` (string) to switch on handling logic (e.g., `DUPLICATE_RESOURCE`, `RESOURCE_NOT_FOUND`, `DATA_INTEGRITY`, `INTERNAL_ERROR`).
- Use `statusCode` (top-level) to implement quick client-side logic for common status codes (400/404/409/500).
- Log and surface `correlationId` (in `data`) when reporting server-side problems — it helps to debug issues in server logs.

If you want, I can also append a short table summarizing exception -> HTTP status -> example payload to `docs/CATEGORY_ENDPOINTS.md` for quick copy-paste on the frontend. Would you like that?

---

# DTOs / Request & Response shapes

## CreateCategoryRequest
```json
{
  "categoryName": "string (required)"
}
```

## UpdateCategoryRequest
```json
{
  "categoryName": "string (required)"
}
```

## CategoryResponse
```json
{
  "id": "UUID",
  "categoryName": "string",
  "productCount":   "Long"
}
```

## PageResponse<T>
Common page response shape used across endpoints:
```json
{
  "content": [  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 123,
  "totalPages": 13,
  "first": true,
  "last": false
}
```

---

# Frontend integration details
- Exact URLs (replace host/port if needed):
  - Create: `POST http://localhost:8080/api/categories`
  - Get by id: `GET http://localhost:8080/api/categories/{id}`
  - Get all: `GET http://localhost:8080/api/categories?page=0&size=10`
  - Count: `GET http://localhost:8080/api/categories/count`
  - Update: `PUT http://localhost:8080/api/categories/{id}`
  - Delete: `DELETE http://localhost:8080/api/categories/{id}`

- Headers: `Content-Type: application/json`
- Dates are ISO OffsetDateTime strings (e.g. `2026-02-12T09:00:00+00:00`).
- Validation: ensure `categoryName` is provided and trimmed; handle `409 Conflict` when name duplicates.

---

# Implementation notes for backend (so frontend expectations match behavior)
- When creating/updating categories, validate uniqueness for `category_name` and return 409 with a clear error message when violated.
- When deleting, if products reference the category and the DB prevents deletion, return 400/409 with message "category has products; cannot delete".
- For `GET /api/categories`, include `totalElements` and `totalPages` so frontend can render paginated lists and counts.
- If you want to show product counts per category, backend should add an extra field (e.g., `productCount`) to `CategoryResponse` computed via `LEFT JOIN product ON product.category_id = product_category.id GROUP BY product_category.id`.

---

# Example cURL
Create category:

```bash
curl -X POST "http://localhost:8080/api/categories" \
  -H "Content-Type: application/json" \
  -d '{"categoryName":"Electronics"}'
```

Get all categories (page 0 size 10):

```bash
curl "http://localhost:8080/api/categories?page=0&size=10"
```

Get category count:

```bash
curl "http://localhost:8080/api/categories/count"
```

---

If you want I can:
- Add `productCount` to `CategoryResponse` and implement the repository query to populate it.
- Add OpenAPI annotations to the `CategoryController` so the frontend can import the spec.
- Generate Postman collection for categories.

Tell me which of those you'd like and I'll implement it next.
