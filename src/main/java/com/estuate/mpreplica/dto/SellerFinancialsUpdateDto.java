package com.estuate.mpreplica.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SellerFinancialsUpdateDto {
    @DecimalMin(value = "0.0", inclusive = true, message = "Commission rate must be non-negative.")
    @DecimalMax(value = "1.0", inclusive = true, message = "Commission rate cannot exceed 1.0 (100%).")
    @Digits(integer=1, fraction=4, message = "Commission rate format is invalid (e.g., 0.10 for 10%).")
    private BigDecimal commissionRate;

    @Email(message = "PayPal email should be valid.")
    @Size(max = 255)
    private String payPalEmail;
}

