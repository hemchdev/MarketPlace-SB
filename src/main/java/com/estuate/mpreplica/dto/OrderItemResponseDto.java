package com.estuate.mpreplica.dto;


import com.estuate.mpreplica.enums.OrderItemStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItemResponseDto {
    private Long id;
    private Long orderId;
    private Long sellerProductAssignmentId;
    private String productName;
    private String productSku;
    private Long sellerId;
    // Corrected to match mapper
    private String sellerName;
    private int quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal subtotal;
    private OrderItemStatus itemStatus;
    private String trackingNumber;
    private String shippingCarrier;
    private LocalDateTime estimatedDeliveryDate;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


