# REST API Reference — Smart E-Commerce API

Base URL (dev): `http://localhost:8080`
Headers: `Content-Type: application/json`
Date/time format: ISO OffsetDateTime (e.g. `2026-02-12T09:00:00+00:00`).

This file lists the REST endpoints and exact request/response DTOs based on the current database schema and repository/controller logs in the project. Fields are strictly derived from the schema and existing DTOs — no invented fields.

---

## Quick checklist
- [x] Authentication endpoints (register, authenticate)
- [x] User endpoints (no password in update)
- [x] Category endpoints
- [x] Product endpoints
- [x] Address endpoints
- [x] Review endpoints
- [x] Cart endpoints
- [x] Wishlist endpoints
- [x] Payment Method & Shipping Method endpoints
- [x] Order endpoints (full create/update/delete/item reconciliation guidance)
- [x] Common error shapes and examples

---

## Common types

ErrorResponse
- timestamp: string (OffsetDateTime)
- status: integer
- error: string
- message: string
- path: string

PageResponse<T>
- page: int
- size: int
- totalElements: long
- totalPages: int
- content: T[]

UUIDs are strings. All timestamps are ISO OffsetDateTime strings.

---

# Authentication
Base path: `/api/auth`

## Register
- Method: POST
- URL: `/api/auth/register`
- Request: CreateUserRequest
  - emailAddress (string, required)
  - firstName (string, optional)
  - lastName (string, optional)
  - phoneNumber (string, optional)
  - password (string, required)
- Response: AuthResponse (201 Created)
  - userId (UUID)
  - role (string)
- Errors: 400, 409, 500

Example request:
```
{
  "emailAddress":"test@gmail.com",
  "firstName":"Miracle",
  "lastName":"Adams",
  "phoneNumber":"1111111111",
  "password":"password"
}
```

## Authenticate (login)
- Method: POST
- URL: `/api/auth/authenticate`
- Request: AuthRequest
  - email (string, required)
  - password (string, required)
- Response: AuthResponse (200)
  - userId (UUID)
  - role (string)
- Errors: 401, 400, 500

Example request:
```
{ "email":"test@gmail.com", "password":"password" }
```

---

# Users
Base path: `/api/users`

UserResponse
- id (UUID)
- emailAddress (string)
- firstName (string|null)
- lastName (string|null)
- phoneNumber (string|null)
- isActive (boolean)
- roles (string)    # current DB stores role as a string
- createdAt (OffsetDateTime)
- updatedAt (OffsetDateTime)

UpdateUserRequest (no password)
- emailAddress (string, optional)
- firstName (string, optional)
- lastName (string, optional)
- phoneNumber (string, optional)
- isActive (boolean, optional)
- roles (string, optional)

Endpoints
- GET `/api/users/{userId}` -> UserResponse (200)
- GET `/api/users` -> PageResponse<UserResponse> (admin)
- PUT `/api/users/{userId}` (body: UpdateUserRequest) -> UserResponse (200)
- DELETE `/api/users/{userId}` -> 204

Errors: 400, 404, 409, 500

---

# Categories
Base path: `/api/categories`

CategoryResponse
- id (UUID)
- categoryName (string)
- createdAt (OffsetDateTime)

CreateCategoryRequest
- categoryName (string)

UpdateCategoryRequest
- categoryName (string, optional)

Endpoints
- GET `/api/categories` -> PageResponse<CategoryResponse>
- GET `/api/categories/{categoryId}` -> CategoryResponse
- POST `/api/categories` -> CategoryResponse (201)
- PUT `/api/categories/{categoryId}` -> CategoryResponse
- DELETE `/api/categories/{categoryId}` -> 204

Errors: 400 (validation), 409 (duplicate categoryName), 500

Note: repository SQL must match current schema (only `category_name` present). If you see "No value specified for parameter 2" fix repository SQL to provide only required params.

---

# Products
Base path: `/api/products`

ProductResponse
- id (UUID)
- categoryId (UUID)
- name (string)
- description (string|null)
- price (decimal)
- stockQuantity (int)
- isActive (boolean)
- images (JSON array)  # maps to JSONB `images` column
- createdAt (OffsetDateTime)
- updatedAt (OffsetDateTime)

CreateProductRequest / UpdateProductRequest
- categoryId (UUID)
- name (string)
- description (string, optional)
- price (decimal)
- stockQuantity (int)
- isActive (boolean, optional)
- images (array of strings, optional)

