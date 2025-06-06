package com.estuate.mpreplica.enums;

public enum OrderItemStatus {
    PENDING_SELLER_CONFIRMATION, // Initial: Seller needs to confirm they can fulfill this item
    CONFIRMED_BY_SELLER,         // Seller confirmed, item is awaiting shipment (assuming payment is good)
    AWAITING_SHIPMENT,           // Confirmed and paid, seller to ship
    SHIPPED,
    DELIVERED_TO_CUSTOMER,
    CANCELLED_BY_SELLER,         // Seller cancelled this specific item
    CANCELLED_BY_OPERATOR,       // Operator cancelled this specific item
    RETURN_REQUESTED_BY_CUSTOMER,
    RETURN_APPROVED_BY_SELLER,
    RETURNED_RECEIVED_BY_SELLER
}

