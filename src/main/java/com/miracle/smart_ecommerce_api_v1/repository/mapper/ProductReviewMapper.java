package com.miracle.smart_ecommerce_api_v1.repository.mapper;

import com.miracle.smart_ecommerce_api_v1.common.util.JdbcUtils;
import com.miracle.smart_ecommerce_api_v1.domain.ProductReview;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowMapper for ProductReview domain model.
 * Maps ResultSet rows to ProductReview objects.
 */
@Component
public class ProductReviewMapper implements RowMapper<ProductReview> {

    @Override
    public ProductReview mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ProductReview.builder()
                .id(JdbcUtils.getUUID(rs, "id"))
                .userId(JdbcUtils.getUUID(rs, "user_id"))
                .productId(JdbcUtils.getUUID(rs, "product_id"))
                .orderItemId(JdbcUtils.getUUID(rs, "order_item_id"))
                .rating(JdbcUtils.getInteger(rs, "rating"))
                .title(rs.getString("title"))
                .comment(rs.getString("comment"))
                .isVerifiedPurchase(JdbcUtils.getBoolean(rs, "is_verified_purchase"))
                .isApproved(JdbcUtils.getBoolean(rs, "is_approved"))
                .createdAt(JdbcUtils.getLocalDateTime(rs, "created_at"))
                .updatedAt(JdbcUtils.getLocalDateTime(rs, "updated_at"))
                .build();
    }
}

