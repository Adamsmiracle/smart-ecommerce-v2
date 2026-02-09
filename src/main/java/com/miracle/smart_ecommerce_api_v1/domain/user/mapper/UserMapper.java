package com.miracle.smart_ecommerce_api_v1.domain.user.mapper;

import com.miracle.smart_ecommerce_api_v1.common.util.JdbcUtils;
import com.miracle.smart_ecommerce_api_v1.domain.user.entity.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * RowMapper for User domain model.
 * Maps ResultSet rows to User objects.
 */
@Component
public class UserMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(JdbcUtils.getUUID(rs, "id"))
                .emailAddress(rs.getString("email_address"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .phoneNumber(rs.getString("phone_number"))
                .passwordHash(rs.getString("password_hash"))
                .isActive(JdbcUtils.getBoolean(rs, "is_active"))
                // FIX: Convert LocalDateTime to OffsetDateTime by adding timezone
                .createdAt(JdbcUtils.getLocalDateTime(rs, "created_at").toLocalDateTime().atOffset(ZoneOffset.UTC))
                .updatedAt(JdbcUtils.getLocalDateTime(rs, "updated_at").toLocalDateTime().atOffset(ZoneOffset.UTC))
                .build();
    }
}