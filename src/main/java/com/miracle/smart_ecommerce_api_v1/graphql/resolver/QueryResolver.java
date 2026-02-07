package com.miracle.smart_ecommerce_api_v1.graphql.resolver;

import com.miracle.smart_ecommerce_api_v1.common.response.PageResponse;
import com.miracle.smart_ecommerce_api_v1.dto.response.*;
import com.miracle.smart_ecommerce_api_v1.service.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

/**
 * GraphQL Query Resolver.
 * Handles all GraphQL queries defined in schema.graphqls.
 */
@Controller
public class QueryResolver {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final WishlistService wishlistService;
    private final AddressService addressService;

    public QueryResolver(UserService userService,
                         ProductService productService,
                         CategoryService categoryService,
                         CartService cartService,
                         OrderService orderService,
                         ReviewService reviewService,
                         WishlistService wishlistService,
                         AddressService addressService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.reviewService = reviewService;
        this.wishlistService = wishlistService;
        this.addressService = addressService;
    }

    // ========================================================================
    // USER QUERIES
    // ========================================================================

    @QueryMapping
    public UserResponse user(@Argument UUID id) {
        return userService.getUserById(id);
    }

    @QueryMapping
    public UserResponse userByEmail(@Argument String email) {
        return userService.getUserByEmail(email);
    }

    @QueryMapping
    public PageResponse<UserResponse> users(@Argument int page, @Argument int size) {
        return userService.getAllUsers(page, size);
    }

    @QueryMapping
    public PageResponse<UserResponse> searchUsers(@Argument String keyword,
                                                   @Argument int page,
                                                   @Argument int size) {
        return userService.searchUsers(keyword, page, size);
    }

    // ========================================================================
    // PRODUCT QUERIES
    // ========================================================================

    @QueryMapping
    public ProductResponse product(@Argument UUID id) {
        return productService.getProductById(id);
    }

    @QueryMapping
    public ProductResponse productBySku(@Argument String sku) {
        return productService.getProductBySku(sku);
    }

    @QueryMapping
    public PageResponse<ProductResponse> products(@Argument int page, @Argument int size) {
        return productService.getAllProducts(page, size);
    }

    @QueryMapping
    public PageResponse<ProductResponse> activeProducts(@Argument int page, @Argument int size) {
        return productService.getActiveProducts(page, size);
    }

    @QueryMapping
    public PageResponse<ProductResponse> productsByCategory(@Argument UUID categoryId,
                                                            @Argument int page,
                                                            @Argument int size) {
        return productService.getProductsByCategory(categoryId, page, size);
    }

    @QueryMapping
    public PageResponse<ProductResponse> searchProducts(@Argument String keyword,
                                                        @Argument int page,
                                                        @Argument int size) {
        return productService.searchProducts(keyword, page, size);
    }

    @QueryMapping
    public PageResponse<ProductResponse> productsInStock(@Argument int page, @Argument int size) {
        return productService.getProductsInStock(page, size);
    }

    // ========================================================================
    // CATEGORY QUERIES
    // ========================================================================

    @QueryMapping
    public CategoryResponse category(@Argument UUID id) {
        return categoryService.getCategoryById(id);
    }

    @QueryMapping
    public List<CategoryResponse> categories() {
        return categoryService.getAllCategories();
    }

    @QueryMapping
    public List<CategoryResponse> rootCategories() {
        return categoryService.getRootCategories();
    }

    @QueryMapping
    public List<CategoryResponse> categoryTree() {
        return categoryService.getCategoryTree();
    }

    @QueryMapping
    public List<CategoryResponse> subcategories(@Argument UUID parentId) {
        return categoryService.getSubcategories(parentId);
    }

    // ========================================================================
    // CART QUERIES
    // ========================================================================

    @QueryMapping
    public CartResponse cart(@Argument UUID userId) {
        return cartService.getCartByUserId(userId);
    }

    @QueryMapping
    public int cartItemCount(@Argument UUID userId) {
        return cartService.getCartItemCount(userId);
    }

    // ========================================================================
    // ORDER QUERIES
    // ========================================================================

    @QueryMapping
    public OrderResponse order(@Argument UUID id) {
        return orderService.getOrderById(id);
    }

    @QueryMapping
    public OrderResponse orderByNumber(@Argument String orderNumber) {
        return orderService.getOrderByOrderNumber(orderNumber);
    }

    @QueryMapping
    public PageResponse<OrderResponse> orders(@Argument int page, @Argument int size) {
        return orderService.getAllOrders(page, size);
    }

    @QueryMapping
    public PageResponse<OrderResponse> ordersByUser(@Argument UUID userId,
                                                    @Argument int page,
                                                    @Argument int size) {
        return orderService.getOrdersByUserId(userId, page, size);
    }

    @QueryMapping
    public PageResponse<OrderResponse> ordersByStatus(@Argument String status,
                                                      @Argument int page,
                                                      @Argument int size) {
        return orderService.getOrdersByStatus(status, page, size);
    }

    // ========================================================================
    // REVIEW QUERIES
    // ========================================================================

    @QueryMapping
    public ReviewResponse review(@Argument UUID id) {
        return reviewService.getReviewById(id);
    }

    @QueryMapping
    public PageResponse<ReviewResponse> reviewsByProduct(@Argument UUID productId,
                                                         @Argument int page,
                                                         @Argument int size) {
        return reviewService.getReviewsByProductId(productId, page, size);
    }

    @QueryMapping
    public PageResponse<ReviewResponse> reviewsByUser(@Argument UUID userId,
                                                      @Argument int page,
                                                      @Argument int size) {
        return reviewService.getReviewsByUserId(userId, page, size);
    }

    @QueryMapping
    public Double productAverageRating(@Argument UUID productId) {
        return reviewService.getAverageRatingForProduct(productId);
    }

    @QueryMapping
    public boolean hasUserReviewedProduct(@Argument UUID userId, @Argument UUID productId) {
        return reviewService.hasUserReviewedProduct(userId, productId);
    }

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
    // ADDRESS QUERIES
    // ========================================================================

    @QueryMapping
    public AddressResponse address(@Argument UUID id) {
        return addressService.getAddressById(id);
    }

    @QueryMapping
    public List<AddressResponse> addressesByUser(@Argument UUID userId) {
        return addressService.getAddressesByUserId(userId);
    }

    @QueryMapping
    public List<AddressResponse> shippingAddresses(@Argument UUID userId) {
        return addressService.getAddressesByUserIdAndType(userId, "shipping");
    }

    @QueryMapping
    public List<AddressResponse> billingAddresses(@Argument UUID userId) {
        return addressService.getAddressesByUserIdAndType(userId, "billing");
    }

    @QueryMapping
    public AddressResponse defaultAddress(@Argument UUID userId) {
        return addressService.getDefaultAddress(userId);
    }
}
