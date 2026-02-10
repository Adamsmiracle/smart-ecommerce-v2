_# API Endpoints — Full Reference

This file documents all REST endpoints in the project, their URLs, HTTP methods, request/response DTOs, and short notes (auth/role requirements).

Format:
- Method URL
  - Request body: DTO (if applicable)
  - Query/path params: list
  - Response: DTO (wrapped in ApiResponse)
  - Notes: auth/roles

---

## Authentication

### POST /api/auth/register
- Request body: `CreateUserRequest` (emailAddress, password, firstName, lastName, phoneNumber)
- Response: `AuthResponse` (accessToken, refreshToken, expiresIn, userId, email, firstName, lastName, roles)
- Notes: Public

### POST /api/auth/login
- Request body: `LoginRequest` (email, password)
- Response: `AuthResponse`
- Notes: Public

### POST /api/auth/refresh
- Request body: `RefreshTokenRequest` (refreshToken)
- Response: `AuthResponse`
- Notes: Public

### GET /api/auth/me
- Request header: `Authorization: Bearer <token>`
- Response: `AuthResponse` (current user info including `roles`)
- Notes: Authenticated

### POST /api/auth/logout
- Request: none
- Response: success message
- Notes: Public (stateless logout)

---

## Users

### POST /api/users
- Request body: `CreateUserRequest`
- Response: `UserResponse`
- Notes: Public (creation), server defaults roles = ["ROLE_USER"]

### GET /api/users/{id}
- Path params: id (UUID)
- Response: `UserResponse`
- Notes: Authenticated

### GET /api/users/email/{email}
- Path params: email
- Response: `UserResponse`
- Notes: Authenticated

### GET /api/users
- Query params: page (default 0), size (default 10)
- Response: `PageResponse<UserResponse>`
- Notes: Admin (per SecurityConfig) — may require ROLE_ADMIN

### GET /api/users/search
- Query params: keyword, page, size
- Response: `PageResponse<UserResponse>`
- Notes: Authenticated

### PUT /api/users/{id}
- Path params: id
- Request body: `CreateUserRequest` (used for updates)
- Response: `UserResponse`
- Notes: Authenticated

### DELETE /api/users/{id}
- Path params: id
- Response: success message
- Notes: Admin (or authenticated depending on controller)

### POST /api/users/{id}/activate
- Path params: id
- Response: success message
- Notes: Authenticated (controller allows)

### POST /api/users/{id}/deactivate
- Path params: id
- Response: success message
- Notes: Authenticated

### PATCH /api/users/{id}/roles
- Path params: id
- Request body: `UpdateRolesRequest` ({ roles: ["ROLE_ADMIN","ROLE_USER"] })
- Response: success message
- Notes: Admin-only (`@PreAuthorize("hasRole('ADMIN')")`)

---

## Products

Base path: `/api/products`

### POST /api/products
- Request body: `CreateProductRequest`
- Response: `ProductResponse`
- Notes: Admin-only

### GET /api/products/{id}
- Path params: id
- Response: `ProductResponse`
- Notes: Public

### GET /api/products/sku/{sku}
- Path params: sku
- Response: `ProductResponse`
- Notes: Public

### GET /api/products
- Query params: page, size
- Response: `PageResponse<ProductResponse>`
- Notes: Public

### GET /api/products/active
- Query params: page, size
- Response: `PageResponse<ProductResponse>`
- Notes: Public

### GET /api/products/category/{categoryId}
- Path params: categoryId
- Query params: page, size
- Response: `PageResponse<ProductResponse>`
- Notes: Public

### GET /api/products/search
- Query params: keyword, page, size
- Response: `PageResponse<ProductResponse>`
- Notes: Public

### GET /api/products/price-range
- Query params: minPrice, maxPrice, page, size
- Response: `PageResponse<ProductResponse>`
- Notes: Public

### GET /api/products/in-stock
- Query params: page, size
- Response: `PageResponse<ProductResponse>`
- Notes: Public

### PUT /api/products/{id}
- Path params: id
- Request body: `CreateProductRequest`
- Response: `ProductResponse`
- Notes: Admin-only

### DELETE /api/products/{id}
- Path params: id
- Response: success message
- Notes: Admin-only

### POST /api/products/{id}/activate
- Path params: id
- Response: success message
- Notes: Admin-only

### POST /api/products/{id}/deactivate
- Path params: id
- Response: success message
- Notes: Admin-only

### PATCH /api/products/{id}/stock?quantity={n}
- Path params: id
- Query param: quantity (int; negative to reduce)
- Response: success message
- Notes: Admin-only

