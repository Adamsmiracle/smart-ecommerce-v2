package com.miracle.smart_ecommerce_api_v1.domain.review.mapper;

import com.miracle.smart_ecommerce_api_v1.common.util.JdbcUtils;
import com.miracle.smart_ecommerce_api_v1.domain.review.entity.ProductReview;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;

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
                // FIXED: Convert LocalDateTime to OffsetDateTime properly
                .createdAt(JdbcUtils.getLocalDateTime(rs, "created_at").toLocalDateTime().atOffset(ZoneOffset.UTC))
                .updatedAt(JdbcUtils.getLocalDateTime(rs, "updated_at").toLocalDateTime().atOffset(ZoneOffset.UTC))
                .build();
    }
}