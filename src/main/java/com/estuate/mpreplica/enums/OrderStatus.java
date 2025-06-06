package com.estuate.mpreplica.enums;

public enum OrderStatus {
    PENDING_CONFIRMATION,         // Order received, system checks (e.g. seller active, product exists)
    AWAITING_PAYMENT,             // Order confirmed, payment pending
    PAYMENT_FAILED,
    PAYMENT_RECEIVED,             // Payment successful, items ready for seller processing
    PROCESSING_BY_SELLERS,        // At least one seller has confirmed their items
    PARTIALLY_SHIPPED,
    SHIPPED,                      // All fulfillable items shipped
    PARTIALLY_DELIVERED,
    DELIVERED,                    // All shipped items delivered
    COMPLETED,                    // Order fulfilled, return window closed etc.
    CANCELLED_BY_CUSTOMER,
    CANCELLED_BY_SELLER,
    CANCELLED_BY_OPERATOR,
    REFUND_PENDING,
    REFUNDED,
    DISPUTED
}
