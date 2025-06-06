package com.estuate.mpreplica.dto;


import com.estuate.mpreplica.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OperatorOrderStatusUpdateDto {
    @NotNull(message = "Order status is required")
    private OrderStatus status;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
}

