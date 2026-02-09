package com.miracle.smart_ecommerce_api_v1.domain.product.service;

import com.miracle.smart_ecommerce_api_v1.common.response.PageResponse;
import com.miracle.smart_ecommerce_api_v1.domain.product.entity.Product;
import com.miracle.smart_ecommerce_api_v1.domain.product.dto.CreateProductRequest;
import com.miracle.smart_ecommerce_api_v1.domain.product.dto.ProductResponse;
import com.miracle.smart_ecommerce_api_v1.exception.DuplicateResourceException;
import com.miracle.smart_ecommerce_api_v1.exception.ResourceNotFoundException;
import com.miracle.smart_ecommerce_api_v1.domain.category.repository.CategoryRepository;
import com.miracle.smart_ecommerce_api_v1.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.miracle.smart_ecommerce_api_v1.config.CacheConfig.*;

/**
 * Implementation of ProductService using raw JDBC.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CacheManager cacheManager;


    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product: {}", request.getName());

        // Validate category exists
        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw ResourceNotFoundException.forResource("Category", request.getCategoryId());
        }

        // Check if SKU already exists
        if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product", "sku", request.getSku());
        }

        Product product = Product.builder()
                .categoryId(request.getCategoryId())
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .images(request.getImages())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        ProductResponse response = mapToResponse(savedProduct);

        // Update caches with new product
        Cache byIdCache = cacheManager.getCache(PRODUCTS_CACHE);
        if (byIdCache != null) {
            byIdCache.put("id:" + savedProduct.getId(), response);
        }

        if (savedProduct.getSku() != null) {
            Cache bySkuCache = cacheManager.getCache(PRODUCTS_CACHE);
            if (bySkuCache != null) {
                bySkuCache.put("sku:" + savedProduct.getSku(), response);
            }
        }

        // Clear list/search caches
        evictCache(PRODUCTS_CACHE);
        evictCache(PRODUCTS_CACHE);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = PRODUCTS_CACHE, key = "'id:' + #id")
    public ProductResponse getProductById(UUID id) {
        log.debug("Getting product by ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Product", id));
        return mapToResponseWithCategory(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = PRODUCTS_CACHE, key = "'sku:' + #sku")
    public ProductResponse getProductBySku(String sku) {
        log.debug("Getting product by SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
        return mapToResponseWithCategory(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(int page, int size) {
        log.debug("Getting all products - page: {}, size: {}", page, size);
        List<Product> products = productRepository.findAll(page, size);
        long total = productRepository.count();

        List<ProductResponse> responses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getActiveProducts(int page, int size) {
        log.debug("Getting active products - page: {}, size: {}", page, size);
        List<Product> products = productRepository.findActiveProducts(page, size);
        long total = productRepository.countActive();

        List<ProductResponse> responses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByCategory(UUID categoryId, int page, int size) {
        log.debug("Getting products by category: {} - page: {}, size: {}", categoryId, page, size);

        if (!categoryRepository.existsById(categoryId)) {
            throw ResourceNotFoundException.forResource("Category", categoryId);
        }

        List<Product> products = productRepository.findByCategoryId(categoryId, page, size);
        long total = productRepository.countByCategoryId(categoryId);

        List<ProductResponse> responses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchProducts(String keyword, int page, int size) {
        log.debug("Searching products with keyword: {} - page: {}, size: {}", keyword, page, size);
        List<Product> products = productRepository.search(keyword, page, size);
        long total = productRepository.countActive();

        List<ProductResponse> responses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        log.debug("Getting products by price range: {} - {} - page: {}, size: {}", minPrice, maxPrice, page, size);
        List<Product> products = productRepository.findByPriceRange(minPrice, maxPrice, page, size);
        long total = productRepository.countActive();

        List<ProductResponse> responses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsInStock(int page, int size) {
        log.debug("Getting products in stock - page: {}, size: {}", page, size);
        List<Product> products = productRepository.findInStock(page, size);
        long total = productRepository.countActive();

        List<ProductResponse> responses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, page, size, total);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID id, CreateProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Product", id));

        String oldSku = existingProduct.getSku();

        // Validate category exists
        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw ResourceNotFoundException.forResource("Category", request.getCategoryId());
        }

        // Check if SKU is being changed to an existing one
        if (request.getSku() != null && !request.getSku().equals(existingProduct.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product", "sku", request.getSku());
        }

        existingProduct.setCategoryId(request.getCategoryId());
        existingProduct.setSku(request.getSku());
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setStockQuantity(request.getStockQuantity());
        existingProduct.setIsActive(request.getIsActive());
        existingProduct.setImages(request.getImages());

        Product updatedProduct = productRepository.update(existingProduct);
        log.info("Product updated successfully: {}", id);

        ProductResponse response = mapToResponse(updatedProduct);

        // Update id cache
        Cache byIdCache = cacheManager.getCache(PRODUCTS_CACHE);
        if (byIdCache != null) {
            byIdCache.put("id:" + id, response);
        }

        // Update SKU cache - evict old SKU if changed
        Cache bySkuCache = cacheManager.getCache(PRODUCTS_CACHE);
        if (bySkuCache != null) {
            if (oldSku != null && !oldSku.equals(updatedProduct.getSku())) {
                bySkuCache.evict("sku:" + oldSku);
            }
            if (updatedProduct.getSku() != null) {
                bySkuCache.put("sku:" + updatedProduct.getSku(), response);
            }
        }

        // Clear list/search caches
        evictCache(PRODUCTS_CACHE);
        evictCache(PRODUCTS_CACHE);

        return response;
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Product", id));

        productRepository.deleteById(id);
        log.info("Product deleted successfully: {}", id);

        // Evict from id and SKU caches
        Cache byIdCache = cacheManager.getCache(PRODUCTS_CACHE);
        if (byIdCache != null) {
            byIdCache.evict("id:" + id);
        }

        if (product.getSku() != null) {
            Cache bySkuCache = cacheManager.getCache(PRODUCTS_CACHE);
            if (bySkuCache != null) {
                bySkuCache.evict("sku:" + product.getSku());
            }
        }

        // Clear list/search caches
        evictCache(PRODUCTS_CACHE);
        evictCache(PRODUCTS_CACHE);
    }

    @Override
    @Transactional
    public void activateProduct(UUID id) {
        log.info("Activating product with ID: {}", id);
        productRepository.setActiveStatus(id, true);

        // Evict caches - product status changed
        evictProductCaches(id);
    }

    @Override
    @Transactional
    public void deactivateProduct(UUID id) {
        log.info("Deactivating product with ID: {}", id);
        productRepository.setActiveStatus(id, false);

        // Evict caches - product status changed
        evictProductCaches(id);
    }

    @Override
    @Transactional
    public void updateStock(UUID id, int quantity) {
        log.info("Updating stock for product {} by {}", id, quantity);
        productRepository.updateStock(id, quantity);

        // Evict caches - stock changed
        evictProductCaches(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts() {
        return productRepository.count();
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .isActive(product.getIsActive())
                .inStock(product.isInStock())
                .images(product.getImages())
                .primaryImage(product.getPrimaryImage())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .createdAt(OffsetDateTime.from(product.getCreatedAt()))
                .updatedAt(OffsetDateTime.from(product.getUpdatedAt()))
                .build();
    }

    private ProductResponse mapToResponseWithCategory(Product product) {
        ProductResponse response = mapToResponse(product);

        // Fetch category name
        categoryRepository.findById(product.getCategoryId())
                .ifPresent(category -> response.setCategoryName(category.getCategoryName()));

        return response;
    }

    /**
     * Helper method to evict all entries from a cache
     */
    private void evictCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Helper method to evict product from all caches
     */
    private void evictProductCaches(UUID productId) {
        Cache byIdCache = cacheManager.getCache(PRODUCTS_CACHE);
        if (byIdCache != null) {
            byIdCache.evict("id:" + productId);
        }

        // Get product to evict SKU cache
        productRepository.findById(productId).ifPresent(product -> {
            if (product.getSku() != null) {
                Cache bySkuCache = cacheManager.getCache(PRODUCTS_CACHE);
                if (bySkuCache != null) {
                    bySkuCache.evict("sku:" + product.getSku());
                }
            }
        });

        // Clear list/search caches
        evictCache(PRODUCTS_CACHE);
        evictCache(PRODUCTS_CACHE);
    }
}

