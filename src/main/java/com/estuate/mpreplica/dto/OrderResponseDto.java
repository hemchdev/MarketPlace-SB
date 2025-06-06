package com.estuate.mpreplica.dto;

import com.estuate.mpreplica.enums.OrderStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDto {
    private Long id;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String shippingAddress;
    private String billingAddress;
    private BigDecimal totalAmount;
    private String currency;
    private OrderStatus orderStatus;
    private String paymentId;
    private String paymentStatus;
    private String paymentMethodDetails;
    private Long createdBySellerProfileId;
    // Corrected to match mapper
    private String createdBySellerName;
    private List<OrderItemResponseDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
