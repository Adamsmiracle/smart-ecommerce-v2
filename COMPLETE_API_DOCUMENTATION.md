# Smart E-Commerce API - Complete Endpoint Documentation

## Overview

This document provides comprehensive documentation for all REST and GraphQL endpoints in the Smart E-Commerce API v1. The API is built with Spring Boot and provides full e-commerce functionality including user management, product catalog, shopping cart, order processing, reviews, and more.

**Base URL**: `http://localhost:8080`
**GraphQL Endpoint**: `/graphql`
**Swagger UI**: `/swagger-ui.html`

---

## Response Format

All REST endpoints return responses in a standardized `ApiResponse` format that wraps the actual entity data:

### Standard ApiResponse Format
```json
{
  "status": true,
  "message": "Request successful",
  "data": { 
    // Entity response data goes here
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "statusCode": 200,
  "path": "/api/endpoint",
  "errors": [] // Only present for validation errors
}
```

### Combined Response Examples

#### User Entity Response
```json
{
  "status": true,
  "message": "User created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "emailAddress": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "isActive": true,
    "role": "CUSTOMER",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "statusCode": 201
}
```

#### Product Entity Response
```json
{
  "status": true,
  "message": "Product retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "categoryId": "550e8400-e29b-41d4-a716-446655440002",
    "name": "Wireless Headphones",
    "description": "High-quality wireless headphones with noise cancellation",
    "price": 199.99,
    "stockQuantity": 50,
    "isActive": true,
    "images": ["headphones1.jpg", "headphones2.jpg"],
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "statusCode": 200
}
```

#### Order Entity Response
```json
{
  "status": true,
  "message": "Order placed successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440003",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "orderNumber": "ORD-20240101-001",
    "status": "pending",
    "paymentMethodId": "550e8400-e29b-41d4-a716-446655440004",
    "shippingMethodId": "550e8400-e29b-41d4-a716-446655440005",
    "paymentStatus": "pending",
    "subtotal": 199.99,
    "total": 219.99,
    "createdAt": "2024-01-01T00:00:00Z",
    "cancelledAt": null
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "statusCode": 201
}
```

### Paginated Responses

Endpoints that return lists use `PageResponse` format wrapped in `ApiResponse`:

```json
{
  "status": true,
  "message": "Products retrieved successfully",
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "name": "Wireless Headphones",
        "price": 199.99,
        "stockQuantity": 50,
        "isActive": true
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "statusCode": 200
}
```

---

## REST API ENDPOINTS

## 1. Home & Health Check

### GET `/`
Returns welcome message and API information.

**Response:**
```json
{
  "status": true,
  "data": {
    "name": "Smart E-Commerce API",
    "version": "1.0.0",
    "description": "A production-ready e-commerce REST API using raw JDBC",
    "timestamp": "2024-01-01T00:00:00Z",
    "endpoints": {
      "users": "/api/users",
      "products": "/api/products",
      "categories": "/api/categories",
      "cart": "/api/cart",
      "orders": "/api/orders",
      "graphql": "/graphql",
      "swagger-ui": "/swagger-ui.html",
      "api-docs": "/v3/api-docs"
    }
  }
}
```

### GET `/health`
Returns API health status.

**Response:**
```json
{
  "status": true,
  "data": {
    "status": "UP",
    "timestamp": "2024-01-01T00:00:00Z"
  }
}
```

---

## 2. Authentication