---

## Cart

Base path: `/api/cart`

### GET /api/cart
- Query params: page, size
- Response: `PageResponse<CartResponse>`
- Notes: Admin (list all carts)

### GET /api/cart/user/{userId}
- Path params: userId
- Response: `CartResponse`
- Notes: Authenticated

### POST /api/cart/user/{userId}/items
- Path params: userId
- Request body: `AddToCartRequest`
- Response: `CartResponse`
- Notes: Authenticated

### PUT /api/cart/user/{userId}/items/{itemId}?quantity={n}
- Path params: userId, itemId
- Query param: quantity
- Response: `CartResponse`
- Notes: Authenticated

### DELETE /api/cart/user/{userId}/items/{itemId}
- Path params: userId, itemId
- Response: `CartResponse`
- Notes: Authenticated

### DELETE /api/cart/user/{userId}
- Path params: userId
- Response: success message
- Notes: Authenticated

### GET /api/cart/user/{userId}/count
- Path params: userId
- Response: Integer (count)
- Notes: Authenticated

### GET /api/cart/count?userId={userId}
- Query param: userId
- Response: Integer (count)
- Notes: Authenticated

---

## Orders

Base path: `/api/orders`

### POST /api/orders
- Request body: `CreateOrderRequest`
- Response: `OrderResponse`
- Notes: Authenticated

### GET /api/orders/{id}
- Path params: id
- Response: `OrderResponse`
- Notes: Authenticated

### GET /api/orders/number/{orderNumber}
- Path params: orderNumber
- Response: `OrderResponse`
- Notes: Authenticated

### GET /api/orders
- Query params: page, size
- Response: `PageResponse<OrderResponse>`
- Notes: Admin (list all orders)

### GET /api/orders/user/{userId}
- Path params: userId
- Query params: page, size
- Response: `PageResponse<OrderResponse>`
- Notes: Authenticated

### GET /api/orders/status/{status}
- Path params: status
- Query params: page, size
- Response: `PageResponse<OrderResponse>`
- Notes: Authenticated

### PATCH /api/orders/{id}/status?status={status}
- Path params: id
- Query param: status
- Response: `OrderResponse`
- Notes: Admin (update order status)

### PATCH /api/orders/{id}/payment-status?paymentStatus={paymentStatus}
- Path params: id
- Query param: paymentStatus
- Response: `OrderResponse`
- Notes: Authenticated

### POST /api/orders/{id}/cancel
- Path params: id
- Response: `OrderResponse`
- Notes: Authenticated

### DELETE /api/orders/{id}
- Path params: id
- Response: success message
- Notes: Admin-only

### GET /api/orders/count
- Response: Long (total orders)
- Notes: Authenticated

### GET /api/orders/count/status/{status}
- Path params: status
- Response: Long
- Notes: Authenticated

### PUT /api/orders/{id}
- Path params: id
- Request body: `UpdateOrderRequest` (update the order)
- Response: `OrderResponse`
- Notes: Authenticated

---

## Reviews

Base path: `/api/reviews`

### POST /api/reviews
- Request body: `CreateReviewRequest`
- Response: `ReviewResponse`
- Notes: Authenticated

### GET /api/reviews/{id}
- Path params: id
- Response: `ReviewResponse`
- Notes: Authenticated

### GET /api/reviews/product/{productId}
- Path params: productId
- Query params: page, size
- Response: `PageResponse<ReviewResponse>`
- Notes: Public

### GET /api/reviews/user/{userId}
- Path params: userId
- Query params: page, size
- Response: `PageResponse<ReviewResponse>`
- Notes: Authenticated

### GET /api/reviews/product/{productId}/average-rating
- Path params: productId
- Response: Double
- Notes: Public

### GET /api/reviews/product/{productId}/count
- Path params: productId
- Response: Long
- Notes: Public

### GET /api/reviews/check?userId={userId}&productId={productId}
- Query params: userId, productId
- Response: Boolean
- Notes: Authenticated

### PUT /api/reviews/{id}
- Request body: `CreateReviewRequest` (used for update)
- Response: `ReviewResponse`
- Notes: Authenticated

### DELETE /api/reviews/{id}
- Path params: id
- Response: success message
- Notes: Authenticated

---

## Addresses

Base path: `/api/addresses`

### POST /api/addresses
- Request body: `CreateAddressRequest`
- Response: `AddressResponse`
- Notes: Authenticated

