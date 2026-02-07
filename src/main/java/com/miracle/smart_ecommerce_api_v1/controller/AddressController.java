package com.miracle.smart_ecommerce_api_v1.controller;

import com.miracle.smart_ecommerce_api_v1.common.response.ApiResponse;
import com.miracle.smart_ecommerce_api_v1.dto.request.CreateAddressRequest;
import com.miracle.smart_ecommerce_api_v1.dto.response.AddressResponse;
import com.miracle.smart_ecommerce_api_v1.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Address management (shipping and billing addresses).
 */
@RestController
@RequestMapping("/api/addresses")
@Tag(name = "Addresses", description = "Address management APIs")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    @Operation(summary = "Create a new address", description = "Creates a new shipping or billing address")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Address created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(@Valid @RequestBody CreateAddressRequest request) {
        AddressResponse address = addressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(address, "Address created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID", description = "Retrieves an address by its unique ID")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @Parameter(description = "Address ID") @PathVariable UUID id) {
        AddressResponse address = addressService.getAddressById(id);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user addresses", description = "Retrieves all addresses for a specific user")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddressesByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<AddressResponse> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/user/{userId}/shipping")
    @Operation(summary = "Get user shipping addresses", description = "Retrieves all shipping addresses for a user")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getShippingAddresses(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<AddressResponse> addresses = addressService.getAddressesByUserIdAndType(userId, "shipping");
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/user/{userId}/billing")
    @Operation(summary = "Get user billing addresses", description = "Retrieves all billing addresses for a user")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getBillingAddresses(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<AddressResponse> addresses = addressService.getAddressesByUserIdAndType(userId, "billing");
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/user/{userId}/default")
    @Operation(summary = "Get user default address", description = "Retrieves the default address for a user")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefaultAddress(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        AddressResponse address = addressService.getDefaultAddress(userId);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address", description = "Updates an existing address")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @Parameter(description = "Address ID") @PathVariable UUID id,
            @Valid @RequestBody CreateAddressRequest request) {
        AddressResponse address = addressService.updateAddress(id, request);
        return ResponseEntity.ok(ApiResponse.success(address, "Address updated successfully"));
    }

    @PatchMapping("/{id}/set-default")
    @Operation(summary = "Set as default address", description = "Sets an address as the default for its user")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @Parameter(description = "Address ID") @PathVariable UUID id) {
        AddressResponse address = addressService.setDefaultAddress(id);
        return ResponseEntity.ok(ApiResponse.success(address, "Default address updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address", description = "Deletes an address by ID")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @Parameter(description = "Address ID") @PathVariable UUID id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully"));
    }
}

