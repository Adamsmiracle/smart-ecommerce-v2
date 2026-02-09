# Smart E-Commerce API - REST Endpoints Documentation

**Base URL**: `http://localhost:8080`  
**API Version**: 1.0.0  
**Date**: February 8, 2026

---

## Table of Contents
1. [Authentication](#1-authentication)
2. [Users](#2-users)
3. [Products](#3-products)
4. [Categories](#4-categories)
5. [Cart](#5-cart)
6. [Orders](#6-orders)
7. [Reviews](#7-reviews)
8. [Wishlist](#8-wishlist)
9. [Addresses](#9-addresses)
10. [Common Response Structure](#common-response-structure)
11. [Error Handling](#error-handling)

---

## 1. Authentication

### Base Path: `/api/auth`

### 1.1 Register User
**POST** `/api/auth/register`

Creates a new user account.

**Request Body**:
```json
{
  "emailAddress": "string (required, email format)",
  "firstName": "string (optional)",
  "lastName": "string (optional)",
  "phoneNumber": "string (optional)",
  "password": "string (required, min 6 characters)"
}
```

**Success Response (201 Created)**:
```json
{
  "status": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "string (JWT token)",
    "refreshToken": "string",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "uuid",
      "emailAddress": "string",
      "firstName": "string",
      "lastName": "string",
      "fullName": "string",
      "phoneNumber": "string",
      "isActive": true,
      "createdAt": "2026-02-08T15:30:00+00:00",
      "updatedAt": "2026-02-08T15:30:00+00:00"
    }
  },
  "statusCode": 201,
  "timestamp": "2026-02-08T15:30:00+00:00"
}
```

---

### 1.2 Login
**POST** `/api/auth/login`

Authenticates a user and returns JWT tokens.

**Request Body**:
```json
{
  "emailAddress": "string (required)",
  "password": "string (required)"
}
```

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Login successful",
  "data": {
    "accessToken": "string (JWT token)",
    "refreshToken": "string",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "uuid",
      "emailAddress": "string",
      "firstName": "string",
      "lastName": "string",
      "fullName": "string",
      "isActive": true
    }
  },
  "statusCode": 200
}
```

---

### 1.3 Refresh Token
**POST** `/api/auth/refresh`

Refreshes an expired access token.

**Request Body**:
```json
{
  "refreshToken": "string (required)"
}
```

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "string (new JWT token)",
    "refreshToken": "string (new refresh token)",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "statusCode": 200
}
```

---

### 1.4 Logout
**POST** `/api/auth/logout`

Logs out the current user.

**Headers**:
```
Authorization: Bearer <access_token>
```

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Logout successful",
  "data": null,
  "statusCode": 200
}
```

---

## 2. Users

### Base Path: `/api/users`

### 2.1 Create User
**POST** `/api/users`

Creates a new user (admin only).

**Request Body**:
```json
{
  "emailAddress": "string (required, email)",
  "firstName": "string",
  "lastName": "string",
  "phoneNumber": "string",
  "password": "string (required)"
}
```

**Success Response (201 Created)**:
```json
{
  "status": true,
  "message": "User created successfully",
  "data": {
    "id": "uuid",
    "emailAddress": "string",
    "firstName": "string",
    "lastName": "string",
    "fullName": "string",
    "phoneNumber": "string",
    "isActive": true,
    "createdAt": "2026-02-08T15:30:00+00:00",
    "updatedAt": "2026-02-08T15:30:00+00:00"
  },
  "statusCode": 201
}
```

---

### 2.2 Get User by ID
**GET** `/api/users/{id}`

Retrieves a specific user by ID.

**Path Parameters**:
- `id` (uuid, required): User ID

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "User retrieved successfully",
  "data": {
    "id": "uuid",
    "emailAddress": "string",
    "firstName": "string",
    "lastName": "string",
    "fullName": "string",
    "phoneNumber": "string",
    "isActive": true,
    "createdAt": "2026-02-08T15:30:00+00:00",
    "updatedAt": "2026-02-08T15:30:00+00:00"
  },
  "statusCode": 200
}
```

---

### 2.3 Get User by Email
**GET** `/api/users/email/{email}`

Retrieves a user by email address.

**Path Parameters**:
- `email` (string, required): User email address

**Success Response (200 OK)**: Same as 2.2

---

### 2.4 Get All Users (Paginated)
**GET** `/api/users`

Retrieves all users with pagination.

**Query Parameters**:
- `page` (integer, optional, default: 0): Page number
- `size` (integer, optional, default: 10): Page size

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Users retrieved successfully",
  "data": {
    "content": [
      {
        "id": "uuid",
        "emailAddress": "string",
        "firstName": "string",
        "lastName": "string",
        "fullName": "string",
        "phoneNumber": "string",
        "isActive": true,
        "createdAt": "2026-02-08T15:30:00+00:00",
        "updatedAt": "2026-02-08T15:30:00+00:00"
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
  "statusCode": 200
}
```

---

### 2.5 Search Users
**GET** `/api/users/search`

Searches users by keyword.

**Query Parameters**:
- `keyword` (string, required): Search keyword
- `page` (integer, optional, default: 0): Page number
- `size` (integer, optional, default: 10): Page size

**Success Response (200 OK)**: Same as 2.4

---

### 2.6 Update User
**PUT** `/api/users/{id}`

Updates a user's information.

**Path Parameters**:
- `id` (uuid, required): User ID

**Request Body**:
```json
{
  "emailAddress": "string",
  "firstName": "string",
  "lastName": "string",
  "phoneNumber": "string",
  "password": "string (optional)"
}
```

**Success Response (200 OK)**: Same as 2.2

---

### 2.7 Delete User
**DELETE** `/api/users/{id}`

Deletes a user.

**Path Parameters**:
- `id` (uuid, required): User ID

**Success Response (204 No Content)**

---

### 2.8 Activate User
**PATCH** `/api/users/{id}/activate`

Activates a deactivated user account.

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "User activated successfully",
  "data": null,
  "statusCode": 200
}
```

---

### 2.9 Deactivate User
**PATCH** `/api/users/{id}/deactivate`

Deactivates a user account.

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "User deactivated successfully",
  "data": null,
  "statusCode": 200
}
```

---

## 3. Products

### Base Path: `/api/products`

### 3.1 Create Product
**POST** `/api/products`

Creates a new product.

**Request Body**:
```json
{
  "categoryId": "uuid (required)",
  "sku": "string (optional, auto-generated if not provided)",
  "name": "string (required)",
  "description": "string",
  "price": "number (required, decimal)",
  "stockQuantity": "integer (optional, default: 0)",
  "isActive": "boolean (optional, default: true)",
  "images": ["string (URLs)", "string"]
}
```

**Success Response (201 Created)**:
```json
{
  "status": true,
  "message": "Product created successfully",
  "data": {
    "id": "uuid",
    "categoryId": "uuid",
    "categoryName": "string",
    "sku": "string",
    "name": "string",
    "description": "string",
    "price": 29.99,
    "stockQuantity": 100,
    "isActive": true,
    "inStock": true,
    "images": ["url1", "url2"],
    "primaryImage": "url1",
    "averageRating": 4.5,
    "reviewCount": 10,
    "createdAt": "2026-02-08T15:30:00+00:00",
    "updatedAt": "2026-02-08T15:30:00+00:00"
  },
  "statusCode": 201
}
```

---

### 3.2 Get Product by ID
**GET** `/api/products/{id}`

Retrieves a specific product by ID.

**Path Parameters**:
- `id` (uuid, required): Product ID

**Success Response (200 OK)**: Same structure as 3.1 response data

---

### 3.3 Get Product by SKU
**GET** `/api/products/sku/{sku}`

Retrieves a product by its SKU.

**Path Parameters**:
- `sku` (string, required): Product SKU

**Success Response (200 OK)**: Same structure as 3.1 response data

---

### 3.4 Get All Products (Paginated)
**GET** `/api/products`

Retrieves all products with pagination.

**Query Parameters**:
- `page` (integer, optional, default: 0): Page number
- `size` (integer, optional, default: 10): Page size

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Products retrieved successfully",
  "data": {
    "content": [
      {
        "id": "uuid",
        "categoryId": "uuid",
        "categoryName": "string",
        "sku": "string",
        "name": "string",
        "description": "string",
        "price": 29.99,
        "stockQuantity": 100,
        "isActive": true,
        "inStock": true,
        "images": ["url1"],
        "primaryImage": "url1",
        "averageRating": 4.5,
        "reviewCount": 10,
        "createdAt": "2026-02-08T15:30:00+00:00",
        "updatedAt": "2026-02-08T15:30:00+00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 500,
    "totalPages": 50,
    "first": true,
    "last": false,
    "hasNext": true,
    "hasPrevious": false
  },
  "statusCode": 200
}
```

---

### 3.5 Get Active Products
**GET** `/api/products/active`

Retrieves only active products.

**Query Parameters**: Same as 3.4

**Success Response (200 OK)**: Same structure as 3.4

---

### 3.6 Get Products by Category
**GET** `/api/products/category/{categoryId}`

Retrieves products in a specific category.

**Path Parameters**:
- `categoryId` (uuid, required): Category ID

**Query Parameters**: Same as 3.4

**Success Response (200 OK)**: Same structure as 3.4

---

### 3.7 Search Products
**GET** `/api/products/search`

Searches products by keyword.

**Query Parameters**:
- `keyword` (string, required): Search keyword (searches name, description, SKU)
- `page` (integer, optional, default: 0)
- `size` (integer, optional, default: 10)

**Success Response (200 OK)**: Same structure as 3.4

---

### 3.8 Get Products In Stock
**GET** `/api/products/in-stock`

Retrieves products with available stock.

**Query Parameters**: Same as 3.4

**Success Response (200 OK)**: Same structure as 3.4

---

### 3.9 Update Product
**PUT** `/api/products/{id}`

Updates a product's information.

**Path Parameters**:
- `id` (uuid, required): Product ID

**Request Body**: Same as 3.1

**Success Response (200 OK)**: Same structure as 3.1 response data

---

### 3.10 Delete Product
**DELETE** `/api/products/{id}`

Deletes a product.

**Success Response (204 No Content)**

---

### 3.11 Activate Product
**PATCH** `/api/products/{id}/activate`

Activates a product.

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Product activated successfully",
  "data": null,
  "statusCode": 200
}
```

---

### 3.12 Deactivate Product
**PATCH** `/api/products/{id}/deactivate`

Deactivates a product.

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Product deactivated successfully",
  "data": null,
  "statusCode": 200
}
```

---

### 3.13 Update Stock
**PATCH** `/api/products/{id}/stock`

Updates product stock quantity.

**Query Parameters**:
- `quantity` (integer, required): New stock quantity

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Stock updated successfully",
  "data": null,
  "statusCode": 200
}
```

---

## 4. Categories

### Base Path: `/api/categories`

### 4.1 Create Category
**POST** `/api/categories`

Creates a new category.

**Request Body**:
```json
{
  "parentCategoryId": "uuid (optional, for subcategories)",
  "categoryName": "string (required)"
}
```

**Success Response (201 Created)**:
```json
{
  "status": true,
  "message": "Category created successfully",
  "data": {
    "id": "uuid",
    "parentCategoryId": "uuid or null",
    "categoryName": "string",
    "parentCategoryName": "string or null",
    "productCount": 0,
    "subCategories": []
  },
  "statusCode": 201
}
```

---

### 4.2 Get Category by ID
**GET** `/api/categories/{id}`

Retrieves a specific category.

**Success Response (200 OK)**: Same as 4.1 response data

---

### 4.3 Get All Categories
**GET** `/api/categories`

Retrieves all categories (flat list).

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "parentCategoryId": null,
      "categoryName": "Electronics",
      "parentCategoryName": null,
      "productCount": 50,
      "subCategories": []
    }
  ],
  "statusCode": 200
}
```

---

### 4.4 Get Root Categories
**GET** `/api/categories/root`

Retrieves only top-level categories (no parent).

**Success Response (200 OK)**: Same structure as 4.3

---

### 4.5 Get Category Tree
**GET** `/api/categories/tree`

Retrieves hierarchical category tree with subcategories.

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Category tree retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "parentCategoryId": null,
      "categoryName": "Electronics",
      "productCount": 50,
      "subCategories": [
        {
          "id": "uuid",
          "parentCategoryId": "parent-uuid",
          "categoryName": "Smartphones",
          "productCount": 25,
          "subCategories": []
        }
      ]
    }
  ],
  "statusCode": 200
}
```

---

### 4.6 Get Subcategories
**GET** `/api/categories/{parentId}/subcategories`

Retrieves subcategories of a specific category.

**Path Parameters**:
- `parentId` (uuid, required): Parent category ID

**Success Response (200 OK)**: Same structure as 4.3

---

### 4.7 Update Category
**PUT** `/api/categories/{id}`

Updates a category.

**Request Body**: Same as 4.1

**Success Response (200 OK)**: Same as 4.1 response data

---

### 4.8 Delete Category
**DELETE** `/api/categories/{id}`

Deletes a category (must have no products or subcategories).

**Success Response (204 No Content)**

---

## 5. Cart

### Base Path: `/api/cart`

### 5.1 Get Cart
**GET** `/api/cart`

Retrieves the current user's shopping cart.

**Query Parameters**:
- `userId` (uuid, required): User ID

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Cart retrieved successfully",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "totalItems": 3,
    "totalValue": 149.97,
    "createdAt": "2026-02-08T15:30:00+00:00",
    "items": [
      {
        "id": "uuid",
        "productId": "uuid",
        "productName": "Wireless Mouse",
        "productImage": "url",
        "unitPrice": 29.99,
        "quantity": 2,
        "subtotal": 59.98,
        "inStock": true,
        "availableStock": 100,
        "addedAt": "2026-02-08T15:30:00+00:00"
      }
    ]
  },
  "statusCode": 200
}
```

---

### 5.2 Add Item to Cart
**POST** `/api/cart/items`

Adds a product to the cart.

**Query Parameters**:
- `userId` (uuid, required): User ID

**Request Body**:
```json
{
  "productId": "uuid (required)",
  "quantity": "integer (required, min: 1)"
}
```

**Success Response (201 Created)**: Same structure as 5.1 response

---

### 5.3 Update Cart Item Quantity
**PUT** `/api/cart/items/{itemId}`

Updates the quantity of a cart item.

**Path Parameters**:
- `itemId` (uuid, required): Cart item ID

**Query Parameters**:
- `userId` (uuid, required): User ID
- `quantity` (integer, required): New quantity

**Success Response (200 OK)**: Same structure as 5.1 response

---

### 5.4 Remove Item from Cart
**DELETE** `/api/cart/items/{itemId}`

Removes an item from the cart.

**Path Parameters**:
- `itemId` (uuid, required): Cart item ID

**Query Parameters**:
- `userId` (uuid, required): User ID

**Success Response (200 OK)**: Same structure as 5.1 response

---

### 5.5 Clear Cart
**DELETE** `/api/cart`

Removes all items from the cart.

**Query Parameters**:
- `userId` (uuid, required): User ID

**Success Response (204 No Content)**

---

### 5.6 Get Cart Item Count
**GET** `/api/cart/count`

Gets the number of items in the cart.

**Query Parameters**:
- `userId` (uuid, required): User ID

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Cart count retrieved successfully",
  "data": 5,
  "statusCode": 200
}
```

---

## 6. Orders

### Base Path: `/api/orders`

### 6.1 Create Order (Checkout)
**POST** `/api/orders`

Creates a new order from the user's cart.

**Request Body**:
```json
{
  "userId": "uuid (required)",
  "shippingAddressId": "uuid (required)",
  "shippingMethodId": "uuid (optional)",
  "customerNotes": "string (optional)"
}
```

**Success Response (201 Created)**:
```json
{
  "status": true,
  "message": "Order created successfully",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "orderNumber": "ORD-20260208-123456",
    "status": "PENDING",
    "paymentStatus": "UNPAID",
    "subtotal": 149.97,
    "shippingCost": 10.00,
    "total": 159.97,
    "itemCount": 3,
    "customerNotes": "string or null",
    "createdAt": "2026-02-08T15:30:00+00:00",
    "cancelledAt": null,
    "items": [
      {
        "id": "uuid",
        "productId": "uuid",
        "productName": "Wireless Mouse",
        "productSku": "MOUSE-001",
        "unitPrice": 29.99,
        "quantity": 2,
        "totalPrice": 59.98
      }
    ]
  },
  "statusCode": 201
}
```

---

### 6.2 Get Order by ID
**GET** `/api/orders/{id}`

Retrieves a specific order.

**Path Parameters**:
- `id` (uuid, required): Order ID

**Success Response (200 OK)**: Same structure as 6.1 response data

---

### 6.3 Get Order by Number
**GET** `/api/orders/number/{orderNumber}`

Retrieves an order by its order number.

**Path Parameters**:
- `orderNumber` (string, required): Order number

**Success Response (200 OK)**: Same structure as 6.1 response data

---

### 6.4 Get All Orders (Paginated)
**GET** `/api/orders`

Retrieves all orders with pagination.

**Query Parameters**:
- `page` (integer, optional, default: 0)
- `size` (integer, optional, default: 10)

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Orders retrieved successfully",
  "data": {
    "content": [
      {
        "id": "uuid",
        "userId": "uuid",
        "orderNumber": "ORD-20260208-123456",
        "status": "PENDING",
        "paymentStatus": "UNPAID",
        "subtotal": 149.97,
        "shippingCost": 10.00,
        "total": 159.97,
        "itemCount": 3,
        "createdAt": "2026-02-08T15:30:00+00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 50,
    "totalPages": 5,
    "first": true,
    "last": false
  },
  "statusCode": 200
}
```

---

### 6.5 Get Orders by User
**GET** `/api/orders/user/{userId}`

Retrieves all orders for a specific user.

**Path Parameters**:
- `userId` (uuid, required): User ID

**Query Parameters**: Same as 6.4

**Success Response (200 OK)**: Same structure as 6.4

---

### 6.6 Get Orders by Status
**GET** `/api/orders/status/{status}`

Retrieves orders by status.

**Path Parameters**:
- `status` (string, required): Order status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)

**Query Parameters**: Same as 6.4

**Success Response (200 OK)**: Same structure as 6.4

---

### 6.7 Update Order Status
**PATCH** `/api/orders/{id}/status`

Updates the status of an order.

**Path Parameters**:
- `id` (uuid, required): Order ID

**Query Parameters**:
- `status` (string, required): New status

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Order status updated successfully",
  "data": null,
  "statusCode": 200
}
```

---

### 6.8 Cancel Order
**PATCH** `/api/orders/{id}/cancel`

Cancels an order.

**Path Parameters**:
- `id` (uuid, required): Order ID

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Order cancelled successfully",
  "data": null,
  "statusCode": 200
}
```

