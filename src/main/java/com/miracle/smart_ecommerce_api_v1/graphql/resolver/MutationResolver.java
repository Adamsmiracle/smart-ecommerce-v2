package com.miracle.smart_ecommerce_api_v1.graphql.resolver;

import com.miracle.smart_ecommerce_api_v1.dto.request.*;
import com.miracle.smart_ecommerce_api_v1.dto.response.*;
import com.miracle.smart_ecommerce_api_v1.service.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GraphQL Mutation Resolver.
 * Handles all GraphQL mutations defined in schema.graphqls.
 */
@Controller
public class MutationResolver {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final ReviewService reviewService;
    private final WishlistService wishlistService;
    private final AddressService addressService;

    public MutationResolver(UserService userService,
                            ProductService productService,
                            CategoryService categoryService,
                            CartService cartService,
                            ReviewService reviewService,
                            WishlistService wishlistService,
                            AddressService addressService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.reviewService = reviewService;
        this.wishlistService = wishlistService;
        this.addressService = addressService;
    }

    // ========================================================================
    // USER MUTATIONS
    // ========================================================================

    @MutationMapping
    public UserResponse createUser(@Argument Map<String, Object> input) {
        CreateUserRequest request = CreateUserRequest.builder()
                .emailAddress((String) input.get("emailAddress"))
                .firstName((String) input.get("firstName"))
                .lastName((String) input.get("lastName"))
                .phoneNumber((String) input.get("phoneNumber"))
                .password((String) input.get("password"))
                .build();
        return userService.createUser(request);
    }

    @MutationMapping
    public UserResponse updateUser(@Argument UUID id, @Argument Map<String, Object> input) {
        CreateUserRequest request = CreateUserRequest.builder()
                .emailAddress((String) input.get("emailAddress"))
                .firstName((String) input.get("firstName"))
                .lastName((String) input.get("lastName"))
                .phoneNumber((String) input.get("phoneNumber"))
                .password((String) input.get("password"))
                .build();
        return userService.updateUser(id, request);
    }

    @MutationMapping
    public boolean deleteUser(@Argument UUID id) {
        userService.deleteUser(id);
        return true;
    }

    @MutationMapping
    public boolean activateUser(@Argument UUID id) {
        userService.activateUser(id);
        return true;
    }

    @MutationMapping
    public boolean deactivateUser(@Argument UUID id) {
        userService.deactivateUser(id);
        return true;
    }

    // ========================================================================
    // PRODUCT MUTATIONS
    // ========================================================================

    @MutationMapping
    public ProductResponse createProduct(@Argument Map<String, Object> input) {
        CreateProductRequest request = mapToProductRequest(input);
        return productService.createProduct(request);
    }

    @MutationMapping
    public ProductResponse updateProduct(@Argument UUID id, @Argument Map<String, Object> input) {
        CreateProductRequest request = mapToProductRequest(input);
        return productService.updateProduct(id, request);
    }

    @MutationMapping
    public boolean deleteProduct(@Argument UUID id) {
        productService.deleteProduct(id);
        return true;
    }

    @MutationMapping
    public boolean activateProduct(@Argument UUID id) {
        productService.activateProduct(id);
        return true;
    }

    @MutationMapping
    public boolean deactivateProduct(@Argument UUID id) {
        productService.deactivateProduct(id);
        return true;
    }

    @MutationMapping
    public boolean updateStock(@Argument UUID id, @Argument int quantity) {
        productService.updateStock(id, quantity);
        return true;
    }

    // ========================================================================
    // CATEGORY MUTATIONS
    // ========================================================================

    @MutationMapping
    public CategoryResponse createCategory(@Argument Map<String, Object> input) {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .parentCategoryId(input.get("parentCategoryId") != null
                        ? UUID.fromString((String) input.get("parentCategoryId")) : null)
                .categoryName((String) input.get("categoryName"))
                .build();
        return categoryService.createCategory(request);
    }

    @MutationMapping
    public CategoryResponse updateCategory(@Argument UUID id, @Argument Map<String, Object> input) {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .parentCategoryId(input.get("parentCategoryId") != null
                        ? UUID.fromString((String) input.get("parentCategoryId")) : null)
                .categoryName((String) input.get("categoryName"))
                .build();
        return categoryService.updateCategory(id, request);
    }

    @MutationMapping
    public boolean deleteCategory(@Argument UUID id) {
        categoryService.deleteCategory(id);
        return true;
    }

