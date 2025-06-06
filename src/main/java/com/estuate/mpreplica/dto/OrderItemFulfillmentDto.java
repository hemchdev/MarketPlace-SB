package com.estuate.mpreplica.dto;

import com.estuate.mpreplica.enums.OrderItemStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderItemFulfillmentDto {
    @NotNull(message = "Item status is required.")
    private OrderItemStatus status;

    @Size(max = 255, message = "Tracking number cannot exceed 255 characters")
    private String trackingNumber;

    @Size(max = 100, message = "Shipping carrier cannot exceed 100 characters")
    private String shippingCarrier;

    private LocalDateTime estimatedDeliveryDate;

    // New field based on user's code in OrderService
    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    private String cancellationReason;
}