### POST `/api/auth/authenticate`
Authenticate user with email and password.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "status": true,
  "data": {
    "userId": "uuid",
    "role": "CUSTOMER"
  }
}
```

### POST `/api/auth/register`
Register a new user.

**Request:**
```json
{
  "emailAddress": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "password": "password123",
  "role": "CUSTOMER"
}
```

**Response:**
```json
{
  "status": true,
  "data": {
    "userId": "uuid",
    "role": "CUSTOMER"
  }
}
```

---

## 3. Users

### POST `/api/users`
Create a new user.

**Request:**
```json
{
  "emailAddress": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "password": "password123"
}
```

**Response:**
```json
{
  "status": true,
  "data": {
    "id": "uuid",
    "emailAddress": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "isActive": true,
    "role": "CUSTOMER",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

### GET `/api/users/{id}`
Get user by ID.

**Response:** Same as user creation response

### GET `/api/users/email/{email}`
Get user by email address.

**Response:** Same as user creation response

### GET `/api/users`
Get all users with pagination.

**Query Parameters:**
- `page` (default: 0) - Page number (0-based)
- `size` (default: 10) - Page size

**Response:**
```json
{
  "status": true,
  "data": {
    "content": [ ... ], // Array of User objects
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### GET `/api/users/search`
Search users by keyword (name or email).

**Query Parameters:**
- `keyword` (required) - Search keyword
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated User objects

### PUT `/api/users/{id}`
Update user information.

**Request:**
```json
{
  "emailAddress": "updated@example.com",
  "firstName": "John",
  "lastName": "Smith",
  "phoneNumber": "+1234567890"
}
```

**Response:** Updated User object

### DELETE `/api/users/{id}`
Delete user by ID.

**Response:**
```json
{
  "status": true,
  "message": "User deleted successfully"
}
```

### POST `/api/users/{id}/activate`
Activate user account.

**Response:**
```json
{
  "status": true,
  "message": "User activated successfully"
}
```

### POST `/api/users/{id}/deactivate`
Deactivate user account.

**Response:**
```json
{
  "status": true,
  "message": "User deactivated successfully"
}
```

---

## 4. Products

### POST `/api/products`
Create a new product.

**Request:**
```json
{
  "categoryId": "uuid",
  "name": "Product Name",
  "description": "Product description",
  "price": 99.99,
  "stockQuantity": 100,
  "isActive": true,
  "images": ["image1.jpg", "image2.jpg"]
}
```

**Response:**
```json
{
  "status": true,
  "data": {
    "id": "uuid",
    "categoryId": "uuid",
    "name": "Product Name",
    "description": "Product description",
    "price": 99.99,
    "stockQuantity": 100,
    "isActive": true,
    "images": ["image1.jpg", "image2.jpg"],
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

### GET `/api/products/{id}`
Get product by ID.

**Response:** Same as product creation response

### GET `/api/products/sku/{sku}`
Get product by SKU.

**Response:** Same as product creation response

### GET `/api/products`
Get all products with pagination.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated Product objects

### GET `/api/products/active`
Get active products with pagination.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated active Product objects

### GET `/api/products/category/{categoryId}`
Get products by category ID.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated Product objects for specified category

### GET `/api/products/search`
Search products by keyword.

**Query Parameters:**
- `keyword` (required) - Search keyword
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated Product objects matching search

### GET `/api/products/price-range`
Get products within price range.

**Query Parameters:**
- `minPrice` (required) - Minimum price
- `maxPrice` (required) - Maximum price
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated Product objects in price range

### GET `/api/products/in-stock`
Get products that are in stock.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated Product objects with stock > 0

### PUT `/api/products/{id}`
Update product information.

**Request:**
```json
{
  "categoryId": "uuid",
  "name": "Updated Product Name",
  "description": "Updated description",
  "price": 149.99,
  "stockQuantity": 50,
  "isActive": true,
  "images": ["newimage.jpg"]
}
```

**Response:** Updated Product object

### DELETE `/api/products/{id}`
Delete product by ID.

**Response:**
```json
{
  "status": true,
  "message": "Product deleted successfully"
}
```

### POST `/api/products/{id}/activate`
Activate product.

**Response:**
```json
{
  "status": true,
  "message": "Product activated successfully"
}
```

### POST `/api/products/{id}/deactivate`
Deactivate product.

**Response:**
```json
{
  "status": true,
  "message": "Product deactivated successfully"
}
```

### PATCH `/api/products/{id}/stock`
Update product stock quantity.

**Query Parameters:**
- `quantity` (required) - Quantity to add (negative to reduce)

**Response:**
```json
{
  "status": true,
  "message": "Stock updated successfully"
}
```

---

## 5. Categories

### POST `/api/categories`
Create a new category.

**Request:**
```json
{
  "categoryName": "Electronics"
}
```

**Response:**
```json
{
  "status": true,
  "data": {
    "id": "uuid",
    "categoryName": "Electronics"
  }
}
```

### GET `/api/categories/{id}`
Get category by ID.

**Response:** Same as category creation response

### GET `/api/categories`
Get all categories.

**Response:**
```json
{
  "status": true,
  "data": [
    {
      "id": "uuid",
      "categoryName": "Electronics"
    },
    {
      "id": "uuid",
      "categoryName": "Clothing"
    }
  ]
}
```

### PUT `/api/categories/{id}`
Update category.

**Request:**
```json
{
  "categoryName": "Updated Category Name"
}
```

**Response:** Updated Category object

### DELETE `/api/categories/{id}`
Delete category by ID.

**Response:**
```json
{
  "status": true,
  "message": "Category deleted successfully"
}
```

---

## 6. Shopping Cart

### GET `/api/cart`
Get all carts with pagination (Admin).

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated Cart objects

### GET `/api/cart/user/{userId}`
Get user's shopping cart.

**Response:**
```json
{
  "status": true,
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "createdAt": "2024-01-01T00:00:00Z",
    "items": [
      {
        "id": "uuid",
        "cartId": "uuid",
        "productId": "uuid",
        "quantity": 2,
        "product": { ... } // Product details
      }
    ]
  }
}
```

### POST `/api/cart/user/{userId}/items`
Add item to cart.

**Request:**
```json
{
  "productId": "uuid",
  "quantity": 2
}
```

**Response:** Updated Cart object

### PUT `/api/cart/user/{userId}/items/{itemId}`
Update cart item quantity.

**Query Parameters:**
- `quantity` (required) - New quantity

**Response:** Updated Cart object

### DELETE `/api/cart/user/{userId}/items/{itemId}`
Remove item from cart.

**Response:** Updated Cart object

### DELETE `/api/cart/user/{userId}`
Clear cart.

**Response:**
```json
{
  "status": true,
  "message": "Cart cleared successfully"
}
```

### GET `/api/cart/user/{userId}/count`
Get cart item count.

**Response:**
```json
{
  "status": true,
  "data": 5
}
```

### GET `/api/cart/count`
Get cart item count (query parameter).

**Query Parameters:**
- `userId` (required) - User ID

**Response:** Same as above

---

## 7. Orders

### POST `/api/orders`
Create a new order (checkout).

**Request:**
```json
{
  "userId": "uuid",
  "paymentMethodId": "uuid",
  "shippingAddressId": "uuid",
  "shippingMethodId": "uuid",
  "customerNotes": "Special delivery instructions",
  "items": [
    {
      "productId": "uuid",
      "quantity": 2
    }
  ]
}
```

**Response:**
```json
{
  "status": true,
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "orderNumber": "ORD-20240101-001",
    "status": "pending",
    "paymentMethodId": "uuid",
    "shippingMethodId": "uuid",
    "paymentStatus": "pending",
    "subtotal": 199.98,
    "total": 219.98,
    "createdAt": "2024-01-01T00:00:00Z",
    "cancelledAt": null,
    "items": [ ... ], // Order items
    "shippingAddress": { ... }, // Address details
    "paymentMethod": { ... } // Payment method details
  }
}
```

### GET `/api/orders/{id}`
Get order by ID.

**Response:** Same as order creation response

### GET `/api/orders/number/{orderNumber}`
Get order by order number.

**Response:** Same as order creation response

### GET `/api/orders`
Get all orders with pagination (Admin).

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated Order objects

### GET `/api/orders/user/{userId}`
Get orders by user ID.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated Order objects for user

### GET `/api/orders/status/{status}`
Get orders by status.

**Path Parameters:**
- `status` - Order status (pending, confirmed, processing, shipped, delivered, cancelled)

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated Order objects with specified status

### PATCH `/api/orders/{id}/status`
Update order status (Admin).

**Query Parameters:**
- `status` (required) - New status

**Response:** Updated Order object

### PATCH `/api/orders/{id}/payment-status`
Update payment status.

**Query Parameters:**
- `paymentStatus` (required) - Payment status (pending, paid, failed, refunded)

**Response:** Updated Order object

### POST `/api/orders/{id}/cancel`
Cancel order.

**Response:** Updated Order object with cancelled status

### DELETE `/api/orders/{id}`
Delete order (Admin only).

**Response:**
```json
{
  "status": true,
  "message": "Order deleted successfully"
}
```

### GET `/api/orders/count`
Get total order count.

**Response:**
```json
{
  "status": true,
  "data": 150
}
```

### GET `/api/orders/count/status/{status}`
Get order count by status.

**Response:**
```json
{
  "status": true,
  "data": 25
}
```

### PUT `/api/orders/{id}`
Update order fields.

**Request:**
```json
{
  "paymentMethodId": "uuid",
  "shippingAddressId": "uuid",
  "shippingMethodId": "uuid",
  "status": "confirmed",
  "paymentStatus": "paid"
}
```

**Response:** Updated Order object

---

## 8. Reviews

### POST `/api/reviews`
Create a product review.

**Request:**
```json
{
  "productId": "uuid",
  "userId": "uuid",
  "rating": 5,
  "comment": "Great product!"
}
```

**Response:**
```json
{
  "status": true,
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "productId": "uuid",
    "rating": 5,
    "comment": "Great product!",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z",
    "user": { ... }, // User details
    "product": { ... } // Product details
  }
}
```

### GET `/api/reviews`
Get all reviews with pagination.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated Review objects

### GET `/api/reviews/{id}`
Get review by ID.

**Response:** Same as review creation response

### GET `/api/reviews/product/{productId}`
Get reviews for a product.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated Review objects for product

### GET `/api/reviews/user/{userId}`
Get reviews by user.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated Review objects by user

### GET `/api/reviews/product/{productId}/average-rating`
Get average rating for a product.

**Response:**
```json
{
  "status": true,
  "data": 4.5
}
```

### GET `/api/reviews/product/{productId}/count`
Get review count for a product.

**Response:**
```json
{
  "status": true,
  "data": 25
}
```

### GET `/api/reviews/check`
Check if user reviewed product.

**Query Parameters:**
- `userId` (required) - User ID
- `productId` (required) - Product ID

**Response:**
```json
{
  "status": true,
  "data": true
}
```

### PUT `/api/reviews/{id}`
Update review.

**Request:** Same as review creation request

**Response:** Updated Review object

### DELETE `/api/reviews/{id}`
Delete review by ID.

**Response:**
```json
{
  "status": true,
  "message": "Review deleted successfully"
}
```

---

## 9. Addresses

### POST `/api/addresses`
Create a new address.

**Request:**
```json
{
  "userId": "uuid",
  "addressLine": "123 Main St",
  "city": "New York",
  "region": "NY",
  "country": "USA",
  "postalCode": "10001",
  "addressType": "shipping"
}
```

**Response:**
```json
{
  "status": true,
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "addressLine": "123 Main St",
    "city": "New York",
    "region": "NY",
    "country": "USA",
    "postalCode": "10001",
    "addressType": "shipping",
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

### GET `/api/addresses/{id}`
Get address by ID.

**Response:** Same as address creation response

### GET `/api/addresses`
Get all addresses (Admin).

**Response:** Array of Address objects

### GET `/api/addresses/user/{userId}`
Get all addresses for a user.

**Response:** Array of Address objects for user

### GET `/api/addresses/user/{userId}/shipping`
Get user's shipping addresses.

**Response:** Array of shipping Address objects

### GET `/api/addresses/user/{userId}/billing`
Get user's billing addresses.

**Response:** Array of billing Address objects

### PUT `/api/addresses/{id}`
Update address.

**Request:** Same as address creation request

**Response:** Updated Address object

### DELETE `/api/addresses/{id}`
Delete address by ID.

**Response:**
```json
{
  "status": true,
  "message": "Address deleted successfully"
}
```

---

## 10. Payment Methods

### POST `/api/payment-methods`
Create a payment method.

**Request:**
```json
{
  "userId": "uuid",
  "methodType": "CREDIT_CARD",
  "provider": "Visa",
  "accountNumber": "****1234",
  "expiryDate": "2025-12-31",
  "isDefault": true
}
```

**Response:**
```json
{
  "id": "uuid",
  "userId": "uuid",
  "methodType": "CREDIT_CARD",
  "provider": "Visa",
  "accountNumber": "****1234",
  "expiryDate": "2025-12-31",
  "isDefault": true,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

### PUT `/api/payment-methods/{id}`
Update payment method.

**Response:** Updated PaymentMethod object

### GET `/api/payment-methods/{id}`
Get payment method by ID.

**Response:** PaymentMethod object

### GET `/api/payment-methods/user/{userId}`
List user's payment methods.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated PaymentMethod objects

### DELETE `/api/payment-methods/{id}`
Delete payment method.

**Response:** 204 No Content

---

## 11. Shipping Methods

### POST `/api/shipping-methods`
Create a shipping method.

**Request:**
```json
{
  "name": "Standard Shipping",
  "description": "5-7 business days",
  "cost": 9.99,
  "estimatedDays": 7,
  "isActive": true
}
```

**Response:**
```json
{
  "id": "uuid",
  "name": "Standard Shipping",
  "description": "5-7 business days",
  "cost": 9.99,
  "estimatedDays": 7,
  "isActive": true,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

### PUT `/api/shipping-methods/{id}`
Update shipping method.

**Response:** Updated ShippingMethod object

### GET `/api/shipping-methods/{id}`
Get shipping method by ID.

**Response:** ShippingMethod object

### GET `/api/shipping-methods`
List all shipping methods.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Paginated ShippingMethod objects

### DELETE `/api/shipping-methods/{id}`
Delete shipping method.

**Response:** 204 No Content

---

## GraphQL API

The GraphQL endpoint is available at `/graphql` and provides the same functionality as the REST API with a single endpoint.

### GraphQL Schema Types

#### Core Types
- **User**: User account information
- **Product**: Product catalog information
- **Category**: Product categories
- **Cart**: Shopping cart with items
- **Order**: Order information with items
- **Review**: Product reviews
- **Address**: User addresses

#### Input Types
- **CreateUserInput**: User creation data
- **UpdateUserInput**: User update data
- **CreateProductInput**: Product creation data
- **UpdateProductInput**: Product update data
- **CreateOrderInput**: Order creation data
- **AddToCartInput**: Cart item addition data
- **CreateReviewInput**: Review creation data
- **CreateAddressInput**: Address creation data

### GraphQL Queries

#### User Queries
```graphql
query {
  user(id: "uuid") {
    id
    emailAddress
    firstName
    lastName
    isActive
    role
  }
}

query {
  users(page: 0, size: 10) {
    content {
      id
      emailAddress
      firstName
      lastName
    }
    pageNumber
    pageSize
    totalElements
  }
}

query {
  searchUsers(keyword: "john", page: 0, size: 10) {
    content {
      id
      emailAddress
      firstName
      lastName
    }
  }
}
```

#### Product Queries
```graphql
query {
  products(page: 0, size: 10) {
    content {
      id
      name
      price
      stockQuantity
      isActive
    }
  }
}

query {
  productsByCategory(categoryId: "uuid", page: 0, size: 10) {
    content {
      id
      name
      price
    }
  }
}

query {
  searchProducts(keyword: "laptop", page: 0, size: 10) {
    content {
      id
      name
      price
      description
    }
  }
}
```

#### Order Queries
```graphql
query {
  ordersByUser(userId: "uuid", page: 0, size: 10) {
    content {
      id
      orderNumber
      status
      total
      createdAt
    }
  }
}

query {
  ordersByStatus(status: "pending", page: 0, size: 10) {
    content {
      id
      orderNumber
      status
      total
    }
  }
}
```

#### Review Queries
```graphql
query {
  reviewsByProduct(productId: "uuid", page: 0, size: 10) {
    content {
      id
      rating
      comment
      createdAt
      user {
        firstName
        lastName
      }
    }
  }
}

query {
  productAverageRating(productId: "uuid")
}
```

### GraphQL Mutations

#### User Mutations
```graphql
mutation {
  createUser(input: {
    emailAddress: "user@example.com"
    firstName: "John"
    lastName: "Doe"
    password: "password123"
  }) {
    id
    emailAddress
    firstName
    lastName
  }
}

mutation {
  updateUser(id: "uuid", input: {
    firstName: "Updated"
    lastName: "Name"
  }) {
    id
    firstName
    lastName
  }
}
```

#### Product Mutations
```graphql
mutation {
  createProduct(input: {
    categoryId: "uuid"
    name: "New Product"
    price: 99.99
    stockQuantity: 100
  }) {
    id
    name
    price
    stockQuantity
  }
}

mutation {
  updateStock(id: "uuid", quantity: 50)
}
```

#### Cart Mutations
```graphql
mutation {
  addToCart(userId: "uuid", input: {
    productId: "uuid"
    quantity: 2
  }) {
    id
    items {
      productId
      quantity
    }
  }
}
```

#### Order Mutations
```graphql
mutation {
  createOrder(input: {
    userId: "uuid"
    items: [{
      productId: "uuid"
      quantity: 2
    }]
  }) {
    id
    orderNumber
    status
    total
  }
}
```

---

## Error Handling

### Standard Error Response Format
```json
{
  "status": false,
  "message": "Error description",
  "timestamp": "2024-01-01T00:00:00Z",
  "statusCode": 400,
  "path": "/api/endpoint",
  "errors": [
    {
      "field": "fieldName",
      "message": "Validation error message",
      "rejectedValue": "invalid_value"
    }
  ]
}
```

### Common HTTP Status Codes
- **200 OK** - Request successful
- **201 Created** - Resource created successfully
- **204 No Content** - Request successful, no content returned
- **400 Bad Request** - Invalid request data or validation errors
- **401 Unauthorized** - Authentication required
- **403 Forbidden** - Insufficient permissions
- **404 Not Found** - Resource not found
- **409 Conflict** - Resource already exists (e.g., duplicate email)
- **500 Internal Server Error** - Server error

---

## Authentication & Authorization

### Authentication
- Use `/api/auth/authenticate` to obtain user session
- Include authentication token in request headers (if implemented)

### Authorization
- **Admin endpoints**: Require admin role
- **User endpoints**: Users can only access their own data
- **Public endpoints**: No authentication required

---

## Rate Limiting & Best Practices

1. **Pagination**: Always use pagination for list endpoints
2. **Validation**: All requests are validated before processing
3. **Error Handling**: Check response status and handle errors appropriately
4. **Idempotency**: POST requests are not idempotent, PUT/PATCH requests are
5. **Caching**: Consider caching frequently accessed data like products and categories

---

## Testing

### Using curl Examples

```bash
# Get all products
curl -X GET "http://localhost:8080/api/products?page=0&size=10"

# Create a new user
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "emailAddress": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "password": "password123"
  }'

# Add item to cart
curl -X POST "http://localhost:8080/api/cart/user/{userId}/items" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "product-uuid",
    "quantity": 2
  }'
```

### GraphQL Testing

```bash
# GraphQL query
curl -X POST "http://localhost:8080/graphql" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { products(page: 0, size: 5) { content { id name price } } }"
  }'
```

---

## API Versioning

- Current version: **v1**
- Version included in base path: `/api/v1/` (if implemented)
- Backward compatibility maintained within major versions

---

## Support & Documentation

- **Swagger UI**: `/swagger-ui.html` - Interactive API documentation
- **OpenAPI Spec**: `/v3/api-docs` - Machine-readable API specification
- **GraphQL Playground**: Available at `/graphql` endpoint for testing queries

---

*This documentation covers all available endpoints as of the current API version. For the most up-to-date information, refer to the Swagger UI or OpenAPI specification.*