Endpoints
- GET `/api/products` -> PageResponse<ProductResponse> (supports page/size, categoryId, search)
- GET `/api/products/{productId}` -> ProductResponse
- POST `/api/products` -> ProductResponse (201)
- PUT `/api/products/{productId}` -> ProductResponse
- DELETE `/api/products/{productId}` -> 204

Errors: 400, 404, 409, 500

Note: Do not expect `primaryImage` (not in DB/schema).

---

# Addresses
Base path: `/api/addresses`

AddressResponse
- id (UUID)
- userId (UUID)
- addressLine (string|null)
- city (string|null)
- region (string|null)
- country (string|null)
- postalCode (string|null)
- addressType (string|null)
- createdAt (OffsetDateTime)

CreateAddressRequest
- userId (UUID)
- addressLine (string)
- city (string)
- region (string)
- country (string)
- postalCode (string)
- addressType (string)

UpdateAddressRequest
- same fields as create but optional

Endpoints
- GET `/api/addresses` -> all addresses (admin)
- GET `/api/addresses/{addressId}` -> AddressResponse
- GET `/api/addresses/user/{userId}` -> AddressResponse[]
- POST `/api/addresses` -> AddressResponse (201)
- PUT `/api/addresses/{addressId}` -> AddressResponse
- DELETE `/api/addresses/{addressId}` -> 204

Troubleshooting
- If createdAt is null in responses: ensure repository RowMapper converts DB TIMESTAMP to OffsetDateTime (e.g., via LocalDateTime -> OffsetDateTime.withZoneSameInstant(ZoneOffset.UTC) or use SQL TIMESTAMP WITH TIME ZONE). Handle nulls safely.
- If `GET /api/addresses/user/{userId}` returns empty but DB has rows: verify SQL uses `user_id` column and query parameter is a valid UUID.

---

# Reviews
Base path: `/api/reviews`

ReviewResponse
- id (UUID)
- userId (UUID)
- productId (UUID)
- rating (int)
- comment (string|null)
- createdAt (OffsetDateTime)
- updatedAt (OffsetDateTime)

CreateReviewRequest
- userId (UUID)
- productId (UUID)
- rating (int)
- comment (string, optional)

UpdateReviewRequest
- rating (int, optional)
- comment (string, optional)

Endpoints
- POST `/api/reviews` -> ReviewResponse (201)
- GET `/api/reviews` -> PageResponse<ReviewResponse>
- GET `/api/reviews/product/{productId}` -> ReviewResponse[]
- GET `/api/reviews/{reviewId}` -> ReviewResponse
- PUT `/api/reviews/{reviewId}` -> ReviewResponse
- DELETE `/api/reviews/{reviewId}` -> 204

Date/time note
- If `OffsetDateTime.from()` errors on a LocalDateTime value in RowMapper, convert LocalDateTime explicitly: `localDateTime.atOffset(ZoneOffset.UTC)` or `OffsetDateTime.of(localDateTime, ZoneOffset.UTC)`.

---

# Shopping Cart
Base path: `/api/cart`

CartResponse
- id (UUID)
- userId (UUID)
- createdAt (OffsetDateTime)
- items: CartItemResponse[]

CartItemResponse
- id (UUID)
- cartId (UUID)
- productId (UUID)
- quantity (int)

CartItemRequest
- productId (UUID)
- quantity (int)

Endpoints (observed in logs)
- GET `/api/cart/user/{userId}` -> CartResponse (auto-creates cart if none exists)
- GET `/api/cart/count?userId={userId}` -> integer count
- POST `/api/cart/user/{userId}/items` -> CartItemResponse (201)
- PUT `/api/cart/items/{itemId}` -> CartItemResponse
- DELETE `/api/cart/items/{itemId}` -> 204

Notes
- If you see "cannot execute INSERT in a read-only transaction" when saving a cart, ensure the repository/service methods that write to DB are not run under read-only transaction attribute.

---

# Wishlist
Base path: `/api/wishlist`

WishlistItemResponse
- id (UUID)
- userId (UUID)
- productId (UUID)
- createdAt (OffsetDateTime)

CreateWishlistItemRequest
- userId (UUID)
- productId (UUID)

Endpoints
- GET `/api/wishlist/user/{userId}` -> WishlistItemResponse[]
- POST `/api/wishlist` -> WishlistItemResponse (201)
- DELETE `/api/wishlist/{id}` -> 204

---

# Payment Methods
Base path: `/api/payment-methods`

