# Cart Data Persistence Guide

This guide provides comprehensive instructions for persisting cart data using the REST API endpoints in your smart e-commerce application.

## Table of Contents
1. [API Endpoints Overview](#api-endpoints-overview)
2. [Authentication](#authentication)
3. [Cart Operations](#cart-operations)
4. [Request/Response Formats](#requestresponse-formats)
5. [Error Handling](#error-handling)
6. [Frontend Implementation Examples](#frontend-implementation-examples)
7. [Common Issues and Solutions](#common-issues-and-solutions)
8. [Testing Guide](#testing-guide)

## API Endpoints Overview

### Base URL
```
http://localhost:8080/api/cart
```

### Available Endpoints
| Method | Endpoint | Description |
|--------|-----------|-------------|
| `GET` | `/api/cart/user/{userId}` | Get user's cart |
| `POST` | `/api/cart/user/{userId}/items` | Add item to cart |
| `PUT` | `/api/cart/user/{userId}/items/{itemId}` | Update item quantity |
| `DELETE` | `/api/cart/user/{userId}/items/{itemId}` | Remove item from cart |
| `DELETE` | `/api/cart/user/{userId}` | Clear entire cart |
| `GET` | `/api/cart/user/{userId}/count` | Get cart item count |

## Authentication

All cart endpoints require JWT authentication. Include the token in the Authorization header:

```javascript
const headers = {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${yourJwtToken}`
};
```

### Getting JWT Token
```javascript
// After login
const loginResponse = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'password'
  })
});

const { token } = await loginResponse.json();
localStorage.setItem('authToken', token);
```

## Cart Operations

### 1. Get User Cart

**Endpoint:** `GET /api/cart/user/{userId}`

**Example Request:**
```javascript
const getCart = async (userId) => {
  const token = localStorage.getItem('authToken');
  
  const response = await fetch(`/api/cart/user/${userId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
  }

  const result = await response.json();
  return result.data;
};

// Usage
const cart = await getCart('550e8400-e29b-41d4-a716-446655440000');
console.log('User cart:', cart);
```

**Response Structure:**
```json
{
  "success": true,
  "data": {
    "id": "cart-uuid",
    "userId": "user-uuid",
    "totalItems": 3,
    "totalValue": 299.97,
    "createdAt": "2024-01-15T10:30:00Z",
    "items": [
      {
        "id": "item-uuid",
        "productId": "product-uuid",
        "productName": "Product Name",
        "productImage": "image-url",
        "unitPrice": 99.99,
        "quantity": 2,
        "subtotal": 199.98,
        "inStock": true,
        "availableStock": 15,
        "addedAt": "2024-01-15T10:30:00Z"
      }
    ]
  }
}
```

### 2. Add Item to Cart

**Endpoint:** `POST /api/cart/user/{userId}/items`

**Request Body:**
```json
{
  "productId": "product-uuid",
  "quantity": 2
}
```

**Example Implementation:**
```javascript
const addToCart = async (userId, productId, quantity) => {
  const token = localStorage.getItem('authToken');
  
  // Validate inputs
  if (!userId || !productId || !quantity || quantity < 1) {
    throw new Error('Invalid input: userId, productId, and quantity (>=1) are required');
  }

  const response = await fetch(`/api/cart/user/${userId}/items`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      productId: productId,
      quantity: quantity
    })
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || `HTTP ${response.status}: ${response.statusText}`);
  }

  const result = await response.json();
  return result.data;
};

// Usage examples
try {
  // Add new item
  const cart = await addToCart(
    '550e8400-e29b-41d4-a716-446655440000',
    '660e8400-e29b-41d4-a716-446655440000',
    2
  );
  console.log('Item added to cart:', cart);

  // Add more of existing item (quantity will be accumulated)
  const updatedCart = await addToCart(
    '550e8400-e29b-41d4-a716-446655440000',
    '660e8400-e29b-41d4-a716-446655440000',
    1
  );
  console.log('Cart updated:', updatedCart);
} catch (error) {
  console.error('Failed to add to cart:', error);
}
```

### 3. Update Item Quantity

**Endpoint:** `PUT /api/cart/user/{userId}/items/{itemId}?quantity={newQuantity}`

**Example Implementation:**
```javascript
const updateItemQuantity = async (userId, itemId, newQuantity) => {
  const token = localStorage.getItem('authToken');
  
  if (!userId || !itemId || !newQuantity || newQuantity < 1) {
    throw new Error('Invalid input: userId, itemId, and quantity (>=1) are required');
  }

  const response = await fetch(
    `/api/cart/user/${userId}/items/${itemId}?quantity=${newQuantity}`,
    {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || `HTTP ${response.status}: ${response.statusText}`);
  }

  const result = await response.json();
  return result.data;
};

// Usage
try {
  const cart = await updateItemQuantity(
    '550e8400-e29b-41d4-a716-446655440000',
    '770e8400-e29b-41d4-a716-446655440000',
    5
  );
  console.log('Item quantity updated:', cart);
} catch (error) {
  console.error('Failed to update quantity:', error);
}
```

### 4. Remove Item from Cart

**Endpoint:** `DELETE /api/cart/user/{userId}/items/{itemId}`

**Example Implementation:**
```javascript
const removeFromCart = async (userId, itemId) => {
  const token = localStorage.getItem('authToken');
  
  if (!userId || !itemId) {
    throw new Error('userId and itemId are required');
  }

  const response = await fetch(`/api/cart/user/${userId}/items/${itemId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || `HTTP ${response.status}: ${response.statusText}`);
  }

  const result = await response.json();
  return result.data;
};

// Usage
try {
  const cart = await removeFromCart(
    '550e8400-e29b-41d4-a716-446655440000',
    '770e8400-e29b-41d4-a716-446655440000'
  );
  console.log('Item removed from cart:', cart);
} catch (error) {
  console.error('Failed to remove item:', error);
}
```

### 5. Clear Entire Cart

**Endpoint:** `DELETE /api/cart/user/{userId}`

**Example Implementation:**
```javascript
const clearCart = async (userId) => {
  const token = localStorage.getItem('authToken');
  
  if (!userId) {
    throw new Error('userId is required');
  }

  const response = await fetch(`/api/cart/user/${userId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || `HTTP ${response.status}: ${response.statusText}`);
  }

  return response.json();
};

// Usage
try {
  await clearCart('550e8400-e29b-41d4-a716-446655440000');
  console.log('Cart cleared successfully');
} catch (error) {
  console.error('Failed to clear cart:', error);
}
```

### 6. Get Cart Item Count

**Endpoint:** `GET /api/cart/user/{userId}/count`

**Example Implementation:**
```javascript
const getCartItemCount = async (userId) => {
  const token = localStorage.getItem('authToken');
  
  if (!userId) {
    throw new Error('userId is required');
  }

  const response = await fetch(`/api/cart/user/${userId}/count`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || `HTTP ${response.status}: ${response.statusText}`);
  }

  const result = await response.json();
  return result.data;
};

// Usage
try {
  const count = await getCartItemCount('550e8400-e29b-41d4-a716-446655440000');
  console.log('Cart item count:', count);
} catch (error) {
  console.error('Failed to get cart count:', error);
}
```

## Request/Response Formats

### Add to Cart Request
```json
{
  "productId": "string (UUID, required)",
  "quantity": "integer (>=1, required)"
}
```

### Cart Response
```json
{
  "success": true,
  "data": {
    "id": "string (UUID)",
    "userId": "string (UUID)",
    "totalItems": "integer",
    "totalValue": "number (BigDecimal)",
    "createdAt": "string (ISO-8601 datetime)",
    "items": [
      {
        "id": "string (UUID)",
        "productId": "string (UUID)",
        "productName": "string",
        "productImage": "string (URL or null)",
        "unitPrice": "number (BigDecimal)",
        "quantity": "integer",
        "subtotal": "number (BigDecimal)",
        "inStock": "boolean",
        "availableStock": "integer",
        "addedAt": "string (ISO-8601 datetime)"
      }
    ]
  }
}
```

### Error Response
```json
{
  "success": false,
  "error": "Error message description",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Error Handling

### Common HTTP Status Codes
| Status | Description | Common Causes |
|--------|-------------|---------------|
| `200` | Success | Operation completed successfully |
| `400` | Bad Request | Invalid input data, validation errors |
| `401` | Unauthorized | Missing or invalid JWT token |
| `404` | Not Found | User, cart, or item not found |
| `500` | Internal Server Error | Server-side error |

### Common Error Messages
```javascript
// Validation errors
"Product ID is required"
"Quantity must be at least 1"
"Invalid UUID format"

// Business logic errors
"Product not found"
"Product is not available"
"Insufficient stock. Available: 5"
"User not found"
"Cart not found for user"
"Item does not belong to user's cart"

// Authentication errors
"JWT token is missing"
"JWT token is invalid or expired"
```

### Error Handling Implementation
```javascript
class CartService {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
  }

  async handleResponse(response) {
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      
      switch (response.status) {
        case 400:
          throw new Error(error.error || 'Invalid request data');
        case 401:
          throw new Error('Authentication required. Please log in again.');
        case 404:
          throw new Error(error.error || 'Resource not found');
        case 500:
          throw new Error('Server error. Please try again later.');
        default:
          throw new Error(error.error || `HTTP ${response.status}: ${response.statusText}`);
      }
    }
    
    return response.json();
  }

  async addToCart(userId, productId, quantity) {
    try {
      const response = await fetch(`${this.baseUrl}/api/cart/user/${userId}/items`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.getToken()}`
        },
        body: JSON.stringify({ productId, quantity })
      });

      const result = await this.handleResponse(response);
      return result.data;
    } catch (error) {
      console.error('Add to cart error:', error);
      throw error;
    }
  }

  getToken() {
    const token = localStorage.getItem('authToken');
    if (!token) {
      throw new Error('No authentication token found');
    }
    return token;
  }
}
```

## Frontend Implementation Examples

### React Component Example
```jsx
import React, { useState, useEffect } from 'react';

const ShoppingCart = ({ userId }) => {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const getAuthToken = () => localStorage.getItem('authToken');

  // Fetch cart
  const fetchCart = async () => {
    try {
      setLoading(true);
      const response = await fetch(`/api/cart/user/${userId}`, {
        headers: {
          'Authorization': `Bearer ${getAuthToken()}`
        }
      });
      
      const result = await response.json();
      if (result.success) {
        setCart(result.data);
      } else {
        setError(result.error);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Add item to cart
  const addToCart = async (productId, quantity) => {
    try {
      const response = await fetch(`/api/cart/user/${userId}/items`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${getAuthToken()}`
        },
        body: JSON.stringify({ productId, quantity })
      });
      
      const result = await response.json();
      if (result.success) {
        setCart(result.data);
      } else {
        setError(result.error);
      }
    } catch (err) {
      setError(err.message);
    }
  };

  // Update item quantity
  const updateQuantity = async (itemId, newQuantity) => {
    try {
      const response = await fetch(
        `/api/cart/user/${userId}/items/${itemId}?quantity=${newQuantity}`,
        {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${getAuthToken()}`
          }
        }
      );
      
      const result = await response.json();
      if (result.success) {
        setCart(result.data);
      } else {
        setError(result.error);
      }
    } catch (err) {
      setError(err.message);
    }
  };

  // Remove item from cart
  const removeItem = async (itemId) => {
    try {
      const response = await fetch(`/api/cart/user/${userId}/items/${itemId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${getAuthToken()}`
        }
      });
      
      const result = await response.json();
      if (result.success) {
        setCart(result.data);
      } else {
        setError(result.error);
      }
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    fetchCart();
  }, [userId]);

  if (loading) return <div>Loading cart...</div>;
  if (error) return <div>Error: {error}</div>;
  if (!cart) return <div>No cart found</div>;

  return (
    <div>
      <h2>Shopping Cart</h2>
      <p>Total Items: {cart.totalItems}</p>
      <p>Total Value: ${cart.totalValue.toFixed(2)}</p>
      
      {cart.items.map(item => (
        <div key={item.id} style={{ border: '1px solid #ccc', padding: '10px', margin: '10px 0' }}>
          <h4>{item.productName}</h4>
          <p>Price: ${item.unitPrice.toFixed(2)}</p>
          <p>Quantity: 
            <input 
              type="number" 
              value={item.quantity} 
              min="1"
              onChange={(e) => updateQuantity(item.id, parseInt(e.target.value))}
              style={{ width: '60px', marginLeft: '10px' }}
            />
          </p>
          <p>Subtotal: ${item.subtotal.toFixed(2)}</p>
          <p>Stock: {item.inStock ? `${item.availableStock} available` : 'Out of stock'}</p>
          <button onClick={() => removeItem(item.id)}>Remove</button>
        </div>
      ))}
      
      <button onClick={() => addToCart('sample-product-id', 1)}>Add Sample Item</button>
      <button onClick={() => {
        fetch(`/api/cart/user/${userId}`, {
          method: 'DELETE',
          headers: { 'Authorization': `Bearer ${getAuthToken()}` }
        }).then(() => setCart(null));
      }}>Clear Cart</button>
    </div>
  );
};

export default ShoppingCart;
```

### Vue.js Component Example
```vue
<template>
  <div>
    <h2>Shopping Cart</h2>
    
    <div v-if="loading">Loading cart...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="!cart">No cart found</div>
    
    <div v-else>
      <p>Total Items: {{ cart.totalItems }}</p>
      <p>Total Value: ${{ cart.totalValue.toFixed(2) }}</p>
      
      <div v-for="item in cart.items" :key="item.id" class="cart-item">
        <h4>{{ item.productName }}</h4>
        <p>Price: ${{ item.unitPrice.toFixed(2) }}</p>
        <p>
          Quantity: 
          <input 
            type="number" 
            v-model.number="item.quantity" 
            min="1"
            @change="updateQuantity(item.id, item.quantity)"
            class="quantity-input"
          />
        </p>
        <p>Subtotal: ${{ item.subtotal.toFixed(2) }}</p>
        <p>Stock: {{ item.inStock ? `${item.availableStock} available` : 'Out of stock' }}</p>
        <button @click="removeItem(item.id)" class="remove-btn">Remove</button>
      </div>
      
      <button @click="addSampleItem" class="add-btn">Add Sample Item</button>
      <button @click="clearCart" class="clear-btn">Clear Cart</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ShoppingCart',
  props: {
    userId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      cart: null,
      loading: false,
      error: null
    };
  },
  methods: {
    getAuthToken() {
      const token = localStorage.getItem('authToken');
      if (!token) {
        throw new Error('No authentication token found');
      }
      return token;
    },

    async fetchCart() {
      try {
        this.loading = true;
        const response = await fetch(`/api/cart/user/${this.userId}`, {
          headers: {
            'Authorization': `Bearer ${this.getAuthToken()}`
          }
        });
        
        const result = await response.json();
        if (result.success) {
          this.cart = result.data;
        } else {
          this.error = result.error;
        }
      } catch (err) {
        this.error = err.message;
      } finally {
        this.loading = false;
      }
    },

    async addToCart(productId, quantity) {
      try {
        const response = await fetch(`/api/cart/user/${this.userId}/items`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.getAuthToken()}`
          },
          body: JSON.stringify({ productId, quantity })
        });
        
        const result = await response.json();
        if (result.success) {
          this.cart = result.data;
        } else {
          this.error = result.error;
        }
      } catch (err) {
        this.error = err.message;
      }
    },

    async updateQuantity(itemId, newQuantity) {
      try {
        const response = await fetch(
          `/api/cart/user/${this.userId}/items/${itemId}?quantity=${newQuantity}`,
          {
            method: 'PUT',
            headers: {
              'Authorization': `Bearer ${this.getAuthToken()}`
            }
          }
        );
        
        const result = await response.json();
        if (result.success) {
          this.cart = result.data;
        } else {
          this.error = result.error;
        }
      } catch (err) {
        this.error = err.message;
      }
    },

    async removeItem(itemId) {
      try {
        const response = await fetch(`/api/cart/user/${this.userId}/items/${itemId}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${this.getAuthToken()}`
          }
        });
        
        const result = await response.json();
        if (result.success) {
          this.cart = result.data;
        } else {
          this.error = result.error;
        }
      } catch (err) {
        this.error = err.message;
      }
    },

    async clearCart() {
      try {
        await fetch(`/api/cart/user/${this.userId}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${this.getAuthToken()}`
          }
        });
        
        this.cart = null;
      } catch (err) {
        this.error = err.message;
      }
    },

    addSampleItem() {
      this.addToCart('sample-product-id', 1);
    }
  },

  mounted() {
    this.fetchCart();
  }
};
</script>

