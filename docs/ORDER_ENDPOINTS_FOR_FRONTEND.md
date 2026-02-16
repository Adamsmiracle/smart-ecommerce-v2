# Orders API Reference (Frontend)

Base URL: `http://localhost:8080` (replace with your deployment host/port)

All endpoints use `Content-Type: application/json`.

This document reflects the current database schema for `customer_order` and `order_item` and the DTOs the frontend should send/expect.

---

## Summary of database fields (for reference)
- customer_order:
  - `id` UUID
  - `user_id` UUID
  - `order_number` VARCHAR(50) UNIQUE
  - `status` VARCHAR(50) (default `pending`)
  - `payment_method_id` UUID (nullable)
  - `shipping_method_id` UUID (nullable)
  - `payment_status` VARCHAR(30) (nullable)
  - `subtotal` NUMERIC(10,2)
  - `total_amount` NUMERIC(10,2)
  - `created_at` TIMESTAMP (use OffsetDateTime in API)
  - `updated_at` TIMESTAMP (use OffsetDateTime in API)

- order_item:
  - `id` UUID
  - `order_id` UUID
  - `product_id` UUID
  - `unit_price` NUMERIC(10,2)
  - `quantity` INT

---

# Endpoints

All endpoints are under the prefix `/api/orders`.

### 1) Create order
- Method: POST
- URL: `/api/orders`
- Purpose: Create a new order with items. Backend will calculate `subtotal` and `total_amount` from provided items (unitPrice × quantity per item) and any shipping cost if `shippingMethodId` is set.

Request DTO: `CreateOrderRequest`
- Fields (JSON):
  - `userId` (string UUID) — required unless server determines user from session
  - `paymentMethodId` (string UUID) — optional
  - `shippingMethodId` (string UUID) — optional
  - `items` (array of OrderItemRequest) — required, at least one item

OrderItemRequest:
- `productId` (string UUID) — required
- `quantity` (integer) — required, > 0
- `unitPrice` (decimal) — optional: if omitted, backend must fetch current product price and use it. If client supplies unitPrice, backend may validate it.

Example request body:
```json
{
  "userId": "0c6ff99a-d894-48c7-a8c6-194413146892",
  "paymentMethodId": "a1111111-1111-1111-1111-111111111111",
  "shippingMethodId": "b2222222-2222-2222-2222-222222222222",
  "items": [
    { "productId": "c0000000-0000-0000-0000-000000000001", "quantity": 2 },
    { "productId": "c0000000-0000-0000-0000-000000000002", "quantity": 1 }
  ]
}
```

Success response:
- Status: 201 Created
- Body: `OrderResponse` (see DTO below)

Errors:
- 400 Bad Request — validation errors (missing fields, invalid UUIDs, quantity ≤ 0)
- 404 Not Found — product/payment/shipping not found
- 409 Conflict — order number duplication (rare)

---

### 2) Get order by ID
- Method: GET
- URL: `/api/orders/{orderId}`
- Params: Path `orderId` (UUID)
- Success: 200 OK with `OrderResponse`
- Errors: 404 if not found

Example:
```
GET http://localhost:8080/api/orders/1fc1e8e5-0471-4684-b732-d3eb71284861
```

---

### 3) Get orders for user
- Method: GET
- URL: `/api/orders/user/{userId}`
- Params: Path `userId` (UUID)
- Query params: `page` (int), `size` (int)
- Success: 200 OK with paginated `PageResponse<OrderResponse>`
- Errors: 400/404 on invalid input

---

### 4) Get all orders (admin)
- Method: GET
- URL: `/api/orders`
- Query params: `page`, `size`, optional filters like `status`, `paymentStatus`
- Success: 200 OK with `PageResponse<OrderResponse>`
- Errors: 403 if unauthorized

---

### 5) Update order (update order fields and items)
- Method: PUT
- URL: `/api/orders/{orderId}`
- Purpose: Update order header fields (status, payment/shipping method/status) and update order items.

Request DTO: `UpdateOrderRequest` (partial update allowed)
- Fields (all optional except items when replacing):
  - `status` (string)
  - `paymentMethodId` (UUID)
  - `shippingMethodId` (UUID)
  - `paymentStatus` (string)
  - `items` (array of UpdateOrderItemRequest) — optional; when provided the backend will reconcile items (see contract below)

UpdateOrderItemRequest shapes supported (choose one approach for the API):
- For item update/creation:
  - `id` (UUID) — optional: if present, update existing item (quantity/unitPrice)
  - `productId` (UUID) — required for creating a new item
  - `quantity` (int) — required
  - `unitPrice` (decimal) — optional

Behavior contract (must be respected by backend):
- If `items` array is provided, backend will treat it as the new authoritative set for the order. Implementation options:
  1) Replace strategy (recommended): remove existing `order_item` rows not present in the new array, update rows with matching `id` (or productId), and insert new rows. Recalculate `subtotal` and `total_amount` after applying items and shipping.
  2) Merge strategy: update/insert items, do not remove items unless explicitly flagged for deletion (less convenient for frontend).

