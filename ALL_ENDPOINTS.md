# All REST API Endpoints (Full)

Base URL (dev): `http://localhost:8080`

This file lists every REST endpoint implemented in the project, with method, full path, parameters, request body shape, response shape summary, example curl/fetch usage and common response codes. Use this as the single reference for frontend integration.

---

## Table of Contents
- Home
- Authentication
- Users
- Addresses
- Products
- Categories
- Wishlist
- Reviews
- Cart
- Orders
- Common response shapes & error handling

---

## Home

### GET /
- Description: API info / health quick endpoint
- Method: GET
- Path: `/`
- Request: none
- Response: ApiResponse<Map<String,Object>> with fields: name, version, description, timestamp
- Success: 200 OK

Example:
```
curl -i http://localhost:8080/
```

---

## Authentication
Base path: `/api/auth`

### POST /api/auth/register
- Description: Register a new user
- Method: POST
- Path: `/api/auth/register`
- Request body (JSON):
  - `emailAddress` (string, required)
  - `firstName` (string, optional)
  - `lastName` (string, optional)
  - `phoneNumber` (string, optional)
  - `password` (string, required)
- Response: ApiResponse<AuthResponse> (contains accessToken, refreshToken, user)
- Success: 201 Created

Example:
```
curl -i -X POST http://localhost:8080/api/auth/register \
 -H 'Content-Type: application/json' \
 -d '{"emailAddress":"john@example.com","password":"pass123"}'
```

### POST /api/auth/login
- Description: Login and receive tokens
- Method: POST
- Request body:
  - `emailAddress` (string)
  - `password` (string)
- Response: ApiResponse<AuthResponse>
- Success: 200 OK

### POST /api/auth/refresh
- Description: Refresh access token
- Method: POST
- Request body:
  - `refreshToken` (string)
- Response: ApiResponse with new tokens
- Success: 200 OK

### POST /api/auth/logout
- Description: Logout current user (invalidate tokens / remove session)
- Method: POST
- Headers: Authorization: Bearer <token> (if required)
- Response: ApiResponse<Void>
- Success: 200 OK

---

## Users
Base path: `/api/users`

### POST /api/users
- Description: Create user (admin or public register depending on app flow)
- Request body:
  - `emailAddress`, `firstName`, `lastName`, `phoneNumber`, `password`
- Response: ApiResponse<UserResponse>
- Success: 201 Created

### GET /api/users/{id}
- Description: Get user by id
- Path param: `id` (uuid)
- Response: ApiResponse<UserResponse>
- Success: 200 OK

### GET /api/users/email/{email}
- Description: Get user by email
- Response: ApiResponse<UserResponse>
- Success: 200 OK

### GET /api/users
- Description: Paginated list of users
- Query: `page`, `size`
- Response: ApiResponse<PageResponse<UserResponse>>

### GET /api/users/search?keyword=...
- Description: Search users by keyword
- Query: `keyword`, `page`, `size`
- Response: PageResponse<UserResponse>

### PUT /api/users/{id}
- Description: Update user
- Request body: fields to update
- Response: ApiResponse<UserResponse>
- Success: 200 OK

### DELETE /api/users/{id}
- Description: Delete user
- Response: 204 No Content

### PATCH /api/users/{id}/activate
- Description: Activate user
- Response: ApiResponse<Void>

### PATCH /api/users/{id}/deactivate
- Description: Deactivate user
- Response: ApiResponse<Void>

Notes: UserResponse shape includes id, emailAddress, firstName, lastName, fullName, phoneNumber, isActive, createdAt, updatedAt.

---

## Addresses
Base path: `/api/addresses`

### POST /api/addresses
- Description: Create new address for user
- Request body:
  - `userId` (uuid)
  - `addressLine`, `city`, `region`, `country`, `postalCode`, `isDefault`, `addressType`
- Response: ApiResponse<AddressResponse>
- Success: 201 Created

### GET /api/addresses/{id}
- Description: Get address by id
- Response: ApiResponse<AddressResponse>

