package com.miracle.smart_ecommerce_api_v1.service.impl;

import com.miracle.smart_ecommerce_api_v1.common.response.PageResponse;
import com.miracle.smart_ecommerce_api_v1.domain.Product;
import com.miracle.smart_ecommerce_api_v1.domain.WishlistItem;
import com.miracle.smart_ecommerce_api_v1.dto.request.AddToCartRequest;
import com.miracle.smart_ecommerce_api_v1.dto.request.AddToWishlistRequest;
import com.miracle.smart_ecommerce_api_v1.dto.response.WishlistItemResponse;
import com.miracle.smart_ecommerce_api_v1.exception.DuplicateResourceException;
import com.miracle.smart_ecommerce_api_v1.exception.ResourceNotFoundException;
import com.miracle.smart_ecommerce_api_v1.repository.ProductRepository;
import com.miracle.smart_ecommerce_api_v1.repository.UserRepository;
import com.miracle.smart_ecommerce_api_v1.repository.WishlistRepository;
import com.miracle.smart_ecommerce_api_v1.service.CartService;
import com.miracle.smart_ecommerce_api_v1.service.WishlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of WishlistService.
 */
@Service
public class WishlistServiceImpl implements WishlistService {

    private static final Logger log = LoggerFactory.getLogger(WishlistServiceImpl.class);

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               UserRepository userRepository,
                               ProductRepository productRepository,
                               CartService cartService) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @Override
    @Transactional
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
    public List<WishlistItemResponse> getWishlistByUserId(UUID userId) {
        log.debug("Getting wishlist for user: {}", userId);
        List<WishlistItem> items = wishlistRepository.findByUserId(userId);
        return items.stream()
                .map(this::loadProductAndMapToResponse)
                .collect(Collectors.toList());
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
        productRepository.findById(item.getProductId())
                .ifPresent(item::setProduct);
        return mapToResponse(item);
    }

    private WishlistItemResponse mapToResponse(WishlistItem item) {
        Product product = item.getProduct();

        WishlistItemResponse.WishlistItemResponseBuilder builder = WishlistItemResponse.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .productId(item.getProductId())
                .createdAt(item.getCreatedAt());

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

