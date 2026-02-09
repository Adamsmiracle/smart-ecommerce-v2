# Spring Boot Caching Implementation

## Overview

This document describes the caching strategy implemented in the Smart E-Commerce API using Spring Boot's caching abstraction.

## Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

## Configuration

### CacheConfig.java

Located at: `com.miracle.smart_ecommerce_api_v1.config.CacheConfig`

**Strategy: One cache per entity type** to avoid data duplication and reduce memory usage.

```java
@Configuration
@EnableCaching
public class CacheConfig {
    // Cache names - one per entity type
    public static final String USERS_CACHE = "users";
    public static final String PRODUCTS_CACHE = "products";
    public static final String CATEGORIES_CACHE = "categories";
    public static final String ORDERS_CACHE = "orders";
    public static final String CART_CACHE = "cart";
    public static final String ADDRESSES_CACHE = "addresses";
    public static final String REVIEWS_CACHE = "reviews";
    public static final String WISHLIST_CACHE = "wishlist";
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(...);
    }
}
```

### Why Single Cache Per Entity?

❌ **Bad approach (what we avoided):**
```java
// Multiple caches for same entity = data duplication!
public static final String USERS_CACHE = "users";
public static final String USER_BY_ID_CACHE = "userById";
public static final String USER_BY_EMAIL_CACHE = "userByEmail";
```
- Same user stored in 3 different caches
- Wastes memory
- Cache inconsistency risks

✅ **Good approach (what we implemented):**
```java
// Single cache with prefixed keys
public static final String USERS_CACHE = "users";

@Cacheable(value = USERS_CACHE, key = "'id:' + #id")
public UserResponse getUserById(UUID id) { ... }

@Cacheable(value = USERS_CACHE, key = "'email:' + #email")
public UserResponse getUserByEmail(String email) { ... }
```
- One cache, different keys with prefixes
- No data duplication
- Single point of eviction

### application.yaml

```yaml
spring:
  cache:
    type: simple  # Use simple in-memory cache
```

## Cache Annotations Used

### @Cacheable
Caches the result of a method. Uses key prefix to differentiate lookup types.

```java
@Cacheable(value = USERS_CACHE, key = "'id:' + #id")
public UserResponse getUserById(UUID id) { ... }

@Cacheable(value = USERS_CACHE, key = "'email:' + #email")
public UserResponse getUserByEmail(String email) { ... }
```

### @CacheEvict
Removes all entries from cache when data is modified.

```java
@CacheEvict(value = USERS_CACHE, allEntries = true)
public UserResponse createUser(CreateUserRequest request) { ... }
```

### @Caching
Used only when multiple entity caches need eviction (e.g., cancelOrder affects orders AND products).

```java
@Caching(evict = {
    @CacheEvict(value = ORDERS_CACHE, allEntries = true),
    @CacheEvict(value = PRODUCTS_CACHE, allEntries = true)
})
public OrderResponse cancelOrder(UUID id) { ... }
```

## Services with Caching

### UserServiceImpl
| Method | Operation | Cache | Key |
|--------|-----------|-------|-----|
| `getUserById` | @Cacheable | USERS_CACHE | `'id:' + #id` |
| `getUserByEmail` | @Cacheable | USERS_CACHE | `'email:' + #email` |
| `createUser` | @CacheEvict | USERS_CACHE | allEntries |
| `updateUser` | @CacheEvict | USERS_CACHE | allEntries |
| `deleteUser` | @CacheEvict | USERS_CACHE | allEntries |
| `activateUser` | @CacheEvict | USERS_CACHE | allEntries |
| `deactivateUser` | @CacheEvict | USERS_CACHE | allEntries |

### ProductServiceImpl
| Method | Operation | Cache | Key |
|--------|-----------|-------|-----|
| `getProductById` | @Cacheable | PRODUCTS_CACHE | `'id:' + #id` |
| `getProductBySku` | @Cacheable | PRODUCTS_CACHE | `'sku:' + #sku` |
| `createProduct` | @CacheEvict | PRODUCTS_CACHE | allEntries |
| `updateProduct` | @CacheEvict | PRODUCTS_CACHE | allEntries |
| `deleteProduct` | @CacheEvict | PRODUCTS_CACHE | allEntries |
| `activateProduct` | @CacheEvict | PRODUCTS_CACHE | allEntries |
| `deactivateProduct` | @CacheEvict | PRODUCTS_CACHE | allEntries |
| `updateStock` | @CacheEvict | PRODUCTS_CACHE | allEntries |