---

## 7. Reviews

### Base Path: `/api/reviews`

### 7.1 Create Review
**POST** `/api/reviews`

Creates a product review.

**Request Body**:
```json
{
  "productId": "uuid (required)",
  "userId": "uuid (required)",
  "rating": "integer (required, 1-5)",
  "title": "string (optional)",
  "comment": "string (optional)"
}
```

**Success Response (201 Created)**:
```json
{
  "status": true,
  "message": "Review created successfully",
  "data": {
    "id": "uuid",
    "productId": "uuid",
    "userId": "uuid",
    "userName": "John Doe",
    "rating": 5,
    "title": "Excellent product!",
    "comment": "Works perfectly, highly recommend",
    "createdAt": "2026-02-08T15:30:00+00:00",
    "updatedAt": "2026-02-08T15:30:00+00:00"
  },
  "statusCode": 201
}
```

---

### 7.2 Get Review by ID
**GET** `/api/reviews/{id}`

Retrieves a specific review.

**Success Response (200 OK)**: Same structure as 7.1 response data

---

### 7.3 Get Reviews by Product (Paginated)
**GET** `/api/reviews/product/{productId}`

Retrieves all reviews for a product.

**Path Parameters**:
- `productId` (uuid, required): Product ID

**Query Parameters**:
- `page` (integer, optional, default: 0)
- `size` (integer, optional, default: 10)

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Reviews retrieved successfully",
  "data": {
    "content": [
      {
        "id": "uuid",
        "productId": "uuid",
        "userId": "uuid",
        "userName": "John Doe",
        "rating": 5,
        "title": "Excellent!",
        "comment": "Great product",
        "createdAt": "2026-02-08T15:30:00+00:00",
        "updatedAt": "2026-02-08T15:30:00+00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 25,
    "totalPages": 3
  },
  "statusCode": 200
}
```

---

### 7.4 Get Reviews by User (Paginated)
**GET** `/api/reviews/user/{userId}`

Retrieves all reviews by a user.

**Path Parameters**:
- `userId` (uuid, required): User ID

**Query Parameters**: Same as 7.3

**Success Response (200 OK)**: Same structure as 7.3

---

### 7.5 Get Product Average Rating
**GET** `/api/reviews/product/{productId}/rating`

Gets the average rating for a product.

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Average rating retrieved successfully",
  "data": 4.5,
  "statusCode": 200
}
```

---

### 7.6 Check if User Reviewed Product
**GET** `/api/reviews/user/{userId}/product/{productId}/exists`

Checks if a user has reviewed a specific product.

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Review status retrieved successfully",
  "data": true,
  "statusCode": 200
}
```

---

### 7.7 Update Review
**PUT** `/api/reviews/{id}`

Updates a review.

**Request Body**: Same as 7.1

**Success Response (200 OK)**: Same structure as 7.1 response data

---

### 7.8 Delete Review
**DELETE** `/api/reviews/{id}`

Deletes a review.

**Success Response (204 No Content)**

---

## 8. Wishlist

### Base Path: `/api/wishlist`

### 8.1 Get Wishlist
**GET** `/api/wishlist`

Retrieves a user's wishlist.

**Query Parameters**:
- `userId` (uuid, required): User ID

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Wishlist retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "userId": "uuid",
      "productId": "uuid",
      "productName": "Wireless Mouse",
      "productSku": "MOUSE-001",
      "productPrice": 29.99,
      "productImage": "url",
      "productInStock": true,
      "createdAt": "2026-02-08T15:30:00+00:00"
    }
  ],
  "statusCode": 200
}
```

