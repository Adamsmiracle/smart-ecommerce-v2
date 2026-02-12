package com.miracle.smart_ecommerce_api_v1.domain.order.service;

import com.miracle.smart_ecommerce_api_v1.common.response.PageResponse;
import com.miracle.smart_ecommerce_api_v1.domain.order.dto.PaymentMethodRequest;
import com.miracle.smart_ecommerce_api_v1.domain.order.dto.PaymentMethodResponse;

import java.util.UUID;

public interface PaymentMethodService {
    PaymentMethodResponse create(PaymentMethodRequest request);
    PaymentMethodResponse update(UUID id, PaymentMethodRequest request);
    PaymentMethodResponse getById(UUID id);
    PageResponse<PaymentMethodResponse> getByUserId(UUID userId, int page, int size);
    void delete(UUID id);
}