PaymentMethodResponse
- id (UUID)
- userId (UUID)
- paymentType (string|null)
- provider (string|null)
- accountNumber (string|null)
- expiryDate (OffsetDateTime|null)
- createdAt (OffsetDateTime)

CreatePaymentMethodRequest
- userId (UUID)
- paymentType (string)
- provider (string)
- accountNumber (string)
- expiryDate (OffsetDateTime, optional)

Endpoints
- GET `/api/payment-methods/user/{userId}` -> PaymentMethodResponse[]
- POST `/api/payment-methods` -> PaymentMethodResponse (201)
- PUT `/api/payment-methods/{id}` -> PaymentMethodResponse
- DELETE `/api/payment-methods/{id}` -> 204

Add Swagger/OpenAPI annotations to the controller for spec generation.

---

# Shipping Methods
Base path: `/api/shipping-methods`

ShippingMethodResponse
- id (UUID)
- name (string)
- description (string|null)
- price (decimal|null)
- estimatedDays (int|null)
- createdAt (OffsetDateTime)

CreateShippingMethodRequest
- name (string)
- description (string)
- price (decimal)
- estimatedDays (int)

Endpoints
- GET `/api/shipping-methods` -> ShippingMethodResponse[]
- GET `/api/shipping-methods/{id}` -> ShippingMethodResponse
- POST `/api/shipping-methods` -> ShippingMethodResponse (201)
- PUT `/api/shipping-methods/{id}` -> ShippingMethodResponse
- DELETE `/api/shipping-methods/{id}` -> 204

---

# Orders
Base path: `/api/orders`

(Full details reproduced from `docs/ORDER_ENDPOINTS.md` — create/update semantics below.)

OrderResponse
- id (UUID)
- orderNumber (string)
- userId (UUID)
- status (string)
- paymentMethodId (UUID|null)
- shippingMethodId (UUID|null)
- paymentStatus (string|null)
- subtotal (decimal)
- totalAmount (decimal)
- items: OrderItemResponse[]
- createdAt (OffsetDateTime)
- updatedAt (OffsetDateTime)

OrderItemResponse
- id (UUID)
- orderId (UUID)
- productId (UUID)
- unitPrice (decimal)
- quantity (int)

CreateOrderRequest
- userId (UUID)
- paymentMethodId (UUID|null)
- shippingMethodId (UUID|null)
- items: [ { productId, quantity, unitPrice? } ]

UpdateOrderRequest
- status (string, optional)
- paymentMethodId (UUID, optional)
- shippingMethodId (UUID, optional)
- paymentStatus (string, optional)
- items: [ { id? , productId, quantity, unitPrice? } ] (optional)

Endpoints
- POST `/api/orders` -> OrderResponse (201)
- GET `/api/orders/{orderId}` -> OrderResponse
- GET `/api/orders/user/{userId}` -> PageResponse<OrderResponse>
- GET `/api/orders` -> PageResponse<OrderResponse> (admin)
- PUT `/api/orders/{orderId}` -> OrderResponse (200)  # reconcile order_item rows when items provided
- DELETE `/api/orders/{orderId}` -> 204
- GET `/api/orders/{orderId}/items` -> OrderItemResponse[]

Important behaviour for PUT (recommended Replace strategy)
- When `items` is provided, treat array as authoritative: update matching item rows, insert new ones, delete rows not present, then recalc `subtotal` and `total_amount` (include shipping price if shippingMethodId is set).
- Frontend must send full desired items list when editing.

---

# Error codes (frontend expectations)
- 200 OK — success
- 201 Created — resource created
- 204 No Content — delete success
- 400 Bad Request — validation/malformed
- 401 Unauthorized — auth failure
- 403 Forbidden — no permission
- 404 Not Found — missing resource
- 409 Conflict — unique constraint
- 500 Internal Server Error — unexpected

---

# Frontend integration notes
- Use exact endpoints above with `Content-Type: application/json`.
- Timestamps: parse OffsetDateTime.
- UUIDs: strings.
- Role is a string on user (not JSON array) — frontend should expect `role` string.
- When updating orders, use Replace strategy unless backend implements a different merge contract.
- If you encounter DB mapping issues (missing columns, null timestamps, incorrect column names), check the repository row-mappers and SQL to match the final schema.

---

If you'd like, I can:
- generate example cURL commands for every endpoint,
- add OpenAPI annotations to controllers that are missing them,
- implement `PUT /api/orders/{orderId}` reconciliation in the backend.

Tell me which next step you want.

