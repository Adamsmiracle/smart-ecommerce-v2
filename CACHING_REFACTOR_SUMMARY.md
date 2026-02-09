# Caching Refactor Summary

## Overview
Refactored caching strategy across the entire application to use targeted cache updates/evictions via `CacheManager` instead of broad `allEntries = true` evictions. Implemented separate caches per query shape (byId, byEmail, list, search) using Caffeine cache provider.

## Changes Made

### 1. Dependencies Added
**File**: `pom.xml`
- Added `com.github.ben-manes.caffeine:caffeine` dependency for advanced caching with TTL and size limits

### 2. Cache Configuration
**File**: `src/main/java/com/miracle/smart_ecommerce_api_v1/config/CacheConfig.java`

**Changes**:
- Replaced `ConcurrentMapCacheManager` with `CaffeineCacheManager` using `SimpleCacheManager`
- Created separate cache names per query shape:
  - **Entity caches** (byId, byEmail, bySku): 1000 entries max, 60-minute TTL
  - **List/search caches**: 200 entries max, 5-minute TTL
  
**New Cache Names**:
- Users: `usersById`, `usersByEmail`, `usersList`, `usersSearch`
- Products: `productsById`, `productsBySku`, `productsList`, `productsSearch`
- Categories: `categoriesById`, `categoriesList`
- Orders: `ordersById`, `ordersByNumber`, `ordersList`
- Addresses: `addressesById`, `addressesList`
- Cart: `cartByUser`
- Reviews: `reviewsById`, `reviewsList`
- Wishlist: `wishlistByUser`

**Deprecated Cache Names** (for backward compatibility):
- `USERS_CACHE`, `PRODUCTS_CACHE`, `CATEGORIES_CACHE`, `ORDERS_CACHE`, `ADDRESSES_CACHE`, `REVIEWS_CACHE`

### 3. Service Layer Refactoring

#### UserServiceImpl
**File**: `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/user/service/impl/UserServiceImpl.java`

**Changes**:
- Injected `CacheManager` via constructor
- Removed all `@CacheEvict(value = USERS_CACHE, allEntries = true)` annotations
- **Read methods**:
  - `getUserById`: Uses `@Cacheable(value = USERS_BY_ID, key = "'id:' + #id")`
  - `getUserByEmail`: Uses `@Cacheable(value = USERS_BY_EMAIL, key = "'email:' + #email")`
- **Write methods**:
  - `createUser`: Puts new user in `usersById` and `usersByEmail`; clears `usersList` and `usersSearch`
  - `updateUser`: Updates `usersById` and `usersByEmail`; evicts old email key if changed; clears list/search caches
  - `deleteUser`: Evicts specific id and email keys; clears list/search caches
  - `activateUser/deactivateUser`: Evicts specific id and email keys; clears list/search caches

#### ProductServiceImpl
**File**: `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/product/service/ProductServiceImpl.java`

**Changes**:
- Injected `CacheManager` via constructor
- Removed all `@CacheEvict(value = PRODUCTS_CACHE, allEntries = true)` annotations
- **Read methods**:
  - `getProductById`: Uses `@Cacheable(value = PRODUCTS_BY_ID, key = "'id:' + #id")`
  - `getProductBySku`: Uses `@Cacheable(value = PRODUCTS_BY_SKU, key = "'sku:' + #sku")`
- **Write methods**:
  - `createProduct`: Puts new product in `productsById` and `productsBySku`; clears `productsList` and `productsSearch`
  - `updateProduct`: Updates `productsById` and `productsBySku`; evicts old SKU key if changed; clears list/search caches
  - `deleteProduct`: Evicts specific id and SKU keys; clears list/search caches
  - `activateProduct/deactivateProduct/updateStock`: Evicts product caches via helper method

#### CategoryServiceImpl
**File**: `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/category/service/CategoryServiceImpl.java`

**Changes**:
- Injected `CacheManager` via constructor
- Removed all `@CacheEvict(value = CATEGORIES_CACHE, allEntries = true)` annotations
- **Read methods**:
  - `getCategoryById`: Uses `@Cacheable(value = CATEGORIES_BY_ID, key = "'id:' + #id")`
- **Write methods**:
  - `createCategory`: Puts new category in `categoriesById`; clears `categoriesList`
  - `updateCategory`: Updates `categoriesById`; clears `categoriesList`
  - `deleteCategory`: Evicts specific id key; clears `categoriesList`

