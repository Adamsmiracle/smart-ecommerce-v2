# GraphQL Resolvers Documentation

## Overview
This document describes all GraphQL resolvers implemented for the Smart E-Commerce API. The API is organized into **Query Resolvers** for data fetching and **Mutation Resolvers** for data modifications.

## Architecture

### Resolver Files
1. **QueryResolver.java** - Handles all GraphQL queries (read operations)
2. **MutationResolver.java** - Handles all GraphQL mutations (write operations)

### Schema Location
- GraphQL Schema: `src/main/resources/graphql/schema.graphqls`
- Resolvers: `src/main/java/com/miracle/smart_ecommerce_api_v1/graphql/resolver/`

---

## Query Resolvers

### User Queries

| Query | Arguments | Return Type | Description |
|-------|-----------|-------------|-------------|
| `user` | `id: UUID!` | `User` | Get user by ID |
| `userByEmail` | `email: String!` | `User` | Get user by email address |
| `users` | `page: Int, size: Int` | `UserPage!` | Get paginated list of all users |
| `searchUsers` | `keyword: String!, page: Int, size: Int` | `UserPage!` | Search users by keyword |

**Example Query:**
```graphql
query {
  user(id: "123e4567-e89b-12d3-a456-426614174000") {
    id
    emailAddress
    firstName
    lastName
    fullName
    isActive
  }
}
```

---

### Product Queries

| Query | Arguments | Return Type | Description |
|-------|-----------|-------------|-------------|
| `product` | `id: UUID!` | `Product` | Get product by ID |
| `productBySku` | `sku: String!` | `Product` | Get product by SKU |
| `products` | `page: Int, size: Int` | `ProductPage!` | Get paginated list of all products |
| `activeProducts` | `page: Int, size: Int` | `ProductPage!` | Get active products only |
| `productsByCategory` | `categoryId: UUID!, page: Int, size: Int` | `ProductPage!` | Get products in a category |
| `searchProducts` | `keyword: String!, page: Int, size: Int` | `ProductPage!` | Search products by keyword |
| `productsInStock` | `page: Int, size: Int` | `ProductPage!` | Get products with available stock |

**Example Query:**
```graphql
query {
  activeProducts(page: 0, size: 10) {
    content {
      id
      name
      price
      stockQuantity
      inStock
    }
    pageInfo {
      totalElements
      totalPages
    }
  }
}
```

---

### Category Queries

| Query | Arguments | Return Type | Description |
|-------|-----------|-------------|-------------|
| `category` | `id: UUID!` | `Category` | Get category by ID |
| `categories` | - | `[Category]!` | Get all categories (flat list) |
| `rootCategories` | - | `[Category]!` | Get top-level categories |
| `categoryTree` | - | `[Category]!` | Get hierarchical category tree |
| `subcategories` | `parentId: UUID!` | `[Category]!` | Get subcategories of a parent |

**Example Query:**
```graphql
query {
  categoryTree {
    id
    categoryName
    subCategories {
      id
      categoryName
    }
  }
}
```

---

### Cart Queries

| Query | Arguments | Return Type | Description |
|-------|-----------|-------------|-------------|
| `cart` | `userId: UUID!` | `Cart` | Get user's shopping cart |
| `cartItemCount` | `userId: UUID!` | `Int!` | Get count of items in cart |

**Example Query:**
```graphql
query {
  cart(userId: "123e4567-e89b-12d3-a456-426614174000") {
    id
    totalItems
    totalValue
    items {
      id
      productName
      quantity
      unitPrice
      subtotal
    }
  }
}
```

---

### Order Queries

| Query | Arguments | Return Type | Description |
|-------|-----------|-------------|-------------|
| `order` | `id: UUID!` | `Order` | Get order by ID |
| `orderByNumber` | `orderNumber: String!` | `Order` | Get order by order number |
| `orders` | `page: Int, size: Int` | `OrderPage!` | Get all orders (paginated) |
| `ordersByUser` | `userId: UUID!, page: Int, size: Int` | `OrderPage!` | Get user's orders |
| `ordersByStatus` | `status: String!, page: Int, size: Int` | `OrderPage!` | Get orders by status |

**Example Query:**
```graphql
query {
  ordersByUser(userId: "123e4567-e89b-12d3-a456-426614174000", page: 0, size: 10) {
    content {
      id
      orderNumber
      status
      total
      createdAt
    }
    pageInfo {
      totalElements
    }
  }
}
```

---

### Review Queries

| Query | Arguments | Return Type | Description |
|-------|-----------|-------------|-------------|
| `review` | `id: UUID!` | `Review` | Get review by ID |
| `reviewsByProduct` | `productId: UUID!, page: Int, size: Int` | `ReviewPage!` | Get reviews for a product |
| `reviewsByUser` | `userId: UUID!, page: Int, size: Int` | `ReviewPage!` | Get user's reviews |
| `productAverageRating` | `productId: UUID!` | `Float` | Get average rating for product |
| `hasUserReviewedProduct` | `userId: UUID!, productId: UUID!` | `Boolean!` | Check if user reviewed product |

