package com.miracle.smart_ecommerce_api_v1.domain.order.mapper;

import com.miracle.smart_ecommerce_api_v1.common.util.JdbcUtils;
import com.miracle.smart_ecommerce_api_v1.domain.order.entity.CustomerOrder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * RowMapper for CustomerOrder domain model.
 */
@Component
public class CustomerOrderMapper implements RowMapper<CustomerOrder> {

    @Override
    public CustomerOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
        return CustomerOrder.builder()
                .id(JdbcUtils.getUUID(rs, "id"))
                .userId(JdbcUtils.getUUID(rs, "user_id"))
                .orderNumber(rs.getString("order_number"))
                .status(rs.getString("status"))
                .paymentMethodId(JdbcUtils.getUUID(rs, "payment_method_id"))
                .paymentStatus(rs.getString("payment_status"))
                .shippingAddressId(JdbcUtils.getUUID(rs, "shipping_address_id"))
                .shippingMethodId(JdbcUtils.getUUID(rs, "shipping_method_id"))
                .subtotal(rs.getBigDecimal("subtotal"))
                .shippingCost(rs.getBigDecimal("shipping_cost"))
                .total(rs.getBigDecimal("total"))
                .customerNotes(rs.getString("customer_notes"))
                // FIXED: Convert LocalDateTime to OffsetDateTime properly
                .createdAt(JdbcUtils.getLocalDateTime(rs, "created_at").toLocalDateTime().atOffset(ZoneOffset.UTC))
                // FIXED: Handle nullable cancelledAt field
                .cancelledAt(JdbcUtils.getLocalDateTime(rs, "cancelled_at") != null
                        ? JdbcUtils.getLocalDateTime(rs, "cancelled_at").toLocalDateTime().atOffset(ZoneOffset.UTC)
                        : null)
                .build();
    }
}