Important: The frontend must know which strategy the backend implements. The recommended is Replace strategy. Example Update request (replace items):
```json
{
  "status": "processing",
  "shippingMethodId": "b2222222-2222-2222-2222-222222222222",
  "items": [
    { "productId": "c0000000-0000-0000-0000-000000000001", "quantity": 3 },
    { "productId": "c0000000-0000-0000-0000-000000000003", "quantity": 1 }
  ]
}
```

Success response:
- 200 OK with updated `OrderResponse` body

Errors:
- 400 Bad Request for validation errors
- 404 Not Found if order/product/payment/shipping not found

---

### 6) Delete order
- Method: DELETE
- URL: `/api/orders/{orderId}`
- Success: 200 OK (or 204 No Content)
- Effect: Deletes order and cascades `order_item` rows (DB schema enforces ON DELETE CASCADE)

---

### 7) Get items for an order
- Method: GET
- URL: `/api/orders/{orderId}/items`
- Success: 200 OK with an array of `OrderItemResponse`

---

### 8) Add or update a single item on an order (optional)
- Method: POST
- URL: `/api/orders/{orderId}/items`
- Body: `OrderItemRequest` (productId, quantity, optional unitPrice)
- Behavior: creates a new order_item or updates existing line (behavior should be agreed; prefer PUT on order for full updates)

---

# DTOs / Request & Response shapes

## CreateOrderRequest
```json
{
  "userId": "string (UUID)",
  "paymentMethodId": "string (UUID) | null",
  "shippingMethodId": "string (UUID) | null",
  "items": [
    { "productId": "string (UUID)", "quantity": int, "unitPrice": decimal? }
  ]
}
```

## UpdateOrderRequest
```json
{
  "status": "string (optional)",
  "paymentMethodId": "string (UUID) (optional)",
  "shippingMethodId": "string (UUID) (optional)",
  "paymentStatus": "string (optional)",
  "items": [
    { "id": "string(UUID) (optional if updating existing)", "productId": "UUID", "quantity": int, "unitPrice": decimal? }
  ]
}
```

## OrderItemRequest (used within create/update)
```json
{
  "productId": "string UUID",
  "quantity": int,
  "unitPrice": decimal (optional)
}
```

## OrderItemResponse
```json
{
  "id": "UUID",
  "orderId": "UUID",
  "productId": "UUID",
  "unitPrice": decimal,
  "quantity": int
}
```

## OrderResponse
```json
{
  "id": "UUID",
  "orderNumber": "string",
  "userId": "UUID",
  "status": "string",
  "paymentMethodId": "UUID | null",
  "shippingMethodId": "UUID | null",
  "paymentStatus": "string | null",
  "subtotal": decimal,
  "totalAmount": decimal,
  "items": [ OrderItemResponse ],
  "createdAt": "ISO OffsetDateTime string",
  "updatedAt": "ISO OffsetDateTime string"
}
```

Notes:
- `subtotal` is sum(unit_price * quantity) calculated from order items.
- `totalAmount` = `subtotal` + shipping cost (if shipping method has `price`) + any taxes/fees (if applicable).
- `unitPrice` should reflect the price used at the time of order and may differ from current product price.

---

# Frontend integration details
- Exact URLs:
  - Create: `POST http://localhost:8080/api/orders`
  - Get by id: `GET http://localhost:8080/api/orders/{orderId}`
  - Get user orders: `GET http://localhost:8080/api/orders/user/{userId}?page=0&size=10`
  - Update: `PUT http://localhost:8080/api/orders/{orderId}`
  - Delete: `DELETE http://localhost:8080/api/orders/{orderId}`
  - Get items: `GET http://localhost:8080/api/orders/{orderId}/items`

- Headers: `Content-Type: application/json`
- Dates are ISO OffsetDateTime (e.g. `2026-02-12T09:00:00+00:00`)
- UUIDs are strings.
- When updating items, the frontend should send the authoritative list (Replace strategy) unless backend implements a merge API. After update the response will include recalculated `subtotal` and `totalAmount`.

---

# Validation & error handling (frontend expectations)
- Validate locally before sending: quantities > 0, valid UUID format for id fields.
- Handle server errors gracefully:
  - 400 -> show validation messages
  - 401/403 -> show auth error (login / insufficient permissions)
  - 404 -> show not found message
  - 409 -> conflict
  - 500 -> generic server error

---

If you want, I can:
- Generate example cURL commands for every endpoint.
- Implement the controller request DTO classes in the codebase and a robust `PUT /api/orders/{id}` implementation that reconciles `order_item` rows (replace strategy) and recalculates totals.
- Add OpenAPI annotations to the existing controllers so the frontend can import the spec.

Tell me which of those you'd like me to do next and I will implement it.