**Example Query:**
```graphql
query {
  reviewsByProduct(productId: "123e4567-e89b-12d3-a456-426614174000", page: 0, size: 10) {
    content {
      id
      rating
      title
      comment
      userName
      createdAt
    }
  }
}
```

---

### Wishlist Queries

| Query | Arguments | Return Type | Description |
|-------|-----------|-------------|-------------|
| `wishlist` | `userId: UUID!` | `[WishlistItem]!` | Get user's wishlist |
| `wishlistCount` | `userId: UUID!` | `Int!` | Get count of wishlist items |
| `isInWishlist` | `userId: UUID!, productId: UUID!` | `Boolean!` | Check if product is in wishlist |

**Example Query:**
```graphql
query {
  wishlist(userId: "123e4567-e89b-12d3-a456-426614174000") {
    id
    productName
    productPrice
    productInStock
    createdAt
  }
}
```

---

### Address Queries

| Query | Arguments | Return Type | Description |
|-------|-----------|-------------|-------------|
| `address` | `id: UUID!` | `Address` | Get address by ID |
| `addressesByUser` | `userId: UUID!` | `[Address]!` | Get all user addresses |
| `shippingAddresses` | `userId: UUID!` | `[Address]!` | Get shipping addresses |
| `billingAddresses` | `userId: UUID!` | `[Address]!` | Get billing addresses |
| `defaultAddress` | `userId: UUID!` | `Address` | Get default address |

**Example Query:**
```graphql
query {
  addressesByUser(userId: "123e4567-e89b-12d3-a456-426614174000") {
    id
    fullAddress
    addressType
    isDefault
  }
}
```

---

## Mutation Resolvers

### User Mutations

| Mutation | Arguments | Return Type | Description |
|----------|-----------|-------------|-------------|
| `createUser` | `input: CreateUserInput!` | `User!` | Create new user |
| `updateUser` | `id: UUID!, input: UpdateUserInput!` | `User!` | Update user |
| `deleteUser` | `id: UUID!` | `Boolean!` | Delete user |
| `activateUser` | `id: UUID!` | `Boolean!` | Activate user account |
| `deactivateUser` | `id: UUID!` | `Boolean!` | Deactivate user account |

**Example Mutation:**
```graphql
mutation {
  createUser(input: {
    emailAddress: "john@example.com"
    firstName: "John"
    lastName: "Doe"
    password: "securePassword123"
  }) {
    id
    emailAddress
    fullName
  }
}
```

---

### Product Mutations

| Mutation | Arguments | Return Type | Description |
|----------|-----------|-------------|-------------|
| `createProduct` | `input: CreateProductInput!` | `Product!` | Create new product |
| `updateProduct` | `id: UUID!, input: UpdateProductInput!` | `Product!` | Update product |
| `deleteProduct` | `id: UUID!` | `Boolean!` | Delete product |
| `activateProduct` | `id: UUID!` | `Boolean!` | Activate product |
| `deactivateProduct` | `id: UUID!` | `Boolean!` | Deactivate product |
| `updateStock` | `id: UUID!, quantity: Int!` | `Boolean!` | Update product stock |

**Example Mutation:**
```graphql
mutation {
  createProduct(input: {
    categoryId: "123e4567-e89b-12d3-a456-426614174000"
    name: "Wireless Mouse"
    description: "Ergonomic wireless mouse"
    price: 29.99
    stockQuantity: 100
    sku: "MOUSE-001"
  }) {
    id
    name
    price
  }
}
```

---

### Category Mutations

| Mutation | Arguments | Return Type | Description |
|----------|-----------|-------------|-------------|
| `createCategory` | `input: CreateCategoryInput!` | `Category!` | Create new category |
| `updateCategory` | `id: UUID!, input: CreateCategoryInput!` | `Category!` | Update category |
| `deleteCategory` | `id: UUID!` | `Boolean!` | Delete category |

**Example Mutation:**
```graphql
mutation {
  createCategory(input: {
    categoryName: "Electronics"
  }) {
    id
    categoryName
  }
}
```

---

### Cart Mutations

| Mutation | Arguments | Return Type | Description |
|----------|-----------|-------------|-------------|
| `addToCart` | `userId: UUID!, input: AddToCartInput!` | `Cart!` | Add item to cart |
| `updateCartItemQuantity` | `userId: UUID!, itemId: UUID!, quantity: Int!` | `Cart!` | Update cart item quantity |
| `removeFromCart` | `userId: UUID!, itemId: UUID!` | `Cart!` | Remove item from cart |
| `clearCart` | `userId: UUID!` | `Boolean!` | Clear all cart items |