---

### 8.2 Add to Wishlist
**POST** `/api/wishlist`

Adds a product to the wishlist.

**Request Body**:
```json
{
  "userId": "uuid (required)",
  "productId": "uuid (required)"
}
```

**Success Response (201 Created)**: Returns added item with same structure as 8.1

---

### 8.3 Remove from Wishlist
**DELETE** `/api/wishlist/{id}`

Removes an item from the wishlist.

**Path Parameters**:
- `id` (uuid, required): Wishlist item ID

**Success Response (204 No Content)**

---

### 8.4 Remove by Product
**DELETE** `/api/wishlist/user/{userId}/product/{productId}`

Removes a specific product from user's wishlist.

**Path Parameters**:
- `userId` (uuid, required): User ID
- `productId` (uuid, required): Product ID

**Success Response (204 No Content)**

---

### 8.5 Clear Wishlist
**DELETE** `/api/wishlist/user/{userId}`

Clears all items from a user's wishlist.

**Success Response (204 No Content)**

---

### 8.6 Get Wishlist Count
**GET** `/api/wishlist/count`

Gets the number of items in the wishlist.

**Query Parameters**:
- `userId` (uuid, required): User ID

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Wishlist count retrieved successfully",
  "data": 5,
  "statusCode": 200
}
```

---

### 8.7 Check if Product in Wishlist
**GET** `/api/wishlist/user/{userId}/product/{productId}/exists`

Checks if a product is in the user's wishlist.

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Wishlist status retrieved successfully",
  "data": true,
  "statusCode": 200
}
```

