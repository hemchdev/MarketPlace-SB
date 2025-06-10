package com.estuate.mpreplica.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;


@Data
public class RefundRequestDto {

    @NotNull(message = "Seller Profile ID is required.")
    private Long sellerProfileId;

    @NotNull(message = "Order ID is required.")
    private Long orderId;

    @NotNull(message = "Refund amount is required.")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than zero.")
    private BigDecimal amount;

    @NotBlank(message = "A reason for the refund is required.")
    private String reason;
}