**Example Mutation:**
```graphql
mutation {
  addToCart(
    userId: "123e4567-e89b-12d3-a456-426614174000"
    input: {
      productId: "456e7890-e89b-12d3-a456-426614174000"
      quantity: 2
    }
  ) {
    id
    totalItems
    totalValue
    items {
      productName
      quantity
      subtotal
    }
  }
}
```

---

### Review Mutations

| Mutation | Arguments | Return Type | Description |
|----------|-----------|-------------|-------------|
| `createReview` | `input: CreateReviewInput!` | `Review!` | Create product review |
| `updateReview` | `id: UUID!, input: CreateReviewInput!` | `Review!` | Update review |
| `deleteReview` | `id: UUID!` | `Boolean!` | Delete review |

**Example Mutation:**
```graphql
mutation {
  createReview(input: {
    productId: "123e4567-e89b-12d3-a456-426614174000"
    userId: "456e7890-e89b-12d3-a456-426614174000"
    rating: 5
    title: "Excellent product!"
    comment: "Works perfectly, highly recommend"
  }) {
    id
    rating
    title
  }
}
```

---

### Wishlist Mutations

| Mutation | Arguments | Return Type | Description |
|----------|-----------|-------------|-------------|
| `addToWishlist` | `input: AddToWishlistInput!` | `WishlistItem!` | Add product to wishlist |
| `removeFromWishlist` | `id: UUID!` | `Boolean!` | Remove wishlist item by ID |
| `removeProductFromWishlist` | `userId: UUID!, productId: UUID!` | `Boolean!` | Remove product from wishlist |
| `clearWishlist` | `userId: UUID!` | `Boolean!` | Clear entire wishlist |
| `moveToCart` | `userId: UUID!, productId: UUID!` | `Boolean!` | Move item to cart |

**Example Mutation:**
```graphql
mutation {
  addToWishlist(input: {
    userId: "123e4567-e89b-12d3-a456-426614174000"
    productId: "456e7890-e89b-12d3-a456-426614174000"
  }) {
    id
    productName
    productPrice
  }
}
```

---

### Address Mutations

| Mutation | Arguments | Return Type | Description |
|----------|-----------|-------------|-------------|
| `createAddress` | `input: CreateAddressInput!` | `Address!` | Create new address |
| `updateAddress` | `id: UUID!, input: CreateAddressInput!` | `Address!` | Update address |
| `deleteAddress` | `id: UUID!` | `Boolean!` | Delete address |
| `setDefaultAddress` | `id: UUID!` | `Address!` | Set address as default |

**Example Mutation:**
```graphql
mutation {
  createAddress(input: {
    userId: "123e4567-e89b-12d3-a456-426614174000"
    addressLine: "123 Main St"
    city: "New York"
    country: "USA"
    postalCode: "10001"
    addressType: "shipping"
    isDefault: true
  }) {
    id
    fullAddress
    isDefault
  }
}
```

---

## Testing GraphQL Resolvers

### Using GraphiQL
Access GraphiQL at: `http://localhost:9090/graphiql?path=/graphql`

### Using Postman
- **URL**: `http://localhost:9090/graphql`
- **Method**: `POST`
- **Headers**: `Content-Type: application/json`
- **Body**:
```json
{
  "query": "query { products(page: 0, size: 10) { content { id name price } } }"
}
```

---

## Implementation Details

### Service Integration
All resolvers delegate business logic to service layer:
- **UserService** - User operations
- **ProductService** - Product operations
- **CategoryService** - Category operations
- **CartService** - Shopping cart operations
- **OrderService** - Order operations
- **ReviewService** - Review operations
- **WishlistService** - Wishlist operations
- **AddressService** - Address operations

### Caching Strategy
- All services use Caffeine cache with targeted eviction
- Single cache per entity with key prefixes (`id:`, `email:`, `sku:`)
- 30-minute TTL, 5000 entries max per cache

### Error Handling
- GraphQL automatically wraps exceptions in `errors` array
- Custom exceptions (`ResourceNotFoundException`, `DuplicateResourceException`) provide clear error messages
- All mutations return boolean for success indication

---

## Summary

✅ **Complete Query Resolvers**: 40+ queries implemented
✅ **Complete Mutation Resolvers**: 30+ mutations implemented
✅ **All Entities Covered**: Users, Products, Categories, Cart, Orders, Reviews, Wishlist, Addresses
✅ **Pagination Support**: All list queries support pagination
✅ **Type Safety**: Full UUID and custom scalar support
✅ **Service Integration**: All resolvers properly integrated with service layer

---

## Next Steps

1. **Security**: Add authentication/authorization to resolvers
2. **DataLoader**: Implement DataLoader for N+1 query optimization
3. **Subscriptions**: Add real-time subscriptions for cart/order updates
4. **Field Resolvers**: Add nested field resolvers for optimized data fetching
5. **Input Validation**: Add @Valid annotations for input validation