### CategoryServiceImpl
| Method | Operation | Cache | Key |
|--------|-----------|-------|-----|
| `getCategoryById` | @Cacheable | CATEGORIES_CACHE | `'id:' + #id` |
| `getAllCategories` | @Cacheable | CATEGORIES_CACHE | default |
| `createCategory` | @CacheEvict | CATEGORIES_CACHE | allEntries |
| `updateCategory` | @CacheEvict | CATEGORIES_CACHE | allEntries |
| `deleteCategory` | @CacheEvict | CATEGORIES_CACHE | allEntries |

### OrderServiceImpl
| Method | Operation | Cache | Key |
|--------|-----------|-------|-----|
| `getOrderById` | @Cacheable | ORDERS_CACHE | `'id:' + #id` |
| `createOrder` | @CacheEvict | ORDERS_CACHE | allEntries |
| `updateOrderStatus` | @CacheEvict | ORDERS_CACHE | allEntries |
| `updatePaymentStatus` | @CacheEvict | ORDERS_CACHE | allEntries |
| `cancelOrder` | @Caching | ORDERS_CACHE, PRODUCTS_CACHE | allEntries |
| `deleteOrder` | @CacheEvict | ORDERS_CACHE | allEntries |

### CartServiceImpl
| Method | Operation | Cache | Key |
|--------|-----------|-------|-----|
| `getCartByUserId` | @Cacheable | CART_CACHE | `#userId` |
| `addItemToCart` | @CacheEvict | CART_CACHE | `#userId` |
| `updateItemQuantity` | @CacheEvict | CART_CACHE | `#userId` |
| `removeItemFromCart` | @CacheEvict | CART_CACHE | `#userId` |
| `clearCart` | @CacheEvict | CART_CACHE | `#userId` |

### AddressServiceImpl
| Method | Operation | Cache | Key |
|--------|-----------|-------|-----|
| `getAddressById` | @Cacheable | ADDRESSES_CACHE | `'id:' + #id` |
| `createAddress` | @CacheEvict | ADDRESSES_CACHE | allEntries |
| `updateAddress` | @CacheEvict | ADDRESSES_CACHE | allEntries |
| `setDefaultAddress` | @CacheEvict | ADDRESSES_CACHE | allEntries |
| `deleteAddress` | @CacheEvict | ADDRESSES_CACHE | allEntries |

### ReviewServiceImpl
| Method | Operation | Cache | Key |
|--------|-----------|-------|-----|
| `createReview` | @CacheEvict | REVIEWS_CACHE | allEntries |
| `updateReview` | @CacheEvict | REVIEWS_CACHE | allEntries |
| `deleteReview` | @CacheEvict | REVIEWS_CACHE | allEntries |

### WishlistServiceImpl
| Method | Operation | Cache | Key |
|--------|-----------|-------|-----|
| `getWishlistByUserId` | @Cacheable | WISHLIST_CACHE | `#userId` |
| `addToWishlist` | @CacheEvict | WISHLIST_CACHE | `#request.userId` |
| `removeFromWishlist` | @CacheEvict | WISHLIST_CACHE | allEntries |
| `clearWishlist` | @CacheEvict | WISHLIST_CACHE | `#userId` |
| `moveToCart` | @CacheEvict | WISHLIST_CACHE | `#userId` |

## Best Practices Implemented

1. **One cache per entity type** - Avoids data duplication
2. **Key prefixes** - `'id:' + #id`, `'email:' + #email` differentiate lookups in same cache
3. **User-specific caching** - Cart and Wishlist use userId as key
4. **allEntries eviction** - Simplifies cache invalidation, ensures consistency
5. **Cross-entity eviction** - cancelOrder evicts both ORDERS and PRODUCTS caches

## Memory Optimization

| Approach | Caches | Memory Usage |
|----------|--------|--------------|
| ❌ Multiple caches per entity | 15+ | High (duplicated data) |
| ✅ Single cache per entity | 8 | Low (no duplication) |

## Production Recommendations

1. **Use Redis for distributed caching**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

2. **Configure TTL (Time-To-Live)**:
```yaml
spring:
  cache:
    redis:
      time-to-live: 3600000  # 1 hour
```

3. **Use Caffeine for advanced local caching** with size limits and eviction policies

4. **Monitor cache hit/miss ratio** via Spring Actuator

