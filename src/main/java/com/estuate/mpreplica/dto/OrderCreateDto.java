package com.estuate.mpreplica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;
@Data
public class OrderCreateDto {

    @NotBlank(message = "Customer name cannot be blank")
    @Size(max = 255, message = "Customer name cannot exceed 255 characters")
    private String customerName;

    @NotBlank(message = "Customer email cannot be blank")
    @Email(message = "Customer email should be valid")
    @Size(max = 255, message = "Customer email cannot exceed 255 characters")
    private String customerEmail;

    @Size(max = 50, message = "Customer phone cannot exceed 50 characters")
    private String customerPhone; // Optional

    @NotBlank(message = "Shipping address cannot be blank")
    @Size(max = 1000, message = "Shipping address cannot exceed 1000 characters")
    private String shippingAddress;

    @Size(max = 1000, message = "Billing address cannot exceed 1000 characters")
    private String billingAddress; // Optional, could default to shipping address

    @NotEmpty(message = "Order must contain at least one item")
    @Valid // This will trigger validation for each OrderItemRequestDto in the list
    private List<OrderItemRequestDto> items;

    @Size(max = 255, message = "Payment ID cannot exceed 255 characters")
    private String paymentId; // Optional, might be set after order creation

    @Size(max = 100, message = "Payment status cannot exceed 100 characters")
    private String paymentStatus; // Optional

    @Size(max = 255, message = "Payment method details cannot exceed 255 characters")
    private String paymentMethodDetails; // Optional

    @Size(max = 3, message = "Currency code should be 3 characters (e.g., USD)")
    private String currency; // Optional, might default in the system
}