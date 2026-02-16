# Product API Reference

Base URL: `http://localhost:8080` (replace with your deployment host/port)

All endpoints use `Content-Type: application/json` unless stated otherwise.

---

## Endpoints

### 1) Create product
- Method: POST
- URL: `/api/products`
- Auth: (typically admin)
- Request DTO: `CreateProductRequest`
  - Fields:
    - `categoryId` (UUID, required)
    - `name` (string, required)
    - `description` (string, optional)
    - `price` (number/string, required) — decimal, e.g. 19.99
    - `stockQuantity` (integer, optional)
    - `isActive` (boolean, optional)
    - `images` (array of string URLs, optional)
- Example request body:

```json
{
  "categoryId": "11111111-1111-1111-1111-111111111111",
  "name": "Acme Widget",
  "description": "Handy widget",
  "price": 19.99,
  "stockQuantity": 100,
  "isActive": true,
  "images": ["https://cdn.example.com/img1.jpg"]
}
```

- Success response: 201 Created
  - Body: `ApiResponse<ProductResponse>`

Example ProductResponse fields (exactly as defined in `ProductResponse.java`):
- `id` (UUID)
- `categoryId` (UUID)
- `name` (string)
- `description` (string)
- `price` (decimal)
- `stockQuantity` (int)
- `isActive` (boolean)
- `inStock` (boolean)
- `images` (array of strings)
- `createdAt` (ISO OffsetDateTime)
- `updatedAt` (ISO OffsetDateTime)

Note: fields such as `primaryImage`, `sku`, `averageRating`, `reviewCount`, and `categoryName` are not present in the `ProductResponse` DTO in the codebase. Some of those values can be derived from the `Product` entity (for example `primaryImage` can be obtained from `product.getPrimaryImage()` which returns the first entry in `images`) or computed in service mapping — but unless those fields are explicitly added to `ProductResponse.java` and populated in the mapping, they will not be returned by the API. If you want them available to the frontend, we should add them to the DTO and set them in the service `mapToResponse` method.

---

### 2) Get product by ID
- Method: GET
- URL: `/api/products/{id}`
- Params: Path `id` (UUID)
- Success: 200 OK, `ApiResponse<ProductResponse>`
- Errors: 404 if not found

Example URL:
```
GET http://localhost:8080/api/products/0c6ff99a-d894-48c7-a8c6-194413146892
```


### 4) Get all products (paginated)
- Method: GET
- URL: `/api/products`
- Query params:
  - `page` (int, default 0)
  - `size` (int, default 10)
- Success: 200 OK, `ApiResponse<PageResponse<ProductResponse>>`

PageResponse contains: `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `first`, `last`, etc.

Example:
```
GET /api/products?page=0&size=20
```

---

### 5) Get active products (paginated)
- Method: GET
- URL: `/api/products/active`
- Query params: `page`, `size`
- Success: 200 OK, `ApiResponse<PageResponse<ProductResponse>>`

---

### 6) Get products by category
- Method: GET
- URL: `/api/products/category/{categoryId}`
- Params: Path `categoryId` (UUID)
- Query params: `page`, `size`
- Success: 200 OK, `ApiResponse<PageResponse<ProductResponse>>`

---

### 7) Search products
- Method: GET
- URL: `/api/products/search`
- Query params:
  - `keyword` (string, required)
  - `page`, `size`
- Success: 200 OK, `ApiResponse<PageResponse<ProductResponse>>`

Example:
```
GET /api/products/search?keyword=widget&page=0&size=10
```

---

### 8) Get products by price range
- Method: GET
- URL: `/api/products/price-range`
- Query params:
  - `minPrice` (decimal, required)
  - `maxPrice` (decimal, required)
  - `page`, `size`
- Success: 200 OK, `ApiResponse<PageResponse<ProductResponse>>`

Example:
```
GET /api/products/price-range?minPrice=10.00&maxPrice=50.00&page=0&size=10
```

---

### 9) Get products in stock
- Method: GET
- URL: `/api/products/in-stock`
- Query params: `page`, `size`
- Success: 200 OK, `ApiResponse<PageResponse<ProductResponse>>`

---

### 10) Update product
- Method: PUT
- URL: `/api/products/{id}`
- Params: Path `id` (UUID)
- Request DTO: `UpdateProductRequest` (all fields optional)
  - `categoryId`, `name`, `description`, `price`, `stockQuantity`, `isActive`, `images`
- Success: 200 OK, `ApiResponse<ProductResponse>`
- Errors: 404 if product or category not found

Example update body (partial):
```json
{
  "price": 24.99,
  "stockQuantity": 75
}
```

---

### 11) Delete product
- Method: DELETE
- URL: `/api/products/{id}`
- Success: 200 OK, `ApiResponse<Void>` (message on success)

---

### 12) Activate / Deactivate product
- Method: POST
- URL: `/api/products/{id}/activate`
- URL: `/api/products/{id}/deactivate`
- Success: 200 OK, `ApiResponse<Void>` (message)

---

### 13) Update stock
- Method: PATCH
- URL: `/api/products/{id}/stock`
- Query param: `quantity` (int, positive to add, negative to subtract)
- Success: 200 OK, `ApiResponse<Void>`

Example:
```
PATCH /api/products/0c6ff99a-d8.../stock?quantity=-1
```

---

## DTO quick reference

### CreateProductRequest
- `categoryId` UUID (required)
- `name` string (required)
- `description` string
- `price` decimal
- `stockQuantity` integer
- `isActive` boolean
- `images` string[]

### UpdateProductRequest
- All fields optional: `categoryId`, `name`, `description`, `price`, `stockQuantity`, `isActive`, `images`

### ProductResponse (exact fields from DTO)
- `id` UUID
- `categoryId` UUID
- `name` string
- `description` string
- `price` decimal
- `stockQuantity` int
- `isActive` boolean
- `inStock` boolean
- `images` string[]
- `createdAt` string (ISO OffsetDateTime)
- `updatedAt` string (ISO OffsetDateTime)

---

## Errors & status codes
- 200 OK — success for GET/PUT/DELETE/PATCH where applicable
- 201 Created — success for POST create
- 400 Bad Request — validation errors
- 401 Unauthorized — if endpoint requires auth and client is not authenticated
- 403 Forbidden — if client lacks necessary permissions
- 404 Not Found — resource doesn't exist
- 500 Internal Server Error — unexpected server error


## Notes for frontend
- Use `page` (0-based) and `size` for paging.
- `price` should be sent as a number (decimal) or string with 2 decimal places.
- `images` is an array stored server-side as JSONB — you can send list of URLs.
- Admin-only actions: creating/updating/deleting/activating products — ensure the frontend only exposes these controls to admin users.
- Dates are returned as ISO OffsetDateTime strings (e.g. `2026-02-12T09:00:00+00:00`).