<style scoped>
.cart-item {
  border: 1px solid #ccc;
  padding: 10px;
  margin: 10px 0;
}

.quantity-input {
  width: 60px;
  margin-left: 10px;
}

.remove-btn {
  background-color: #ff4444;
  color: white;
  border: none;
  padding: 5px 10px;
  cursor: pointer;
}

.add-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  cursor: pointer;
  margin-right: 10px;
}

.clear-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 10px 20px;
  cursor: pointer;
}

.error {
  color: red;
  font-weight: bold;
}
</style>
```

## Common Issues and Solutions

### 1. UUID Format Issues
**Problem:** Invalid UUID format in requests
**Solution:** Ensure UUIDs are valid strings
```javascript
// Validate UUID format
function isValidUUID(uuid) {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
  return uuidRegex.test(uuid);
}

// Usage
if (!isValidUUID(productId)) {
  throw new Error('Invalid product ID format');
}
```

### 2. Authentication Issues
**Problem:** Missing or invalid JWT token
**Solution:** Implement proper token management
```javascript
// Token refresh logic
const refreshToken = async () => {
  try {
    const response = await fetch('/api/auth/refresh', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('refreshToken')}`
      }
    });
    
    const { token } = await response.json();
    localStorage.setItem('authToken', token);
    return token;
  } catch (error) {
    localStorage.removeItem('authToken');
    localStorage.removeItem('refreshToken');
    window.location.href = '/login';
  }
};
```

### 3. Network Issues
**Problem:** CORS or network connectivity issues
**Solution:** Check CORS configuration and network
```javascript
// Retry logic with exponential backoff
const fetchWithRetry = async (url, options, maxRetries = 3) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      const response = await fetch(url, options);
      if (response.ok) return response;
      
      if (response.status === 401) {
        // Try to refresh token
        await refreshToken();
        options.headers.Authorization = `Bearer ${localStorage.getItem('authToken')}`;
        continue;
      }
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      await new Promise(resolve => setTimeout(resolve, Math.pow(2, i) * 1000));
    }
  }
};
```

### 4. Stock Validation
**Problem:** Adding items that are out of stock
**Solution:** Check stock before adding
```javascript
const addToCartWithStockCheck = async (userId, productId, quantity) => {
  // First check product availability
  const productResponse = await fetch(`/api/products/${productId}`);
  const product = await productResponse.json();
  
  if (!product.data.isActive) {
    throw new Error('Product is not available');
  }
  
  if (product.data.stockQuantity < quantity) {
    throw new Error(`Insufficient stock. Only ${product.data.stockQuantity} available`);
  }
  
  // Proceed with adding to cart
  return addToCart(userId, productId, quantity);
};
```

## Testing Guide

### 1. Manual Testing with curl

```bash
# Set variables
USER_ID="550e8400-e29b-41d4-a716-446655440000"
PRODUCT_ID="660e8400-e29b-41d4-a716-446655440000"
JWT_TOKEN="your-jwt-token-here"

# Get cart
curl -X GET "http://localhost:8080/api/cart/user/$USER_ID" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Add item to cart
curl -X POST "http://localhost:8080/api/cart/user/$USER_ID/items" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{\"productId\":\"$PRODUCT_ID\",\"quantity\":2}"

# Update item quantity
curl -X PUT "http://localhost:8080/api/cart/user/$USER_ID/items/ITEM-ID?quantity=5" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Remove item
curl -X DELETE "http://localhost:8080/api/cart/user/$USER_ID/items/ITEM-ID" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Clear cart
curl -X DELETE "http://localhost:8080/api/cart/user/$USER_ID" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 2. Automated Testing with Jest

```javascript
// cart.test.js
describe('Cart API', () => {
  let authToken;
  let userId;
  let productId;

  beforeAll(async () => {
    // Setup: Login and get token
    const loginResponse = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: 'test@example.com',
        password: 'password'
      })
    });
    
    const { token, user } = await loginResponse.json();
    authToken = token;
    userId = user.id;
    
    // Get a test product
    const productsResponse = await fetch('/api/products?page=0&size=1');
    const products = await productsResponse.json();
    productId = products.data.content[0].id;
  });

  test('should add item to cart', async () => {
    const response = await fetch(`/api/cart/user/${userId}/items`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      },
      body: JSON.stringify({
        productId: productId,
        quantity: 2
      })
    });

    expect(response.ok).toBe(true);
    
    const result = await response.json();
    expect(result.success).toBe(true);
    expect(result.data.items).toHaveLength(1);
    expect(result.data.items[0].quantity).toBe(2);
  });

  test('should get cart', async () => {
    const response = await fetch(`/api/cart/user/${userId}`, {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });

    expect(response.ok).toBe(true);
    
    const result = await response.json();
    expect(result.success).toBe(true);
    expect(result.data.userId).toBe(userId);
  });

  test('should update item quantity', async () => {
    // First add an item
    const addResponse = await fetch(`/api/cart/user/${userId}/items`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      },
      body: JSON.stringify({
        productId: productId,
        quantity: 1
      })
    });
    
    const addResult = await addResponse.json();
    const itemId = addResult.data.items[0].id;

    // Update quantity
    const updateResponse = await fetch(
      `/api/cart/user/${userId}/items/${itemId}?quantity=5`,
      {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      }
    );

    expect(updateResponse.ok).toBe(true);
    
    const updateResult = await updateResponse.json();
    expect(updateResult.success).toBe(true);
    expect(updateResult.data.items[0].quantity).toBe(5);
  });
});
```

### 3. Integration Testing Checklist

- [ ] Add item to cart with valid data
- [ ] Add item to cart with invalid UUID format
- [ ] Add item to cart with quantity < 1
- [ ] Add item to cart with non-existent product
- [ ] Add item to cart with inactive product
- [ ] Add item to cart with insufficient stock
- [ ] Update item quantity to valid value
- [ ] Update item quantity to invalid value (< 1)
- [ ] Update non-existent item
- [ ] Remove existing item
- [ ] Remove non-existent item
- [ ] Clear cart with items
- [ ] Clear empty cart
- [ ] Get cart with items
- [ ] Get empty cart
- [ ] All operations with invalid/missing JWT
- [ ] All operations with expired JWT

## Best Practices

1. **Always validate inputs** before making API calls
2. **Handle authentication** properly with token refresh
3. **Implement proper error handling** with user-friendly messages
4. **Use optimistic updates** for better UX
5. **Implement loading states** during API calls
6. **Cache cart data** appropriately to reduce API calls
7. **Validate stock availability** before adding items
8. **Use proper UUID formatting** in all requests
9. **Implement retry logic** for network failures
10. **Log errors** for debugging purposes

## Summary

This guide provides comprehensive coverage of cart data persistence using REST endpoints. The key points to remember are:

1. **Authentication is required** for all cart operations
2. **UUID format must be valid** for all ID parameters
3. **Quantity must be >= 1** for all operations
4. **Products must exist and be active** to be added to cart
5. **Stock is validated** before adding items
6. **Cart is created automatically** when first item is added
7. **Quantities are accumulated** when adding existing items
8. **All operations return updated cart state** for UI consistency

Follow the examples and best practices in this guide to ensure reliable cart functionality in your frontend application.
