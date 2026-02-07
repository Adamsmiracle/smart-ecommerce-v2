package com.miracle.smart_ecommerce_api_v1.graphql.resolver;

import com.miracle.smart_ecommerce_api_v1.dto.response.ProductResponse;
import com.miracle.smart_ecommerce_api_v1.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.miracle.smart_ecommerce_api_v1.service.ProductService;


import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ProductQueryResolver {
    private final ProductService productService;


    @QueryMapping
    public ProductResponse Product(@Argument UUID id) {
        return productService.getProductById(id);
    }
}

