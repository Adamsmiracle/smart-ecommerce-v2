# Database Schema (current - post migrations V1..V4)

This document lists the current PostgreSQL schema (DDL), migration notes, and recommended Java DTO/entity mappings (types and field names) you should apply to update DTOs and entities in the codebase.

> Note: Flyway reported the DB at version 4. The schema below is the V1 baseline with V2, V3 and V4 modifications applied.

---

## Quick plan

1. Use the DDL below to update your DTOs/entities and row-mappers.
2. Map TIMESTAMPTZ -> `OffsetDateTime` in Java and update Jackson config.
3. Replace any JSONB `roles` handling with a single `role` VARCHAR (or an enum) — V2 converted roles to `VARCHAR(20)`.
4. Remove fields dropped by migrations (listed per table).

Checklist
- [ ] Update entity field types (UUID, OffsetDateTime, BigDecimal, etc.).
- [ ] Update DTOs (request/response) to match the new fields.
- [ ] Update RowMappers to use `rs.getObject(name, OffsetDateTime.class)` for timestamp columns.
- [ ] Update GraphQL schema/scalars to use OffsetDateTime scalar.
- [ ] Run tests and verify queries and inserts.

---

## Table DDL (current)

### `app_user`
```
CREATE TABLE app_user (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  email_address VARCHAR(255) NOT NULL UNIQUE,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  phone_number VARCHAR(20),
  password_hash VARCHAR(255),
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  roles VARCHAR(20) DEFAULT 'customer'
);

CREATE INDEX idx_app_user_email ON app_user(email_address);
CREATE INDEX idx_app_user_active ON app_user(is_active);
```

- Migration notes: V1 created `roles` as JSONB; V2 converted it to `VARCHAR(20)` and default `'customer'`.

---

### `address`
```
CREATE TABLE address (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  address_line VARCHAR(500),
  city VARCHAR(100) NOT NULL,
  region VARCHAR(100),
  country VARCHAR(100) NOT NULL,
  postal_code VARCHAR(20),
  address_type VARCHAR(20) DEFAULT 'shipping',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_address_user ON address(user_id);
```

- Migration notes: V2 dropped `is_default`.

---

### `product_category`
```
CREATE TABLE product_category (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  category_name VARCHAR(100) NOT NULL UNIQUE
);
```

- Migration notes: V3 removed `parent_category_id`.

---

### `product`
```
CREATE TABLE product (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  category_id UUID NOT NULL REFERENCES product_category(id) ON DELETE RESTRICT,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(12,2) NOT NULL,
  stock_quantity INTEGER DEFAULT 1,
  is_active BOOLEAN DEFAULT TRUE,
  images JSONB DEFAULT '[]',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_product_name ON product(name);
CREATE INDEX idx_product_active ON product(is_active);
CREATE INDEX idx_product_price ON product(price);
```

- Migration notes: V3 dropped `sku`.

---

### `product_review`
```
CREATE TABLE product_review (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  product_id UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
  order_item_id UUID,
  rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment TEXT,
  is_verified_purchase BOOLEAN DEFAULT FALSE,
  is_approved BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE(user_id, product_id)
);

CREATE INDEX idx_review_product ON product_review(product_id);
CREATE INDEX idx_review_user ON product_review(user_id);
CREATE INDEX idx_review_rating ON product_review(rating);
```

- Migration notes: V3 dropped `title`.

---

### `shopping_cart`
```
CREATE TABLE shopping_cart (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID UNIQUE REFERENCES app_user(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cart_user ON shopping_cart(user_id);
```

---

### `cart_item`
```
CREATE TABLE cart_item (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  cart_id UUID NOT NULL REFERENCES shopping_cart(id) ON DELETE CASCADE,
  product_id UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  UNIQUE(cart_id, product_id)
);

CREATE INDEX idx_cart_item_cart ON cart_item(cart_id);
CREATE INDEX idx_cart_item_product ON cart_item(product_id);
```

- Migration notes: V3 dropped `added_at`.

---

### `shipping_method`
```
CREATE TABLE shipping_method (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(100) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  estimated_days INTEGER
);
```

- Migration notes: V4 dropped `is_active`.

---

### `payment_method`
```
CREATE TABLE payment_method (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  payment_type VARCHAR(50) NOT NULL,
  provider VARCHAR(100),
  account_number VARCHAR(255) NOT NULL,
  expiry_date DATE,
  is_default BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_method_user ON payment_method(user_id);
```