#### OrderServiceImpl
**File**: `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/order/service/OrderServiceImpl.java`

**Changes**:
- Injected `CacheManager` via constructor
- Removed all `@CacheEvict` and `@Caching` annotations with `allEntries = true`
- **Read methods**:
  - `getOrderById`: Uses `@Cacheable(value = ORDERS_BY_ID, key = "'id:' + #id")`
- **Write methods**:
  - `createOrder`: Puts new order in `ordersById` and `ordersByNumber`; clears `ordersList`
  - `updateOrderStatus`: Updates `ordersById` and `ordersByNumber`; clears `ordersList`
  - `updatePaymentStatus`: Updates `ordersById` and `ordersByNumber`; clears `ordersList`
  - `cancelOrder`: Updates order caches; evicts affected product caches; clears list caches
  - `deleteOrder`: Evicts specific id and order number keys; clears `ordersList`

#### WishlistServiceImpl
**File**: `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/product/service/WishlistServiceImpl.java`

**Changes**:
- Replaced all `WISHLIST_CACHE` references with `WISHLIST_BY_USER`
- All cache annotations now use `@Cacheable(value = WISHLIST_BY_USER, key = "#userId")` or `@CacheEvict(value = WISHLIST_BY_USER, key = "#userId")`

#### CartServiceImpl
**File**: `src/main/java/com/miracle/smart_ecommerce_api_v1/domain/order/service/cart/service/CartServiceImpl.java`

**Changes**:
- Replaced all `CART_CACHE` references with `CART_BY_USER`
- All cache annotations now use `@Cacheable(value = CART_BY_USER, key = "#userId")` or `@CacheEvict(value = CART_BY_USER, key = "#userId")`

### 4. Helper Methods Added

Added `evictCache(String cacheName)` helper method to all modified service implementations:
```java
private void evictCache(String cacheName) {
    Cache cache = cacheManager.getCache(cacheName);
    if (cache != null) {
        cache.clear();
    }
}
```

Added `evictProductCaches(UUID productId)` helper method to `ProductServiceImpl` for consolidated cache eviction.

## Benefits

### 1. Memory Efficiency
- **Before**: Every write operation cleared entire entity cache (all users, all products, etc.)
- **After**: Only affected keys are updated/evicted
- **Impact**: Significant memory savings and reduced cache churn

### 2. Performance
- **Before**: Cache had to be rebuilt from DB after every write
- **After**: Only specific entries are refreshed; unaffected entries remain cached
- **Impact**: Reduced database load and faster response times

### 3. Cache Tuning
- **Entity caches**: Long TTL (60 min) for frequently accessed data
- **List caches**: Short TTL (5 min) for volatile, expensive queries
- **Size limits**: Prevents unbounded memory growth

### 4. Scalability
- Separate caches per query type allow independent tuning
- List caches expire quickly, preventing stale data in high-write scenarios
- Entity caches remain stable even under heavy write load

## Cache Strategy Summary

| Operation | Cache Action | Reasoning |
|-----------|-------------|-----------|
| **Create** | Put in byId/byEmail/bySku caches; clear list caches | New entity should be immediately available; lists need refresh |
| **Update** | Update byId/byEmail/bySku caches; evict old indexed keys; clear lists | Keep entity caches fresh; prevent stale indexed entries |
| **Delete** | Evict byId/byEmail/bySku keys; clear list caches | Remove deleted entity; force list refresh |
| **Status Change** | Evict byId/byEmail keys; clear list caches | Simpler than updating; forces fresh read |

## Testing Recommendations

1. **Cache Hit Verification**: Assert repository is called once, then cached on subsequent calls
2. **Cache Eviction Tests**: Verify old keys are evicted on indexed field changes (email, SKU)
3. **List Cache Refresh**: Verify list caches are cleared after creates/deletes
4. **TTL Tests**: Verify entity caches expire after 60 minutes, list caches after 5 minutes
5. **Size Limit Tests**: Verify caches respect maximum size constraints

## Migration Notes

- Old cache constant names are deprecated but still work (mapped to new byId caches)
- No API changes required
- Backward compatible with existing code
- Consider removing deprecated constants in next major version

## Configuration

To adjust cache settings, modify `CacheConfig.java`:
- **Entity cache**: `buildEntityCache()` - adjust `maximumSize` and `expireAfterWrite`
- **List cache**: `buildListCache()` - adjust `maximumSize` and `expireAfterWrite`

## Compilation Status

✅ **BUILD SUCCESS** - All services compiled without errors