### GET /api/addresses/{id}
- Path params: id
- Response: `AddressResponse`
- Notes: Authenticated

### GET /api/addresses
- Response: List<AddressResponse>
- Notes: Admin

### GET /api/addresses/user/{userId}
- Path params: userId
- Response: List<AddressResponse>
- Notes: Authenticated

### GET /api/addresses/user/{userId}/shipping
- Path params: userId
- Response: List<AddressResponse>
- Notes: Authenticated

### GET /api/addresses/user/{userId}/billing
- Path params: userId
- Response: List<AddressResponse>
- Notes: Authenticated

### GET /api/addresses/user/{userId}/default
- Path params: userId
- Response: AddressResponse
- Notes: Authenticated

### PUT /api/addresses/{id}
- Path params: id
- Request body: `CreateAddressRequest`
- Response: `AddressResponse`
- Notes: Authenticated

### PATCH /api/addresses/{id}/set-default
- Path params: id
- Response: `AddressResponse`
- Notes: Authenticated

### DELETE /api/addresses/{id}
- Path params: id
- Response: success message
- Notes: Authenticated

---

## Wishlist

Base path: `/api/wishlist`

### POST /api/wishlist
- Request body: `AddToWishlistRequest`
- Response: `WishlistItemResponse`
- Notes: Authenticated

### GET /api/wishlist/user/{userId}
- Path params: userId
- Response: List<WishlistItemResponse>
- Notes: Authenticated

### GET /api/wishlist/user/{userId}/page
- Query params: page, size
- Response: `PageResponse<WishlistItemResponse>`
- Notes: Authenticated

### GET /api/wishlist/user/{userId}/count
- Path params: userId
- Response: Long
- Notes: Authenticated

### GET /api/wishlist/check?userId={userId}&productId={productId}
- Query params: userId, productId
- Response: Boolean
- Notes: Authenticated

### DELETE /api/wishlist/{id}
- Path params: id
- Response: success message
- Notes: Authenticated

### DELETE /api/wishlist/user/{userId}/product/{productId}
- Path params: userId, productId
- Response: success message
- Notes: Authenticated

### DELETE /api/wishlist/user/{userId}
- Path params: userId
- Response: success message
- Notes: Authenticated

### POST /api/wishlist/user/{userId}/product/{productId}/move-to-cart
- Path params: userId, productId
- Response: success message
- Notes: Authenticated

---

## Categories

Base path: `/api/categories`

### POST /api/categories
- Request body: `CreateCategoryRequest`
- Response: `CategoryResponse`
- Notes: Admin

### GET /api/categories/{id}
- Path params: id
- Response: `CategoryResponse`
- Notes: Public

### GET /api/categories
- Response: List<CategoryResponse>
- Notes: Public

### GET /api/categories/root
- Response: List<CategoryResponse>
- Notes: Public

### GET /api/categories/tree
- Response: List<CategoryResponse> (hierarchical)
- Notes: Public

### GET /api/categories/{parentId}/subcategories
- Path params: parentId
- Response: List<CategoryResponse>
- Notes: Public

### PUT /api/categories/{id}
- Request body: `CreateCategoryRequest`
- Response: `CategoryResponse`
- Notes: Admin

### DELETE /api/categories/{id}
- Path params: id
- Response: success message
- Notes: Admin

---

## Home / Health

### GET /
- Response: Map info (name, version, endpoints, timestamp)
- Notes: Public

### GET /health
- Response: Map (status, timestamp)
- Notes: Public

---

## Request / Response DTOs

Below are the request and response DTOs used across the API with their fields. Use these for building frontend request payloads and parsing responses.

### CreateUserRequest
- emailAddress: string
- firstName?: string
- lastName?: string
- phoneNumber?: string
- password: string

### LoginRequest
- email: string
- password: string

### RefreshTokenRequest
- refreshToken: string

### AuthResponse
- accessToken: string
- refreshToken: string
- tokenType: string
- expiresIn: number
- userId: UUID
- email?: string
- firstName?: string
- lastName?: string
- roles?: string[]

### UserResponse
- id: UUID
- emailAddress: string
- firstName?: string
- lastName?: string
- fullName?: string
- phoneNumber?: string
- isActive?: boolean
- createdAt?: OffsetDateTime (ISO 8601)
- updatedAt?: OffsetDateTime (ISO 8601)
- roles?: string[]

### UpdateRolesRequest
- roles: string[]

### CreateProductRequest
- categoryId: UUID
- sku?: string
- name: string
- description?: string
- price: number (decimal)
- stockQuantity?: number
- isActive?: boolean
- images?: string[]

