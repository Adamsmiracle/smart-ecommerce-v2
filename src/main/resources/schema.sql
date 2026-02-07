-- ============================================================================
-- Smart E-Commerce System - Database Schema
-- PostgreSQL Database Schema for raw JDBC implementation
-- ============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- USER MANAGEMENT
-- ============================================================================

-- App User table
CREATE TABLE IF NOT EXISTS app_user (
                                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email_address VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    password_hash VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_app_user_email ON app_user(email_address);
CREATE INDEX IF NOT EXISTS idx_app_user_active ON app_user(is_active);

-- Address table
CREATE TABLE IF NOT EXISTS address (
                                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    address_line VARCHAR(500),
    city VARCHAR(100) NOT NULL,
    region VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    is_default BOOLEAN DEFAULT FALSE,
    address_type VARCHAR(20) DEFAULT 'shipping', -- 'shipping', 'billing'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_address_user ON address(user_id);

-- ============================================================================
-- PRODUCT CATALOG
-- ============================================================================

-- Product Category table (hierarchical)
CREATE TABLE IF NOT EXISTS product_category (
                                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parent_category_id UUID REFERENCES product_category(id) ON DELETE SET NULL,
    category_name VARCHAR(100) NOT NULL UNIQUE
    );

CREATE INDEX IF NOT EXISTS idx_category_parent ON product_category(parent_category_id);

-- Product table
CREATE TABLE IF NOT EXISTS product (
                                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_id UUID NOT NULL REFERENCES product_category(id) ON DELETE RESTRICT,
    sku VARCHAR(50) UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    stock_quantity INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    images JSONB DEFAULT '[]',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_product_category ON product(category_id);
CREATE INDEX IF NOT EXISTS idx_product_sku ON product(sku);
CREATE INDEX IF NOT EXISTS idx_product_name ON product(name);
CREATE INDEX IF NOT EXISTS idx_product_active ON product(is_active);
CREATE INDEX IF NOT EXISTS idx_product_price ON product(price);

-- Product Review table
CREATE TABLE IF NOT EXISTS product_review (
                                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    order_item_id UUID,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(200),
    comment TEXT,
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    is_approved BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id)
    );

CREATE INDEX IF NOT EXISTS idx_review_product ON product_review(product_id);
CREATE INDEX IF NOT EXISTS idx_review_user ON product_review(user_id);
CREATE INDEX IF NOT EXISTS idx_review_rating ON product_review(rating);

-- ============================================================================
-- SHOPPING CART
-- ============================================================================

-- Shopping Cart table
CREATE TABLE IF NOT EXISTS shopping_cart (
                                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE REFERENCES app_user(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_cart_user ON shopping_cart(user_id);

-- Cart Item table
CREATE TABLE IF NOT EXISTS cart_item (
                                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id UUID NOT NULL REFERENCES shopping_cart(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(cart_id, product_id)
    );

CREATE INDEX IF NOT EXISTS idx_cart_item_cart ON cart_item(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_item_product ON cart_item(product_id);

-- ============================================================================
-- ORDERS
-- ============================================================================

-- Shipping Method table
CREATE TABLE IF NOT EXISTS shipping_method (
                                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    estimated_days INTEGER,
    is_active BOOLEAN DEFAULT TRUE
    );

-- Payment Method table
CREATE TABLE IF NOT EXISTS payment_method (
                                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    payment_type VARCHAR(50) NOT NULL, -- 'credit_card', 'debit_card', 'paypal'
    provider VARCHAR(100), -- 'Visa', 'Mastercard', 'PayPal'
    account_number VARCHAR(255) NOT NULL, -- Should be encrypted
    expiry_date DATE,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_payment_method_user ON payment_method(user_id);

-- Customer Order table
CREATE TABLE IF NOT EXISTS customer_order (
                                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(30) DEFAULT 'pending', -- pending, confirmed, processing, shipped, delivered, cancelled, refunded
    payment_method_id UUID REFERENCES payment_method(id) ON DELETE SET NULL,
    payment_status VARCHAR(30) DEFAULT 'pending', -- pending, paid, failed, refunded
    shipping_address_id UUID REFERENCES address(id) ON DELETE SET NULL,
    shipping_method_id UUID REFERENCES shipping_method(id) ON DELETE SET NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    shipping_cost DECIMAL(10, 2) DEFAULT 0,
    total DECIMAL(12, 2) NOT NULL,
    customer_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_order_user ON customer_order(user_id);
CREATE INDEX IF NOT EXISTS idx_order_number ON customer_order(order_number);
CREATE INDEX IF NOT EXISTS idx_order_status ON customer_order(status);
CREATE INDEX IF NOT EXISTS idx_order_created ON customer_order(created_at);

-- Order Item table
CREATE TABLE IF NOT EXISTS order_item (
                                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES customer_order(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(50),
    unit_price DECIMAL(12, 2) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    total_price DECIMAL(12, 2) NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_order_item_order ON order_item(order_id);
CREATE INDEX IF NOT EXISTS idx_order_item_product ON order_item(product_id);

-- ============================================================================
-- WISHLIST
-- ============================================================================

-- Wishlist Item table
CREATE TABLE IF NOT EXISTS wishlist_item (
                                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id)
    );

CREATE INDEX IF NOT EXISTS idx_wishlist_user ON wishlist_item(user_id);

--
-- Insert sample categories
INSERT INTO product_category (id, parent_category_id, category_name) VALUES
                                                                         ('11111111-1111-1111-1111-111111111111', NULL, 'Electronics'),
                                                                         ('22222222-2222-2222-2222-222222222222', NULL, 'Clothing'),
                                                                         ('33333333-3333-3333-3333-333333333333', NULL, 'Home & Garden'),
                                                                         ('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', 'Smartphones'),
                                                                         ('55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', 'Laptops'),
                                                                         ('66666666-6666-6666-6666-666666666666', '22222222-2222-2222-2222-222222222222', 'Men'),
                                                                         ('77777777-7777-7777-7777-777777777777', '22222222-2222-2222-2222-222222222222', 'Women')
    ON CONFLICT (id) DO NOTHING;

-- Insert sample shipping methods
INSERT INTO shipping_method (id, name, description, price, estimated_days, is_active) VALUES
                                                                                          ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Standard Shipping', 'Delivery in 5-7 business days', 5.99, 7, TRUE),
                                                                                          ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Express Shipping', 'Delivery in 2-3 business days', 12.99, 3, TRUE),
                                                                                          ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Next Day Delivery', 'Delivery next business day', 24.99, 1, TRUE)
    ON CONFLICT (id) DO NOTHING;

