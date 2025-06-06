package com.estuate.mpreplica.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequestDto {

    @NotNull(message = "Seller product assignment ID cannot be null")
    private Long sellerProductAssignmentId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}



