package com.miracle.smart_ecommerce_api_v1.domain.order.mapper;

import com.miracle.smart_ecommerce_api_v1.common.util.JdbcUtils;
import com.miracle.smart_ecommerce_api_v1.domain.order.entity.OrderItem;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowMapper for OrderItem domain model.
 * Maps ResultSet rows to OrderItem objects.
 */
@Component
public class OrderItemMapper implements RowMapper<OrderItem> {

    @Override
    public OrderItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        return OrderItem.builder()
                .id(JdbcUtils.getUUID(rs, "id"))
                .orderId(JdbcUtils.getUUID(rs, "order_id"))
                .productId(JdbcUtils.getUUID(rs, "product_id"))
                .productName(rs.getString("product_name"))
                .productSku(rs.getString("product_sku"))
                .unitPrice(rs.getBigDecimal("unit_price"))
                .quantity(JdbcUtils.getInteger(rs, "quantity"))
                .totalPrice(rs.getBigDecimal("total_price"))
                .build();
    }
}