### GET /api/addresses/user/{userId}
- Description: Get all addresses for user
- Response: ApiResponse<List<AddressResponse>>

### GET /api/addresses/user/{userId}/type/{type}
- Description: Get addresses filtered by type (shipping/billing)
- Response: ApiResponse<List<AddressResponse>>

### GET /api/addresses/user/{userId}/default
- Description: Get default address for user
- Response: ApiResponse<AddressResponse>

### PUT /api/addresses/{id}
- Description: Update address
- Response: ApiResponse<AddressResponse>

### DELETE /api/addresses/{id}
- Description: Delete address
- Response: 204 No Content

### PATCH /api/addresses/{id}/default
- Description: Set address as default
- Response: ApiResponse<AddressResponse>

AddressResponse includes id, userId, addressLine, city, region, country, postalCode, isDefault, addressType, fullAddress, createdAt.

---

## Products
Base path: `/api/products`

### POST /api/products
- Create product
- Request body: categoryId, sku, name, description, price, stockQuantity, isActive, images
- Response: ApiResponse<ProductResponse>
- Success: 201 Created

### GET /api/products/{id}
- Get product by id
- Response: ApiResponse<ProductResponse>

### GET /api/products/sku/{sku}
- Get product by SKU
- Response: ApiResponse<ProductResponse>

### GET /api/products
- Paginated list: query `page`, `size`
- Response: ApiResponse<PageResponse<ProductResponse>>

### GET /api/products/active
- Active products

### GET /api/products/category/{categoryId}
- Filter by category

### GET /api/products/search?keyword=...
- Search products

### GET /api/products/in-stock
- Products with stock

### PUT /api/products/{id}
- Update product

### DELETE /api/products/{id}
- Delete product

### PATCH /api/products/{id}/activate
- Activate

### PATCH /api/products/{id}/deactivate
- Deactivate

### PATCH /api/products/{id}/stock?quantity={n}
- Update stock (query param)

ProductResponse includes id, categoryId, categoryName, sku, name, description, price, stockQuantity, isActive, inStock, images, primaryImage, averageRating, reviewCount, createdAt, updatedAt.

---

## Categories
Base path: `/api/categories`

### POST /api/categories
- Create category
- Request: parentCategoryId (uuid), categoryName
- Response: ApiResponse<CategoryResponse>

### GET /api/categories/{id}
- Get category by id

### GET /api/categories
- Get all categories (flat)

### GET /api/categories/root
- Get root categories

### GET /api/categories/tree
- Get hierarchical tree

### GET /api/categories/{parentId}/subcategories
- Get subcategories

### PUT /api/categories/{id}
- Update

### DELETE /api/categories/{id}
- Delete

CategoryResponse includes id, parentCategoryId, categoryName, parentCategoryName, productCount, subCategories.

---

## Wishlist
Base path: `/api/wishlist`

### POST /api/wishlist
- Add to wishlist
- Request body: AddToWishlistRequest
  - `userId` (uuid)
  - `productId` (uuid)
- Response: ApiResponse<WishlistItemResponse>
- Success: 201 Created

### GET /api/wishlist/user/{userId}
- Get wishlist for user
- Response: ApiResponse<List<WishlistItemResponse>>

### GET /api/wishlist/user/{userId}/page?page={p}&size={s}
- Paginated

### GET /api/wishlist/user/{userId}/count
- Get wishlist count (returns long)

### GET /api/wishlist/check?userId={u}&productId={p}
- Check if product in wishlist — returns boolean

### DELETE /api/wishlist/{id}
- Remove wishlist item by id

### DELETE /api/wishlist/user/{userId}/product/{productId}
- Remove product from wishlist

### DELETE /api/wishlist/user/{userId}
- Clear wishlist

### POST /api/wishlist/user/{userId}/product/{productId}/move-to-cart
- Move wishlist item to cart

WishlistItemResponse fields: id, userId, productId, productName, productSku, productPrice, productImage, productInStock, createdAt.

---

## Reviews
Base path: `/api/reviews`

