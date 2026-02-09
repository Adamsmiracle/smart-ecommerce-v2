# Order Update Endpoint (Frontend Reference)

This document contains everything the frontend needs to successfully call the Order Update endpoint (PUT /api/orders/{id}) in this project.

---

## Endpoint (exact)
- Method: PUT
- URL (development): `http://localhost:8080/api/orders/{orderId}`
  - Replace `{orderId}` with the order UUID.
  - Example: `http://localhost:8080/api/orders/1fc1e8e5-0471-4684-b732-d3eb71284861`
- Controller signature: `public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(@PathVariable UUID id, @Valid @RequestBody UpdateOrderRequest request)`

---

## Required request headers
- `Content-Type: application/json`
- `Authorization: Bearer <ACCESS_TOKEN>` (if your deployment requires authentication)
- `Accept: application/json` (optional)

---

## Request DTO (Java) — `UpdateOrderRequest`
- File: `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/order/dto/UpdateOrderRequest.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderRequest {
    private UUID paymentMethodId;
    private UUID shippingAddressId;
    private UUID shippingMethodId;

    @Size(max = 1000)
    private String customerNotes;

    // Optional: list of item updates. If omitted, items remain unchanged.
    private List<OrderItemUpdateRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemUpdateRequest {
        // existing order_item id (optional) — provide to update/delete
        private UUID id;
        // product id for new items (optional)
        private UUID productId;
        // new quantity (>=1 to add/update; 0 or <=0 to delete existing item)
        @Min(1)
        private Integer quantity;
    }
}
```

---

## How the `items` list is interpreted
- Add new item: include an object with `productId` and `quantity` (no `id`).
- Update existing item: provide the existing `id` (order_item id) and `quantity` (>=1).
- Delete existing item: provide the existing `id` and `quantity: 0` (or <= 0).
- If `items` is omitted or null, the order items are left unchanged.

Notes: the backend aggregates stock deltas and applies them atomically; increasing quantity reduces stock, deleting/restoring items increases stock.

---

## Example JSON payloads

Metadata-only update (notes):
```json
{ "customerNotes": "Leave at the back door" }
```

Add a new item:
```json
{ "items": [ { "productId": "c0000000-0000-0000-0000-000000000003", "quantity": 2 } ] }
```

Update existing item quantity:
```json
{ "items": [ { "id": "11111111-2222-3333-4444-555555555555", "quantity": 3 } ] }
```

Delete an existing item:
```json
{ "items": [ { "id": "11111111-2222-3333-4444-555555555555", "quantity": 0 } ] }
```

Mixed update (update, delete, add):
```json
{
  "shippingAddressId":"3fd194a4-6b9e-4a81-8fde-6f305dbff4e1",
  "items": [
    { "id": "11111111-2222-3333-4444-555555555555", "quantity": 2 },
    { "id": "22222222-3333-4444-5555-666666666666", "quantity": 0 },
    { "productId": "c0000000-0000-0000-0000-000000000004", "quantity": 1 }
  ]
}
```

---

## Example client calls

curl (with auth):

```bash
curl -i -X PUT "http://localhost:8080/api/orders/1fc1e8e5-0471-4684-b732-d3eb71284861" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -d '{"items":[{"productId":"c0000000-0000-0000-0000-000000000003","quantity":2}],"customerNotes":"Please deliver in evening"}'
```

fetch (browser):

```javascript
const body = { items: [{ productId: 'c0000000-0000-0000-0000-000000000003', quantity: 2 }] };
fetch(`http://localhost:8080/api/orders/${orderId}`, {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify(body)
}).then(r => r.json()).then(console.log).catch(console.error);
```

---

## Expected successful response
- HTTP status: 200 OK
- Body: ApiResponse<OrderResponse> — `data` contains the updated `OrderResponse` with recalculated totals and `items[]`.

Important fields in `OrderResponse`:
- id, userId, orderNumber, status, paymentStatus
- subtotal, shippingCost, total, itemCount
- customerNotes
- createdAt, cancelledAt
- items: array of { id, productId, productName, productSku, unitPrice, quantity, totalPrice }

---

## Error responses you must handle
- 400 Bad Request — validation problems (invalid UUID, missing productId for new item, invalid quantity, insufficient stock)
- 401 / 403 — unauthorized / forbidden (include proper token)
- 404 Not Found — order, product, or address not found
- 409 Conflict — business conflicts (not currently used but possible)
- 500 Internal Server Error — generic failure (check `data.detail` for correlationId)

All errors return the standard `ApiResponse` wrapper with `status:false` and `data` containing errorCode, message, detail, path and correlationId.

---

## Where to get IDs (practical frontend steps)
- `orderId`: from the page context or `GET /api/orders/user/{userId}` or `GET /api/orders/{id}`.
- existing item `id`s: call `GET /api/orders/{orderId}` — look at `items[].id`.
- `productId`: from product listing (`GET /api/products`) or product detail endpoint.
- `shippingAddressId`: from `GET /api/addresses/user/{userId}`.

Always fetch the latest `GET /api/orders/{orderId}` before update to get current item ids and quantities.

---

## Runtime semantics & frontend implications (short)
- Update is transactional: stock changes, item deletes/inserts and order update are atomic.
- Stock behavior:
  - Increasing quantity reduces available stock.
  - Decreasing quantity or deleting an item restores stock.
- Persistence approach: server deletes all existing items for the order and re-inserts the resulting `items` list — new items will receive new `id`s.
- Concurrency: parallel edits may race on stock. Consider locking or optimistic checks if frontend attempts concurrent updates.
- Validation: send correct `id`s and `productId`s; server will validate existence.

---

## TypeScript interfaces (handy copy-paste)

Request (TS):
```ts
interface UpdateOrderRequest {
  paymentMethodId?: string;
  shippingAddressId?: string;
  shippingMethodId?: string;
  customerNotes?: string;
  items?: Array<{ id?: string; productId?: string; quantity?: number }>;
}
```

OrderResponse (partial):
```ts
interface OrderResponse {
  id: string;
  userId: string;
  orderNumber: string;
  status: string;
  paymentStatus: string;
  subtotal: number;
  shippingCost: number;
  total: number;
  itemCount: number;
  customerNotes?: string;
  createdAt?: string;
  cancelledAt?: string | null;
  items: Array<{ id: string; productId: string; productName: string; productSku?: string; unitPrice: number; quantity: number; totalPrice: number }>;
}
```

---

If you want, I can also:
- Add a Postman collection entry for this endpoint.
- Provide an axios helper function the frontend can import.
- Add server-side validation to ensure a `shippingAddressId` belongs to the order's user.

File created at project root: `ORDER_UPDATE_ENDPOINT.md`.

