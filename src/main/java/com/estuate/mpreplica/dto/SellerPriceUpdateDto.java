package com.estuate.mpreplica.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SellerPriceUpdateDto {

    @NotNull(message = "Seller price cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Seller price must be zero or greater")
    @Digits(integer = 15, fraction = 4, message = "Seller price can have up to 15 integer and 4 fractional digits")
    private BigDecimal sellerPrice;
}

