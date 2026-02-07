package com.miracle.smart_ecommerce_api_v1.service;

import com.miracle.smart_ecommerce_api_v1.common.response.PageResponse;
import com.miracle.smart_ecommerce_api_v1.dto.request.AddToCartRequest;
import com.miracle.smart_ecommerce_api_v1.dto.response.CartResponse;

import java.util.UUID;

/**
 * Service interface for Cart operations.
 */
public interface CartService {

    /**
     * Get all carts with pagination
     */
    PageResponse<CartResponse> getAllCarts(int page, int size);

    /**
     * Get cart by user ID (creates one if not exists)
     */
    CartResponse getCartByUserId(UUID userId);

    /**
     * Add item to cart
     */
    CartResponse addItemToCart(UUID userId, AddToCartRequest request);

    /**
     * Update item quantity
     */
    CartResponse updateItemQuantity(UUID userId, UUID itemId, int quantity);

    /**
     * Remove item from cart
     */
    CartResponse removeItemFromCart(UUID userId, UUID itemId);

    /**
     * Clear cart
     */
    void clearCart(UUID userId);

    /**
     * Get cart item count
     */
    int getCartItemCount(UUID userId);
}

