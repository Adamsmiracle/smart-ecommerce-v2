package com.miracle.smart_ecommerce_api_v1.domain.order.mapper;

import com.miracle.smart_ecommerce_api_v1.common.util.JdbcUtils;
import com.miracle.smart_ecommerce_api_v1.domain.order.entity.PaymentMethod;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;

/**
 * RowMapper for PaymentMethod domain model.
 */
@Component
public class PaymentMethodMapper implements RowMapper<PaymentMethod> {

    @Override
    public PaymentMethod mapRow(ResultSet rs, int rowNum) throws SQLException {
        return PaymentMethod.builder()
                .id(JdbcUtils.getUUID(rs, "id"))
                .userId(JdbcUtils.getUUID(rs, "user_id"))
                .paymentType(rs.getString("payment_type"))
                .provider(rs.getString("provider"))
                .accountNumber(rs.getString("account_number"))
                .expiryDate(JdbcUtils.getLocalDate(rs, "expiry_date"))
                .isDefault(JdbcUtils.getBoolean(rs, "is_default"))
                // FIXED: Convert LocalDateTime to OffsetDateTime properly
                .createdAt(JdbcUtils.getLocalDateTime(rs, "created_at").toLocalDateTime().atOffset(ZoneOffset.UTC))
                .build();
    }
}