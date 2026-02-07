package com.miracle.smart_ecommerce_api_v1.repository;

import com.miracle.smart_ecommerce_api_v1.domain.WishlistItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Wishlist operations.
 */
public interface WishlistRepository {

    /**
     * Save a new wishlist item
     */
    WishlistItem save(WishlistItem item);

    /**
     * Find wishlist item by ID
     */
    Optional<WishlistItem> findById(UUID id);

    /**
     * Find all wishlist items for a user
     */
    List<WishlistItem> findByUserId(UUID userId);

    /**
     * Find all wishlist items for a user with pagination
     */
    List<WishlistItem> findByUserId(UUID userId, int page, int size);

    /**
     * Find wishlist item by user ID and product ID
     */
    Optional<WishlistItem> findByUserIdAndProductId(UUID userId, UUID productId);

    /**
     * Check if item exists by ID
     */
    boolean existsById(UUID id);

    /**
     * Check if product is in user's wishlist
     */
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    /**
     * Delete wishlist item by ID
     */
    void deleteById(UUID id);

    /**
     * Delete wishlist item by user ID and product ID
     */
    void deleteByUserIdAndProductId(UUID userId, UUID productId);

    /**
     * Delete all wishlist items for a user
     */
    void deleteByUserId(UUID userId);

    /**
     * Count wishlist items for a user
     */
    long countByUserId(UUID userId);
}

