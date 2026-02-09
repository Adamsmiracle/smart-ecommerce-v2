package com.miracle.smart_ecommerce_api_v1.domain.order.mapper;

import com.miracle.smart_ecommerce_api_v1.common.util.JdbcUtils;
import com.miracle.smart_ecommerce_api_v1.domain.order.entity.ShippingMethod;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowMapper for ShippingMethod domain model.
 */
@Component
public class ShippingMethodMapper implements RowMapper<ShippingMethod> {

    @Override
    public ShippingMethod mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ShippingMethod.builder()
                .id(JdbcUtils.getUUID(rs, "id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .price(rs.getBigDecimal("price"))
                .estimatedDays(JdbcUtils.getInteger(rs, "estimated_days"))
                .isActive(JdbcUtils.getBoolean(rs, "is_active"))
                .build();
    }
}

