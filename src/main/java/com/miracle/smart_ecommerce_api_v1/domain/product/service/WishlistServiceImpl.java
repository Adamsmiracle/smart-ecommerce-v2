package com.miracle.smart_ecommerce_api_v1.domain.product.service;

import com.miracle.smart_ecommerce_api_v1.common.response.PageResponse;
import com.miracle.smart_ecommerce_api_v1.domain.product.entity.Product;
import com.miracle.smart_ecommerce_api_v1.domain.product.entity.WishlistItem;
import com.miracle.smart_ecommerce_api_v1.domain.order.service.cart.dto.AddToCartRequest;
import com.miracle.smart_ecommerce_api_v1.domain.product.dto.AddToWishlistRequest;
import com.miracle.smart_ecommerce_api_v1.domain.product.dto.WishlistItemResponse;
import com.miracle.smart_ecommerce_api_v1.exception.DuplicateResourceException;
import com.miracle.smart_ecommerce_api_v1.exception.ResourceNotFoundException;
import com.miracle.smart_ecommerce_api_v1.domain.product.repository.ProductRepository;
import com.miracle.smart_ecommerce_api_v1.domain.user.repository.UserRepository;
import com.miracle.smart_ecommerce_api_v1.domain.product.repository.WishlistRepository;
import com.miracle.smart_ecommerce_api_v1.domain.order.service.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.miracle.smart_ecommerce_api_v1.config.CacheConfig.*;

/**
 * Implementation of WishlistService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;


    @Override
    @Transactional
    @CacheEvict(value = WISHLIST_CACHE, key = "#request.userId")
    public WishlistItemResponse addToWishlist(AddToWishlistRequest request) {
        log.info("Adding product {} to wishlist for user {}", request.getProductId(), request.getUserId());

        // Verify user exists
        if (!userRepository.existsById(request.getUserId())) {
            throw ResourceNotFoundException.forResource("User", request.getUserId());
        }

        // Verify product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> ResourceNotFoundException.forResource("Product", request.getProductId()));

        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndProductId(request.getUserId(), request.getProductId())) {
            throw new DuplicateResourceException("Wishlist item", "product", request.getProductId().toString());
        }

        WishlistItem item = WishlistItem.builder()
                .userId(request.getUserId())
                .productId(request.getProductId())
                .build();

        WishlistItem savedItem = wishlistRepository.save(item);
        savedItem.setProduct(product);

        log.info("Product added to wishlist successfully with ID: {}", savedItem.getId());
        return mapToResponse(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    // @Cacheable(value = WISHLIST_CACHE, key = "#userId") // Temporarily disabled for debugging
    public List<WishlistItemResponse> getWishlistByUserId(UUID userId) {
        log.info("Getting wishlist for user: {}", userId);
        List<WishlistItem> items = wishlistRepository.findByUserId(userId);
        log.info("Found {} wishlist items for user: {}", items.size(), userId);
        
        List<WishlistItemResponse> responses = items.stream()
                .map(this::loadProductAndMapToResponse)
                .collect(Collectors.toList());
        
        log.info("Returning {} wishlist item responses for user: {}", responses.size(), userId);
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WishlistItemResponse> getWishlistByUserId(UUID userId, int page, int size) {
        log.debug("Getting wishlist for user: {} - page: {}, size: {}", userId, page, size);
        List<WishlistItem> items = wishlistRepository.findByUserId(userId, page, size);
        long total = wishlistRepository.countByUserId(userId);

        List<WishlistItemResponse> responses = items.stream()
                .map(this::loadProductAndMapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWishlist(UUID userId, UUID productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional
    @CacheEvict(value = WISHLIST_CACHE, allEntries = true)
    public void removeFromWishlist(UUID id) {
        log.info("Removing wishlist item: {}", id);
        if (!wishlistRepository.existsById(id)) {
            throw ResourceNotFoundException.forResource("Wishlist item", id);
        }
        wishlistRepository.deleteById(id);
        log.info("Wishlist item removed successfully: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = WISHLIST_CACHE, key = "#userId")
    public void removeFromWishlist(UUID userId, UUID productId) {
        log.info("Removing product {} from wishlist for user {}", productId, userId);
        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResourceNotFoundException("Wishlist item", "user-product",
                    userId + "-" + productId);
        }
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
        log.info("Product removed from wishlist successfully");
    }

    @Override
    @Transactional
    @CacheEvict(value = WISHLIST_CACHE, key = "#userId")
    public void clearWishlist(UUID userId) {
        log.info("Clearing wishlist for user: {}", userId);
        wishlistRepository.deleteByUserId(userId);
        log.info("Wishlist cleared successfully for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getWishlistCount(UUID userId) {
        return wishlistRepository.countByUserId(userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = WISHLIST_CACHE, key = "#userId")
    public void moveToCart(UUID userId, UUID productId) {
        log.info("Moving product {} from wishlist to cart for user {}", productId, userId);

        // Verify item is in wishlist
        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResourceNotFoundException("Wishlist item", "user-product",
                    userId + "-" + productId);
        }

        // Add to cart
        AddToCartRequest cartRequest = AddToCartRequest.builder()
                .productId(productId)
                .quantity(1)
                .build();
        cartService.addItemToCart(userId, cartRequest);

        // Remove from wishlist
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);

        log.info("Product moved from wishlist to cart successfully");
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private WishlistItemResponse loadProductAndMapToResponse(WishlistItem item) {
        log.debug("Loading product for wishlist item: {}, product ID: {}", item.getId(), item.getProductId());
        productRepository.findById(item.getProductId())
                .ifPresentOrElse(
                    product -> {
                        log.debug("Found product {} for wishlist item {}", product.getName(), item.getId());
                        item.setProduct(product);
                    },
                    () -> log.warn("Product not found for wishlist item: {}, product ID: {}", item.getId(), item.getProductId())
                );
        return mapToResponse(item);
    }

    private WishlistItemResponse mapToResponse(WishlistItem item) {
        Product product = item.getProduct();

        WishlistItemResponse.WishlistItemResponseBuilder builder = WishlistItemResponse.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .productId(item.getProductId())
                .createdAt(OffsetDateTime.from(item.getCreatedAt()));

        if (product != null) {
            builder.productName(product.getName())
                    .productSku(product.getSku())
                    .productPrice(product.getPrice())
                    .productInStock(product.getStockQuantity() != null && product.getStockQuantity() > 0);

            // Get first image if available
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                builder.productImage(product.getImages().get(0));
            }
        }

        return builder.build();
    }
}

