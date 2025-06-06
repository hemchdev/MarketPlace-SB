package com.estuate.mpreplica.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.*;

@Data
public class ProductCreateDto {

    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;

    private String description; // Optional

    @NotBlank(message = "SKU cannot be blank")
    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    private String sku;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category; // Optional

    private List<@NotBlank(message = "Image URL cannot be blank") @Size(max = 1024, message = "Image URL cannot exceed 1024 characters") String> imageUrls; // Optional

    @NotNull(message = "Base price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Base price can have up to 15 integer and 4 fractional digits")
    private BigDecimal basePrice;

    private Map<@Size(max = 100, message = "Attribute key cannot exceed 100 characters") String,
            @Size(max = 255, message = "Attribute value cannot exceed 255 characters") String> attributes; // Optional
}