---

### `customer_order`
```
CREATE TABLE customer_order (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  order_number VARCHAR(50) NOT NULL UNIQUE,
  status VARCHAR(30) DEFAULT 'pending',
  payment_method_id UUID REFERENCES payment_method(id) ON DELETE SET NULL,
  payment_status VARCHAR(30) DEFAULT 'pending',
  shipping_address_id UUID REFERENCES address(id) ON DELETE SET NULL,
  shipping_method_id UUID REFERENCES shipping_method(id) ON DELETE SET NULL,
  subtotal DECIMAL(12,2) NOT NULL,
  shipping_cost DECIMAL(10,2) DEFAULT 0,
  total DECIMAL(12,2) NOT NULL,
  customer_notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  cancelled_at TIMESTAMPTZ
);

CREATE INDEX idx_order_user ON customer_order(user_id);
CREATE INDEX idx_order_number ON customer_order(order_number);
CREATE INDEX idx_order_status ON customer_order(status);
CREATE INDEX idx_order_created ON customer_order(created_at);
```

---

### `order_item`
```
CREATE TABLE order_item (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  order_id UUID NOT NULL REFERENCES customer_order(id) ON DELETE CASCADE,
  product_id UUID NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
  product_name VARCHAR(255) NOT NULL,
  product_sku VARCHAR(50),
  unit_price DECIMAL(12,2) NOT NULL,
  quantity INTEGER NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_order_item_order ON order_item(order_id);
CREATE INDEX idx_order_item_product ON order_item(product_id);
```

- Migration notes: V4 dropped `total_price`.

---

### `wishlist_item`
- Migration notes: V4 dropped the `wishlist_item` table; it no longer exists in the schema.

---

## Type mapping recommendations (Java)

- `UUID` → `java.util.UUID`
- `VARCHAR/TEXT` → `String`
- `DECIMAL(12,2)` → `java.math.BigDecimal`
- `INTEGER` → `Integer` (use boxed type if nullable)
- `BOOLEAN` → `Boolean` (boxed if nullable)
- `TIMESTAMPTZ` → `java.time.OffsetDateTime`
- `DATE` → `java.time.LocalDate`
- `JSONB` → `String` (raw JSON) or typed objects using Jackson (e.g., `List<String>` for `images`)

### Specifics
- Use `OffsetDateTime` for `created_at`, `updated_at`, `cancelled_at` and map in RowMappers with:
  ```java
  OffsetDateTime odt = rs.getObject("created_at", OffsetDateTime.class);
  ```
- `app_user.roles` is now a single `VARCHAR(20)` value — map to `String role` or to an enum `Role { CUSTOMER, ADMIN }` and use an `AttributeConverter` or manual mapping.
- Remove fields dropped by migrations from entities and DTOs:
  - product.sku
  - product_category.parent_category_id
  - product_review.title
  - cart_item.added_at
  - order_item.total_price
  - shipping_method.is_active
  - wishlist_item table entirely (delete entity/controller code referencing it)

---

## RowMapper example (JdbcTemplate)

Example mapping a `product` row to a Java `Product` entity:
```java
public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
  return Product.builder()
    .id(rs.getObject("id", UUID.class))
    .categoryId(rs.getObject("category_id", UUID.class))
    .name(rs.getString("name"))
    .description(rs.getString("description"))
    .price(rs.getBigDecimal("price"))
    .stockQuantity(rs.getInt("stock_quantity"))
    .isActive(rs.getBoolean("is_active"))
    .images(objectMapper.readValue(rs.getString("images"), new TypeReference<List<String>>() {}))
    .createdAt(rs.getObject("created_at", OffsetDateTime.class))
    .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
    .build();
}
```

- Use Jackson `ObjectMapper` to parse JSONB fields.

---

## GraphQL / Serialization notes
- Register a scalar for `OffsetDateTime` (graphql-java-extended-scalars `DateTime` or custom) and ensure Spring GraphQL knows how to map it.
- Do not expose `password_hash` in response DTOs.

---

## Next steps I can do for you
- Generate Lombok-annotated Java entity classes and DTOs for all tables (ready-to-copy).
- Generate RowMappers for each table (OffsetDateTime-safe).
- Update GraphQL schema to use DateTime scalars and remove deprecated fields.

Tell me which of the above (entities, mappers, GraphQL updates) you want me to generate now and I'll implement them in the repo.

