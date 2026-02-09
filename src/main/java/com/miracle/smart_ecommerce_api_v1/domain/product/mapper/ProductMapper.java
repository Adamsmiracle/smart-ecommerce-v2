package com.miracle.smart_ecommerce_api_v1.domain.product.mapper;

import com.miracle.smart_ecommerce_api_v1.common.util.JdbcUtils;
import com.miracle.smart_ecommerce_api_v1.domain.product.entity.Product;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;

/**
 * RowMapper for Product domain model.
 * Maps ResultSet rows to Product objects.
 */
@Component
public class ProductMapper implements RowMapper<Product> {

    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Product.builder()
                .id(JdbcUtils.getUUID(rs, "id"))
                .categoryId(JdbcUtils.getUUID(rs, "category_id"))
                .sku(rs.getString("sku"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .price(rs.getBigDecimal("price"))
                .stockQuantity(JdbcUtils.getInteger(rs, "stock_quantity"))
                .isActive(JdbcUtils.getBoolean(rs, "is_active"))
                .images(JdbcUtils.getStringListFromJsonb(rs, "images"))
                // FIXED: Convert LocalDateTime to OffsetDateTime properly
                .createdAt(JdbcUtils.getLocalDateTime(rs, "created_at").toLocalDateTime().atOffset(ZoneOffset.UTC))
                .updatedAt(JdbcUtils.getLocalDateTime(rs, "updated_at").toLocalDateTime().atOffset(ZoneOffset.UTC))
                .build();
    }
}