---

### 8.8 Move to Cart
**POST** `/api/wishlist/{id}/move-to-cart`

Moves a wishlist item to the shopping cart.

**Path Parameters**:
- `id` (uuid, required): Wishlist item ID

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Item moved to cart successfully",
  "data": null,
  "statusCode": 200
}
```

---

## 9. Addresses

### Base Path: `/api/addresses`

### 9.1 Create Address
**POST** `/api/addresses`

Creates a new address.

**Request Body**:
```json
{
  "userId": "uuid (required)",
  "addressLine": "string (optional)",
  "city": "string (required)",
  "region": "string (optional)",
  "country": "string (required)",
  "postalCode": "string (optional)",
  "isDefault": "boolean (optional, default: false)",
  "addressType": "string (optional, e.g., 'shipping', 'billing')"
}
```

**Success Response (201 Created)**:
```json
{
  "status": true,
  "message": "Address created successfully",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "addressLine": "123 Main St",
    "city": "New York",
    "region": "NY",
    "country": "USA",
    "postalCode": "10001",
    "isDefault": true,
    "addressType": "shipping",
    "fullAddress": "123 Main St, New York, NY 10001, USA",
    "createdAt": "2026-02-08T15:30:00+00:00"
  },
  "statusCode": 201
}
```

---

### 9.2 Get Address by ID
**GET** `/api/addresses/{id}`

Retrieves a specific address.

**Success Response (200 OK)**: Same structure as 9.1 response data

---

### 9.3 Get Addresses by User
**GET** `/api/addresses/user/{userId}`

Retrieves all addresses for a user.

**Success Response (200 OK)**:
```json
{
  "status": true,
  "message": "Addresses retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "userId": "uuid",
      "addressLine": "123 Main St",
      "city": "New York",
      "region": "NY",
      "country": "USA",
      "postalCode": "10001",
      "isDefault": true,
      "addressType": "shipping",
      "fullAddress": "123 Main St, New York, NY 10001, USA",
      "createdAt": "2026-02-08T15:30:00+00:00"
    }
  ],
  "statusCode": 200
}
```

---

### 9.4 Get Addresses by Type
**GET** `/api/addresses/user/{userId}/type/{type}`

Retrieves addresses by type (shipping or billing).

**Path Parameters**:
- `userId` (uuid, required): User ID
- `type` (string, required): Address type

**Success Response (200 OK)**: Same structure as 9.3

---

### 9.5 Get Default Address
**GET** `/api/addresses/user/{userId}/default`

Retrieves the user's default address.

**Success Response (200 OK)**: Same structure as 9.1 response data

---

### 9.6 Update Address
**PUT** `/api/addresses/{id}`

Updates an address.

**Request Body**: Same as 9.1

**Success Response (200 OK)**: Same structure as 9.1 response data

---

### 9.7 Delete Address
**DELETE** `/api/addresses/{id}`

Deletes an address.

**Success Response (204 No Content)**

---

### 9.8 Set Default Address
**PATCH** `/api/addresses/{id}/default`

Sets an address as the default.

**Success Response (200 OK)**: Same structure as 9.1 response data

---

## Common Response Structure

All API responses follow this standard structure:

### Success Response
```json
{
  "status": true,
  "message": "Operation successful",
  "data": {}, // or [] or primitive value or null
  "statusCode": 200,
  "timestamp": "2026-02-08T15:30:00+00:00"
}
```

### Paginated Response
```json
{
  "status": true,
  "message": "Data retrieved successfully",
  "data": {
    "content": [],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false,
    "hasNext": true,
    "hasPrevious": false
  },
  "statusCode": 200
}
```

---

## Error Handling

### Error Response Structure
```json
{
  "status": false,
  "message": "Error message",
  "data": {
    "errorCode": "ERROR_CODE_ENUM",
    "message": "User-friendly error message",
    "detail": "Technical error details",
    "path": "/api/endpoint",
    "correlationId": "uuid",
    "clientIp": "192.168.1.1",
    "timestamp": "2026-02-08T15:30:00+00:00"
  },
  "statusCode": 400
}
```

### HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 204 | No Content | Resource deleted or operation successful with no data |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Authentication required or failed |
| 403 | Forbidden | Access denied |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate resource or constraint violation |
| 422 | Unprocessable Entity | Validation error |
| 500 | Internal Server Error | Server error |
| 503 | Service Unavailable | Service temporarily unavailable |

### Common Error Codes

- `RESOURCE_NOT_FOUND` - Requested resource does not exist
- `BAD_REQUEST` - Invalid request parameters or body
- `UNAUTHORIZED` - Authentication required or invalid token
- `FORBIDDEN` - User lacks permission for this action
- `CONFLICT` - Resource already exists or constraint violation
- `VALIDATION_FAILED` - Request validation failed
- `DUPLICATE_RESOURCE` - Resource with same identifier exists
- `INSUFFICIENT_STOCK` - Not enough stock for operation
- `INTERNAL_ERROR` - Server-side error
- `DATA_INTEGRITY` - Database constraint violation
- `DATABASE_ERROR` - Database operation failed
- `PAYMENT_FAILED` - Payment processing error
- `ORDER_PROCESSING_ERROR` - Order creation or update failed
- `CART_ERROR` - Cart operation failed

---

## Authentication

Most endpoints require authentication via JWT (JSON Web Token).

### Including JWT Token in Requests

Add the JWT token to the `Authorization` header:

```
Authorization: Bearer <your_jwt_token>
```

### Token Expiration

- **Access Token**: Expires in 1 hour (3600 seconds)
- **Refresh Token**: Expires in 7 days

Use the `/api/auth/refresh` endpoint to obtain a new access token when expired.

---

## Rate Limiting

- **Default**: 100 requests per minute per IP
- **Burst**: 20 requests per second

Rate limit headers included in responses:
- `X-RateLimit-Limit`: Maximum requests allowed
- `X-RateLimit-Remaining`: Remaining requests
- `X-RateLimit-Reset`: Time when limit resets (Unix timestamp)

---

## CORS

The API supports CORS for the following origins:
- `http://localhost:3000` (development)
- `http://localhost:4200` (development)
- Production domains (configured in deployment)

