package com.miracle.smart_ecommerce_api_v1.service;

import com.miracle.smart_ecommerce_api_v1.common.response.PageResponse;
import com.miracle.smart_ecommerce_api_v1.dto.request.AddToWishlistRequest;
import com.miracle.smart_ecommerce_api_v1.dto.response.WishlistItemResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Wishlist operations.
 */
public interface WishlistService {

    /**
     * Add item to wishlist
     */
    WishlistItemResponse addToWishlist(AddToWishlistRequest request);

    /**
     * Get all wishlist items for a user
     */
    List<WishlistItemResponse> getWishlistByUserId(UUID userId);

    /**
     * Get wishlist items for a user with pagination
     */
    PageResponse<WishlistItemResponse> getWishlistByUserId(UUID userId, int page, int size);

    /**
     * Check if product is in user's wishlist
     */
    boolean isInWishlist(UUID userId, UUID productId);

    /**
     * Remove item from wishlist by ID
     */
    void removeFromWishlist(UUID id);

    /**
     * Remove product from user's wishlist
     */
    void removeFromWishlist(UUID userId, UUID productId);

    /**
     * Clear user's wishlist
     */
    void clearWishlist(UUID userId);

    /**
     * Get wishlist count for a user
     */
    long getWishlistCount(UUID userId);

    /**
     * Move wishlist item to cart
     */
    void moveToCart(UUID userId, UUID productId);
}