    // ========================================================================
    // CART MUTATIONS
    // ========================================================================

    @MutationMapping
    public CartResponse addToCart(@Argument UUID userId, @Argument Map<String, Object> input) {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(UUID.fromString((String) input.get("productId")))
                .quantity((Integer) input.get("quantity"))
                .build();
        return cartService.addItemToCart(userId, request);
    }

    @MutationMapping
    public CartResponse updateCartItemQuantity(@Argument UUID userId,
                                               @Argument UUID itemId,
                                               @Argument int quantity) {
        return cartService.updateItemQuantity(userId, itemId, quantity);
    }

    @MutationMapping
    public CartResponse removeFromCart(@Argument UUID userId, @Argument UUID itemId) {
        return cartService.removeItemFromCart(userId, itemId);
    }

    @MutationMapping
    public boolean clearCart(@Argument UUID userId) {
        cartService.clearCart(userId);
        return true;
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    @SuppressWarnings("unchecked")
    private CreateProductRequest mapToProductRequest(Map<String, Object> input) {
        return CreateProductRequest.builder()
                .categoryId(input.get("categoryId") != null
                        ? UUID.fromString((String) input.get("categoryId")) : null)
                .sku((String) input.get("sku"))
                .name((String) input.get("name"))
                .description((String) input.get("description"))
                .price(input.get("price") != null
                        ? new BigDecimal(input.get("price").toString()) : null)
                .stockQuantity(input.get("stockQuantity") != null
                        ? (Integer) input.get("stockQuantity") : null)
                .isActive(input.get("isActive") != null
                        ? (Boolean) input.get("isActive") : null)
                .images(input.get("images") != null
                        ? (List<String>) input.get("images") : null)
                .build();
    }

    // ========================================================================
    // REVIEW MUTATIONS
    // ========================================================================

    @MutationMapping
    public ReviewResponse createReview(@Argument Map<String, Object> input) {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .productId(UUID.fromString((String) input.get("productId")))
                .userId(UUID.fromString((String) input.get("userId")))
                .rating((Integer) input.get("rating"))
                .title((String) input.get("title"))
                .comment((String) input.get("comment"))
                .build();
        return reviewService.createReview(request);
    }

    @MutationMapping
    public ReviewResponse updateReview(@Argument UUID id, @Argument Map<String, Object> input) {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .productId(UUID.fromString((String) input.get("productId")))
                .userId(UUID.fromString((String) input.get("userId")))
                .rating((Integer) input.get("rating"))
                .title((String) input.get("title"))
                .comment((String) input.get("comment"))
                .build();
        return reviewService.updateReview(id, request);
    }

    @MutationMapping
    public boolean deleteReview(@Argument UUID id) {
        reviewService.deleteReview(id);
        return true;
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

    // ========================================================================
    // ADDRESS MUTATIONS
    // ========================================================================

    @MutationMapping
    public AddressResponse createAddress(@Argument Map<String, Object> input) {
        CreateAddressRequest request = CreateAddressRequest.builder()
                .userId(UUID.fromString((String) input.get("userId")))
                .addressLine((String) input.get("addressLine"))
                .city((String) input.get("city"))
                .region((String) input.get("region"))
                .country((String) input.get("country"))
                .postalCode((String) input.get("postalCode"))
                .isDefault(input.get("isDefault") != null ? (Boolean) input.get("isDefault") : false)
                .addressType((String) input.get("addressType"))
                .build();
        return addressService.createAddress(request);
    }

    @MutationMapping
    public AddressResponse updateAddress(@Argument UUID id, @Argument Map<String, Object> input) {
        CreateAddressRequest request = CreateAddressRequest.builder()
                .userId(UUID.fromString((String) input.get("userId")))
                .addressLine((String) input.get("addressLine"))
                .city((String) input.get("city"))
                .region((String) input.get("region"))
                .country((String) input.get("country"))
                .postalCode((String) input.get("postalCode"))
                .isDefault(input.get("isDefault") != null ? (Boolean) input.get("isDefault") : false)
                .addressType((String) input.get("addressType"))
                .build();
        return addressService.updateAddress(id, request);
    }

    @MutationMapping
    public boolean deleteAddress(@Argument UUID id) {
        addressService.deleteAddress(id);
        return true;
    }

    @MutationMapping
    public AddressResponse setDefaultAddress(@Argument UUID id) {
        return addressService.setDefaultAddress(id);
    }
}
