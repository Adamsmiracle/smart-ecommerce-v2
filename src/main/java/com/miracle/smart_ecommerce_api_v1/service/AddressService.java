package com.miracle.smart_ecommerce_api_v1.service;

import com.miracle.smart_ecommerce_api_v1.dto.request.CreateAddressRequest;
import com.miracle.smart_ecommerce_api_v1.dto.response.AddressResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Address operations.
 */
public interface AddressService {

    /**
     * Create a new address
     */
    AddressResponse createAddress(CreateAddressRequest request);

    /**
     * Get address by ID
     */
    AddressResponse getAddressById(UUID id);

    /**
     * Get all addresses for a user
     */
    List<AddressResponse> getAddressesByUserId(UUID userId);

    /**
     * Get addresses by user ID and type (shipping/billing)
     */
    List<AddressResponse> getAddressesByUserIdAndType(UUID userId, String addressType);

    /**
     * Get user's default address
     */
    AddressResponse getDefaultAddress(UUID userId);

    /**
     * Update an address
     */
    AddressResponse updateAddress(UUID id, CreateAddressRequest request);

    /**
     * Set an address as default
     */
    AddressResponse setDefaultAddress(UUID id);

    /**
     * Delete an address
     */
    void deleteAddress(UUID id);
}