---

## API Versioning

Current version: **v1**

All endpoints are prefixed with `/api/` for REST endpoints.

---

## Additional Resources

- **Swagger/OpenAPI Documentation**: `http://localhost:8080/swagger-ui.html`
- **GraphQL Playground**: `http://localhost:8080/graphiql`
- **Health Check**: `http://localhost:8080/actuator/health`
- **API Info**: `http://localhost:8080/`

---

## Example Usage (JavaScript/Fetch)

### Register User
```javascript
const response = await fetch('http://localhost:8080/api/auth/register', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    emailAddress: 'john@example.com',
    firstName: 'John',
    lastName: 'Doe',
    password: 'securePassword123'
  })
});

const data = await response.json();
console.log(data);
```

### Get Products (with Authentication)
```javascript
const response = await fetch('http://localhost:8080/api/products?page=0&size=10', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
});

const data = await response.json();
console.log(data.data.content); // Array of products
```

### Add to Cart
```javascript
const response = await fetch('http://localhost:8080/api/cart/items?userId=' + userId, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    productId: 'product-uuid',
    quantity: 2
  })
});

const data = await response.json();
console.log(data.data); // Updated cart
```

### Create Order (Checkout)
```javascript
const response = await fetch('http://localhost:8080/api/orders', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    userId: 'user-uuid',
    shippingAddressId: 'address-uuid',
    customerNotes: 'Please deliver after 5 PM'
  })
});

const data = await response.json();
console.log(data.data); // Created order
```

---

## Notes for Frontend Development

1. **Store JWT Token**: Save the access token and refresh token in secure storage (httpOnly cookies recommended, or localStorage for development)

2. **Handle Token Expiration**: Implement automatic token refresh when receiving 401 errors

3. **Pagination**: All list endpoints support pagination - use `page` and `size` parameters

4. **UUID Format**: All IDs are UUIDs in the format: `"550e8400-e29b-41d4-a716-446655440000"`

5. **Date Format**: All timestamps are in ISO 8601 format with timezone: `"2026-02-08T15:30:00+00:00"`

6. **Decimal Numbers**: Prices and monetary values use decimal format: `29.99`

7. **Image URLs**: Image fields return arrays of URLs: `["https://example.com/image1.jpg"]`

8. **Boolean Values**: Use lowercase `true`/`false` in JSON

9. **Null Values**: Empty optional fields return `null`, not empty strings

10. **Error Handling**: Always check `response.status` field in the response body, not just HTTP status codes

---

**Last Updated**: February 8, 2026  
**API Version**: 1.0.0  
**Documentation Version**: 1.0

