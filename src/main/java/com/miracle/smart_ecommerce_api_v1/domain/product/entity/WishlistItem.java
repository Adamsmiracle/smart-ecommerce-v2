package com.miracle.smart_ecommerce_api_v1.domain.product.entity;

import com.miracle.smart_ecommerce_api_v1.domain.BaseModel;
import com.miracle.smart_ecommerce_api_v1.domain.user.entity.User;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Wishlist Item domain model (POJO) - represents wishlist_item table.
 * No JPA annotations - used with raw JDBC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class WishlistItem extends BaseModel {

    private UUID userId;
    private UUID productId;

    // Transient fields for relationships (populated when needed)
    private transient User user;
    private transient Product product;
}