### ProductResponse
- id: UUID
- categoryId: UUID
- categoryName?: string
- sku?: string
- name: string
- description?: string
- price: number
- stockQuantity?: number
- isActive?: boolean
- inStock?: boolean
- images?: string[]
- primaryImage?: string
- averageRating?: number
- reviewCount?: number
- createdAt?: OffsetDateTime
- updatedAt?: OffsetDateTime

### AddToCartRequest
- productId: UUID
- quantity: number

### CartResponse
- id: UUID
- userId: UUID
- totalItems: number
- totalValue: decimal
- createdAt: OffsetDateTime
- items: CartItemResponse[]

CartItemResponse
- id: UUID
- productId: UUID
- productName: string
- productImage?: string
- unitPrice: decimal
- quantity: number
- subtotal: decimal
- inStock?: boolean
- availableStock?: number
- addedAt?: OffsetDateTime

### CreateOrderRequest
- userId: UUID
- paymentMethodId?: UUID
- shippingAddressId?: UUID
- shippingMethodId?: UUID
- customerNotes?: string
- items: OrderItemRequest[]

OrderItemRequest
- productId: UUID
- quantity: number

### OrderResponse
- id: UUID
- userId: UUID
- customerName?: string
- orderNumber?: string
- status?: string
- paymentStatus?: string
- subtotal?: decimal
- shippingCost?: decimal
- total?: decimal
- itemCount?: number
- customerNotes?: string
- createdAt?: OffsetDateTime
- cancelledAt?: OffsetDateTime
- items: OrderItemResponse[]
- shippingAddress: ShippingAddressResponse
- shippingMethod: ShippingMethodResponse

OrderItemResponse
- id: UUID
- productId: UUID
- productName?: string
- productSku?: string
- unitPrice?: decimal
- quantity?: number
- totalPrice?: decimal

ShippingAddressResponse
- id: UUID
- addressLine?: string
- city?: string
- region?: string
- country?: string
- postalCode?: string
- fullAddress?: string

ShippingMethodResponse
- id: UUID
- name?: string
- price?: decimal
- estimatedDelivery?: string

### UpdateOrderRequest
- paymentMethodId?: UUID
- shippingAddressId?: UUID
- shippingMethodId?: UUID
- customerNotes?: string
- items?: OrderItemUpdateRequest[]

OrderItemUpdateRequest
- id?: UUID
- productId?: UUID
- quantity?: number

### CreateReviewRequest
- productId: UUID
- userId: UUID
- rating: number (1-5)
- title?: string
- comment?: string

### ReviewResponse
- id: UUID
- productId: UUID
- userId: UUID
- userName?: string
- rating: integer
- title?: string
- comment?: string
- createdAt?: OffsetDateTime
- updatedAt?: OffsetDateTime

### CreateAddressRequest
- userId: UUID
- addressLine?: string
- city: string
- region?: string
- country: string
- postalCode?: string
- isDefault?: boolean
- addressType?: string ("shipping" or "billing")

### AddressResponse
- id: UUID
- userId: UUID
- addressLine?: string
- city: string
- region?: string
- country: string
- postalCode?: string
- isDefault?: boolean
- addressType?: string
- createdAt?: OffsetDateTime
- fullAddress?: string

### AddToWishlistRequest
- userId: UUID
- productId: UUID

### WishlistItemResponse
- id: UUID
- userId: UUID
- productId: UUID
- productName?: string
- productSku?: string
- productPrice?: decimal
- productImage?: string
- productInStock?: boolean
- createdAt?: OffsetDateTime

### CreateCategoryRequest
- parentCategoryId?: UUID
- categoryName: string

### CategoryResponse
- id: UUID
- parentCategoryId?: UUID
- categoryName: string
- parentCategoryName?: string
- subCategories?: CategoryResponse[]
- productCount?: number

---

## Notes & Conventions
- All responses are wrapped in `ApiResponse<T>` (see `com.miracle.smart_ecommerce_api_v1.common.response.ApiResponse`).
- Paging responses use `PageResponse<T>`.
- Auth-protected endpoints require `Authorization: Bearer <token>` header.
- Admin-only endpoints are protected with `hasRole('ADMIN')`.
- DTO classes are under `domain/*/dto` packages (look for `*Request` and `*Response` classes).

If you want I can:
- Generate a Postman collection JSON for these endpoints.
- Export a narrower MD file only for admin panel actions.
- Add OpenAPI documentation customization._
