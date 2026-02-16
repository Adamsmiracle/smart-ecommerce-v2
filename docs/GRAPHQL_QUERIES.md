# GraphQL Queries Reference

This document lists every Query from `src/main/resources/graphql/schema.graphqls` grouped by domain. For each query you'll find the GraphQL signature, arguments, return type and a short example you can paste into GraphiQL / Apollo Studio.

Notes:
- Scalars used: `UUID`, `BigDecimal`, `OffsetDateTime`.
- Pagination pattern: `page` (default 0) and `size` (default 10) produce Page objects (with `content`, `pageNumber`, `pageSize`, etc.).

---

## Users

- user(id: UUID!): User
  - Args: `id: UUID!`
  - Returns: `User` or `null`
  - Example:
    query:
    {
      user(id: "0c6ff99a-d894-48c7-a8c6-194413146892") {
        id
        emailAddress
        fullName
        role
      }
    }

- userByEmail(email: String!): User
  - Args: `email: String!`
  - Returns: `User` or `null`
  - Example:
    query:
    {
      userByEmail(email: "alice@example.com") {
        id
        emailAddress
        firstName
        lastName
      }
    }

- users(page: Int = 0, size: Int = 10): UserPage!
  - Args: `page: Int`, `size: Int`
  - Returns: `UserPage` (paginated list)
  - Example:
    query Variables: `{ "page": 0, "size": 20 }`
    query:
    {
      users(page: 0, size: 20) {
        content { id emailAddress firstName lastName }
        pageNumber
        pageSize
        totalElements
      }
    }

- searchUsers(keyword: String!, page: Int = 0, size: Int = 10): UserPage!
  - Args: `keyword: String!`, optional `page`, `size`
  - Returns: `UserPage` (results matching the keyword)
  - Example:
    query:
    {
      searchUsers(keyword: "john", page: 0, size: 10) {
        content { id emailAddress fullName }
      }
    }

---

## Products

- product(id: UUID!): Product
  - Args: `id: UUID!`
  - Returns: `Product` or `null`
  - Example:
    query:
    {
      product(id: "...uuid...") {
        id name price stockQuantity isActive
      }
    }

- products(page: Int = 0, size: Int = 10): ProductPage!
  - Returns paginated `ProductPage`
  - Example:
    {
      products(page:0,size:10) { content { id name price } totalElements }
    }

- activeProducts(page: Int = 0, size: Int = 10): ProductPage!
  - Returns active products only.

- productsByCategory(categoryId: UUID!, page: Int = 0, size: Int = 10): ProductPage!
  - Args: `categoryId: UUID!`.
  - Example:
    {
      productsByCategory(categoryId: "...uuid...", page:0,size:10) { content { id name } }
    }

- searchProducts(keyword: String!, page: Int = 0, size: Int = 10): ProductPage!
  - Args: `keyword: String!` — searches product name/description.
  - Example:
    {
      searchProducts(keyword:"phone", page:0,size:10) { content { id name } }
    }

- productsInStock(page: Int = 0, size: Int = 10): ProductPage!
  - Returns products whose stockQuantity > 0.

---

## Categories

- category(id: UUID!): Category
  - Args: `id: UUID!`
  - Returns: `Category` or `null`.

- categories: [Category]!
  - Returns all categories as a list.
  - Example:
    {
      categories { id categoryName }
    }

---

## Cart

- cart(userId: UUID!): Cart
  - Args: `userId: UUID!`
  - Returns the user's `Cart` (or null if none).
  - Example:
    {
      cart(userId: "...uuid...") { id userId createdAt }
    }

- cartItemCount(userId: UUID!): Int!
  - Args: `userId: UUID!`
  - Returns integer count of items in cart.
  - Example:
    {
      cartItemCount(userId: "...uuid...")
    }

---

## Orders

- order(id: UUID!): Order
  - Args: `id: UUID!`.
  - Returns single `Order`.
  - Example:
    {
      order(id: "...uuid...") { id orderNumber status subtotal total createdAt }
    }

- orderByNumber(orderNumber: String!): Order
  - Args: `orderNumber: String!`.

- orders(page: Int = 0, size: Int = 10): OrderPage!
  - Returns paginated orders.

- ordersByUser(userId: UUID!, page: Int = 0, size: Int = 10): OrderPage!
  - Args: `userId: UUID!` — returns paginated orders for a user.
  - Example:
    {
      ordersByUser(userId: "...uuid...", page:0,size:10) {
        content { id orderNumber status subtotal total }
      }
    }

- ordersByStatus(status: String!, page: Int = 0, size: Int = 10): OrderPage!
  - Args: `status: String!` — filter by order status.

---

## Reviews

- review(id: UUID!): Review
  - Args: `id: UUID!` — returns single review.

- reviewsByProduct(productId: UUID!, page: Int = 0, size: Int = 10): ReviewPage!
  - Args: `productId: UUID!` — paginated list of product reviews.
  - Example:
    {
      reviewsByProduct(productId: "...uuid...", page:0,size:10) {
        content { id userId rating comment createdAt }
      }
    }

- reviewsByUser(userId: UUID!, page: Int = 0, size: Int = 10): ReviewPage!
  - Args: `userId: UUID!`.

- productAverageRating(productId: UUID!): Float
  - Args: `productId: UUID!` — returns average rating as float.
  - Example:
    {
      productAverageRating(productId: "...uuid...")
    }

- hasUserReviewedProduct(userId: UUID!, productId: UUID!): Boolean!
  - Args: `userId`, `productId` — returns true/false.

---

## Addresses

- address(id: UUID!): Address
  - Args: `id: UUID!` — single address.

- addressesByUser(userId: UUID!): [Address]!
  - Args: `userId: UUID!` — returns all addresses for a user.
  - Example:
    {
      addressesByUser(userId: "...uuid...") {
        id addressLine city region country postalCode addressType createdAt
      }
    }

- shippingAddresses(userId: UUID!): [Address]!
  - Args: `userId: UUID!` — addresses filtered as shipping type.

- billingAddresses(userId: UUID!): [Address]!
  - Args: `userId: UUID!` — addresses filtered as billing type.

---

## Quick copy-paste examples

- Get product with nested fields

```
query GetProduct($id: UUID!) {
  product(id: $id) {
    id
    name
    description
    price
    images
    createdAt
    updatedAt
  }
}

# Variables:
# { "id": "<uuid>" }
```

- Get paginated products

```
query ListProducts($page:Int,$size:Int) {
  products(page:$page,size:$size) {
    content { id name price }
    pageNumber
    pageSize
    totalElements
  }
}

# Variables: { "page":0, "size":10 }
```

- Create a review (mutation example - not a query but useful context)

```
mutation CreateReview($input: CreateReviewInput!) {
  createReview(input: $input) {
    id
    productId
    rating
    comment
    createdAt
  }
}

/* Variables example:
{
  "input": {
    "productId": "<uuid>",
    "userId": "<uuid>",
    "rating": 5,
    "comment": "Great product"
  }
}
*/
```

---

If you want, I can:
- add example responses for each query using your DTO shapes,
- generate a Postman collection or GraphiQL-ready file with all queries pre-filled,
- create matching resolver stubs in `src/main/java/.../graphql/resolver`.

Which of these next steps should I do? (I can proceed automatically if you want.)