### POST /api/reviews
- Create review
- Request body: CreateReviewRequest
  - productId, userId, rating (1-5), title, comment
- Response: ApiResponse<ReviewResponse>
- Success: 201 Created

### GET /api/reviews/{id}
- Get review by id

### GET /api/reviews/product/{productId}?page=&size=
- Reviews by product (paginated)

### GET /api/reviews/user/{userId}?page=&size=
- Reviews by user (paginated)

### GET /api/reviews/product/{productId}/rating
- Get average rating for product (returns decimal)

### GET /api/reviews/user/{userId}/product/{productId}/exists
- Check if user reviewed product (returns boolean)

### PUT /api/reviews/{id}
- Update review

### DELETE /api/reviews/{id}
- Delete review

ReviewResponse fields: id, productId, userId, userName, rating, title, comment, createdAt, updatedAt.

---

## Cart
Base path: `/api/cart`

### GET /api/cart
- List all carts (paginated, admin)
- Query: page, size

### GET /api/cart/user/{userId}
- Get user's cart

### POST /api/cart/user/{userId}/items
- Add item to cart
- Body: { productId: uuid, quantity: int }

### PUT /api/cart/user/{userId}/items/{itemId}?quantity={n}
- Update item quantity

### DELETE /api/cart/user/{userId}/items/{itemId}
- Remove item

### DELETE /api/cart/user/{userId}
- Clear cart

### GET /api/cart/user/{userId}/count
- Get cart item count (path)

### GET /api/cart/count?userId={userId}
- Get cart item count (query)

CartResponse shape provided in DTO: id, userId, totalItems, totalValue, createdAt, items[]. See project DTOs for types.

---

## Orders
Base path: `/api/orders`

### POST /api/orders
- Create order (checkout)
- Body: CreateOrderRequest
  - userId, shippingAddressId, shippingMethodId (optional), customerNotes
- Response: ApiResponse<OrderResponse>
- Success: 201 Created

### GET /api/orders/{id}
- Get order by id

### GET /api/orders/number/{orderNumber}
- Get by order number

### GET /api/orders
- List orders paginated

### GET /api/orders/user/{userId}
- Orders by user

### GET /api/orders/status/{status}
- Orders by status

### PATCH /api/orders/{id}/status?status={newStatus}
- Update order status

### PATCH /api/orders/{id}/cancel
- Cancel order

OrderResponse fields: id, userId, orderNumber, status, paymentStatus, subtotal, shippingCost, total, itemCount, customerNotes, createdAt, cancelledAt, items[]. See project DTOs.

---

## Common response shapes & error handling

All endpoints return `ApiResponse<T>` wrapper with fields: status (boolean), message (string), data (T), statusCode (int), timestamp.

Error responses use `ApiError` inside `ApiResponse.data` (status=false) with fields: errorCode (enum), message, detail, path, correlationId, clientIp, timestamp.

Common error codes: RESOURCE_NOT_FOUND, BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, CONFLICT, VALIDATION_FAILED, DUPLICATE_RESOURCE, INSUFFICIENT_STOCK, INTERNAL_ERROR, PAYMENT_FAILED etc.

HTTP codes used:
- 200 OK — success
- 201 Created — resource created
- 204 No Content — deletion successful
- 400 Bad Request — validation/type mismatch
- 401 Unauthorized — authentication required/failed
- 403 Forbidden — access denied
- 404 Not Found — resource missing
- 409 Conflict — duplicate/resource conflict
- 500 Internal Server Error — server error

---

## Notes for Frontend
- Use `http://localhost:8080` (backend) as base in development, or set up a dev proxy to avoid CORS.
- For endpoints requiring authentication, include header `Authorization: Bearer <token>`.
- For CORS with credentials/cookies: include `credentials: 'include'` and ensure backend allows credentials for your origin.
- Date format: ISO-8601 with offset (OffsetDateTime). All IDs are UUIDs.

---

If you want a Postman collection or TypeScript interfaces for all endpoints, I can generate and add them next.

