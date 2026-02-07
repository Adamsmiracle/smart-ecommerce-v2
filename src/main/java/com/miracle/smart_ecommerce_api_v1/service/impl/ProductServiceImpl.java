package com.miracle.smart_ecommerce_api_v1.service.impl;

import com.miracle.smart_ecommerce_api_v1.common.response.PageResponse;
import com.miracle.smart_ecommerce_api_v1.domain.Category;
import com.miracle.smart_ecommerce_api_v1.domain.Product;
import com.miracle.smart_ecommerce_api_v1.dto.request.CreateProductRequest;
import com.miracle.smart_ecommerce_api_v1.dto.response.ProductResponse;
import com.miracle.smart_ecommerce_api_v1.exception.DuplicateResourceException;
import com.miracle.smart_ecommerce_api_v1.exception.ResourceNotFoundException;
import com.miracle.smart_ecommerce_api_v1.repository.CategoryRepository;
import com.miracle.smart_ecommerce_api_v1.repository.ProductRepository;
import com.miracle.smart_ecommerce_api_v1.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ProductService using raw JDBC.
 */
@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

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

        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        log.debug("Getting product by ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Product", id));
        return mapToResponseWithCategory(product);
    }

    @Override
    @Transactional(readOnly = true)
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

        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        log.info("Deleting product with ID: {}", id);
        if (!productRepository.existsById(id)) {
            throw ResourceNotFoundException.forResource("Product", id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public void activateProduct(UUID id) {
        log.info("Activating product with ID: {}", id);
        productRepository.setActiveStatus(id, true);
    }

    @Override
    @Transactional
    public void deactivateProduct(UUID id) {
        log.info("Deactivating product with ID: {}", id);
        productRepository.setActiveStatus(id, false);
    }

    @Override
    @Transactional
    public void updateStock(UUID id, int quantity) {
        log.info("Updating stock for product {} by {}", id, quantity);
        productRepository.updateStock(id, quantity);
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
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductResponse mapToResponseWithCategory(Product product) {
        ProductResponse response = mapToResponse(product);

        // Fetch category name
        categoryRepository.findById(product.getCategoryId())
                .ifPresent(category -> response.setCategoryName(category.getCategoryName()));

        return response;
    }
}

