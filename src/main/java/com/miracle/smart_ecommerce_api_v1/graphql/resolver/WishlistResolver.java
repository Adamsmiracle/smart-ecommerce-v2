package com.miracle.smart_ecommerce_api_v1.graphql.resolver;

import com.miracle.smart_ecommerce_api_v1.domain.product.dto.AddToWishlistRequest;
import com.miracle.smart_ecommerce_api_v1.domain.product.dto.WishlistItemResponse;
import com.miracle.smart_ecommerce_api_v1.domain.product.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GraphQL Resolver for Wishlist entity.
 * Handles all wishlist-related queries and mutations.
 */
@Controller
@RequiredArgsConstructor
public class WishlistResolver {

    private final WishlistService wishlistService;

    // ========================================================================
    // WISHLIST QUERIES
    // ========================================================================

    @QueryMapping
    public List<WishlistItemResponse> wishlist(@Argument UUID userId) {
        return wishlistService.getWishlistByUserId(userId);
    }

    @QueryMapping
    public long wishlistCount(@Argument UUID userId) {
        return wishlistService.getWishlistCount(userId);
    }

    @QueryMapping
    public boolean isInWishlist(@Argument UUID userId, @Argument UUID productId) {
        return wishlistService.isInWishlist(userId, productId);
    }

    // ========================================================================
    // WISHLIST MUTATIONS
    // ========================================================================

    @MutationMapping
    public WishlistItemResponse addToWishlist(@Argument Map<String, Object> input) {
        AddToWishlistRequest request = AddToWishlistRequest.builder()
                .userId(UUID.fromString((String) input.get("userId")))
                .productId(UUID.fromString((String) input.get("productId")))
                .build();
        return wishlistService.addToWishlist(request);
    }

    @MutationMapping
    public boolean removeFromWishlist(@Argument UUID id) {
        wishlistService.removeFromWishlist(id);
        return true;
    }

    @MutationMapping
    public boolean removeProductFromWishlist(@Argument UUID userId, @Argument UUID productId) {
        wishlistService.removeFromWishlist(userId, productId);
        return true;
    }

    @MutationMapping
    public boolean clearWishlist(@Argument UUID userId) {
        wishlistService.clearWishlist(userId);
        return true;
    }

    @MutationMapping
    public boolean moveToCart(@Argument UUID userId, @Argument UUID productId) {
        wishlistService.moveToCart(userId, productId);
        return true;
    }
}

