# Cart API Endpoints

This document lists every Shopping Cart endpoint in the project, with method, path, parameters, request/response shapes, example requests, and common errors.

Base URL (dev): `http://localhost:8080`

---

## Summary (quick)
- GET  /api/cart                             — list all carts (paginated, admin)
- GET  /api/cart/user/{userId}               — get user's cart
- POST /api/cart/user/{userId}/items         — add item to cart
- PUT  /api/cart/user/{userId}/items/{itemId}?quantity={n} — update item quantity
- DELETE /api/cart/user/{userId}/items/{itemId} — remove item from cart
- DELETE /api/cart/user/{userId}            — clear user's cart
- GET  /api/cart/user/{userId}/count        — get cart item count (path)
- GET  /api/cart/count?userId={userId}      — get cart item count (query)

---

## 1. Get all carts (admin)
- Method: GET
- Path: `/api/cart`
- Query params:
  - `page` (int, optional, default 0)
  - `size` (int, optional, default 10)
- Request body: none
- Response: ApiResponse<PageResponse<CartResponse>>
- Success: 200 OK

Example curl:
```
curl -i "http://localhost:8080/api/cart?page=0&size=10"
```

---

## 2. Get user's cart
- Method: GET
- Path: `/api/cart/user/{userId}`
- Path params: `userId` (UUID)
- Request body: none
- Response: ApiResponse<CartResponse>
- Success: 200 OK

Example curl:
```
curl -i "http://localhost:8080/api/cart/user/550e8400-e29b-41d4-a716-446655440000"
```

---

## 3. Add item to cart
- Method: POST
- Path: `/api/cart/user/{userId}/items`
- Path params: `userId` (UUID)
- Request body (JSON) — AddToCartRequest:
  - `productId` (UUID) — required
  - `quantity` (integer >= 1) — required
- Response: ApiResponse<CartResponse> (updated cart)
- Success: 200 OK

Example curl:
```
curl -i -X POST "http://localhost:8080/api/cart/user/550e8400-e29b-41d4-a716-446655440000/items" \
  -H "Content-Type: application/json" \
  -d '{"productId":"c0000000-0000-0000-0000-000000000001","quantity":2}'
```

Example fetch (frontend):
```js
fetch(`http://localhost:8080/api/cart/user/${userId}/items`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ productId, quantity })
})
.then(res => res.json())
.then(console.log)
.catch(console.error);
```

Possible errors:
- 400 Bad Request — validation (missing fields, quantity < 1)
- 404 Not Found — user or product not found
- 409 Conflict — business rule (if implemented)
- 401/403 — authentication/authorization if endpoint protected

---

## 4. Update cart item quantity
- Method: PUT
- Path: `/api/cart/user/{userId}/items/{itemId}`
- Path params: `userId` (UUID), `itemId` (UUID)
- Query param: `quantity` (int) — required
- Request body: none
- Response: ApiResponse<CartResponse>
- Success: 200 OK

Example curl:
```
curl -i -X PUT "http://localhost:8080/api/cart/user/550e8400-e29b-41d4-a716-446655440000/items/11111111-1111-1111-1111-111111111111?quantity=3"
```

Example fetch:
```js
fetch(`http://localhost:8080/api/cart/user/${userId}/items/${itemId}?quantity=3`, { method: 'PUT' })
  .then(r=>r.json()).then(console.log)
```

---

## 5. Remove item from cart
- Method: DELETE
- Path: `/api/cart/user/{userId}/items/{itemId}`
- Path params: `userId`, `itemId` — UUIDs
- Response: ApiResponse<CartResponse> (updated cart)
- Success: 200 OK

Example curl:
```
curl -i -X DELETE "http://localhost:8080/api/cart/user/550e8400-e29b-41d4-a716-446655440000/items/11111111-1111-1111-1111-111111111111"
```

---

## 6. Clear cart
- Method: DELETE
- Path: `/api/cart/user/{userId}`
- Path param: `userId` (UUID)
- Response: ApiResponse<Void> (message)
- Success: 200 OK

Example curl:
```
curl -i -X DELETE "http://localhost:8080/api/cart/user/550e8400-e29b-41d4-a716-446655440000"
```

---

## 7. Get cart item count (path)
- Method: GET
- Path: `/api/cart/user/{userId}/count`
- Path param: `userId` (UUID)
- Response: ApiResponse<Integer> (number of items)
- Success: 200 OK

Example curl:
```
curl -i "http://localhost:8080/api/cart/user/550e8400-e29b-41d4-a716-446655440000/count"
```

---

## 8. Get cart item count (query)
- Method: GET
- Path: `/api/cart/count`
- Query param: `userId` (UUID) — required
- Response: ApiResponse<Integer>
- Success: 200 OK

Example curl:
```
curl -i "http://localhost:8080/api/cart/count?userId=550e8400-e29b-41d4-a716-446655440000"
```

Notes: this query endpoint prevents ambiguity when callers use `/api/cart/count` directly (it accepts `?userId=...`). Make sure your frontend passes the actual UUID (not the literal word `count`).

---

## Response DTOs (shapes)

### AddToCartRequest (request body for POST)
```json
{
  "productId": "uuid",
  "quantity": 2
}
```

### CartResponse (returned by many endpoints)
```json
{
  "id": "uuid",
  "userId": "uuid",
  "totalItems": 3,
  "totalValue": 159.97,
  "createdAt": "2026-02-09T09:01:46.980101800Z",
  "items": [
    {
      "id": "uuid",
      "productId": "uuid",
      "productName": "string",
      "productImage": "url",
      "unitPrice": 29.99,
      "quantity": 2,
      "subtotal": 59.98,
      "inStock": true,
      "availableStock": 100,
      "addedAt": "2026-02-09T08:24:15.34893Z"
    }
  ]
}
```

---

## Common errors and tips
- 400 Bad Request: invalid UUID format, missing fields, or type mismatch (example: sending `userId=count` instead of a UUID).
- 401/403: authentication/authorization failures (include `Authorization: Bearer <token>` if required).
- 404 Not Found: user/product/cart item not found.
- 409 Conflict: duplicate or business rule violation.

Frontend tips
- Use the full backend URL in development (e.g., `http://localhost:8080`) or configure a dev proxy to avoid CORS.
- For endpoints using query params, pass the actual UUID value. Do not pass the literal string `count`.
- When sending JSON, set `Content-Type: application/json` and serialize the object.
- If using cookies, set `credentials: 'include'` and ensure backend CORS allows credentials and the origin.

---

If you want, I can:
- Add example TypeScript interfaces for the request/response shapes.
- Add a short Postman collection file (JSON) with these endpoints.
- Add small sample frontend helper functions (axios instance + calls).

File created at project root: `CART_ENDPOINTS.md`.

