package com.miracle.smart_ecommerce_api_v1.controller;

import com.miracle.smart_ecommerce_api_v1.common.response.ApiResponse;
import com.miracle.smart_ecommerce_api_v1.common.response.PageResponse;
import com.miracle.smart_ecommerce_api_v1.dto.request.AddToWishlistRequest;
import com.miracle.smart_ecommerce_api_v1.dto.response.WishlistItemResponse;
import com.miracle.smart_ecommerce_api_v1.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Wishlist management.
 */
@RestController
@RequestMapping("/api/wishlist")
@Tag(name = "Wishlist", description = "Wishlist management APIs")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping
    @Operation(summary = "Add to wishlist", description = "Adds a product to user's wishlist")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Product added to wishlist"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Product already in wishlist")
    })
    public ResponseEntity<ApiResponse<WishlistItemResponse>> addToWishlist(
            @Valid @RequestBody AddToWishlistRequest request) {
        WishlistItemResponse item = wishlistService.addToWishlist(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(item, "Product added to wishlist"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's wishlist", description = "Retrieves all items in user's wishlist")
    public ResponseEntity<ApiResponse<List<WishlistItemResponse>>> getWishlist(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<WishlistItemResponse> items = wishlistService.getWishlistByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/user/{userId}/page")
    @Operation(summary = "Get user's wishlist (paginated)", description = "Retrieves user's wishlist with pagination")
    public ResponseEntity<ApiResponse<PageResponse<WishlistItemResponse>>> getWishlistPaginated(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        PageResponse<WishlistItemResponse> items = wishlistService.getWishlistByUserId(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Get wishlist count", description = "Returns the number of items in user's wishlist")
    public ResponseEntity<ApiResponse<Long>> getWishlistCount(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        long count = wishlistService.getWishlistCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/check")
    @Operation(summary = "Check if in wishlist", description = "Checks if a product is in user's wishlist")
    public ResponseEntity<ApiResponse<Boolean>> isInWishlist(
            @Parameter(description = "User ID") @RequestParam UUID userId,
            @Parameter(description = "Product ID") @RequestParam UUID productId) {
        boolean inWishlist = wishlistService.isInWishlist(userId, productId);
        return ResponseEntity.ok(ApiResponse.success(inWishlist));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove from wishlist by ID", description = "Removes an item from wishlist by item ID")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlistById(
            @Parameter(description = "Wishlist item ID") @PathVariable UUID id) {
        wishlistService.removeFromWishlist(id);
        return ResponseEntity.ok(ApiResponse.success("Item removed from wishlist"));
    }

    @DeleteMapping("/user/{userId}/product/{productId}")
    @Operation(summary = "Remove product from wishlist", description = "Removes a specific product from user's wishlist")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist"));
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Clear wishlist", description = "Removes all items from user's wishlist")
    public ResponseEntity<ApiResponse<Void>> clearWishlist(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        wishlistService.clearWishlist(userId);
        return ResponseEntity.ok(ApiResponse.success("Wishlist cleared successfully"));
    }

    @PostMapping("/user/{userId}/product/{productId}/move-to-cart")
    @Operation(summary = "Move to cart", description = "Moves a product from wishlist to shopping cart")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product moved to cart"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wishlist item not found")
    })
    public ResponseEntity<ApiResponse<Void>> moveToCart(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        wishlistService.moveToCart(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Product moved to cart"));
    }
}

