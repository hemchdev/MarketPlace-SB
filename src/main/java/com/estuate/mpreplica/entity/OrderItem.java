package com.estuate.mpreplica.entity;

import com.estuate.mpreplica.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem extends Auditable { // Assuming Auditable class exists
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_product_assignment_id", nullable = false)
    private SellerProductAssignment sellerProductAssignment;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId; // Should correspond to SellerProfile's ID

    @Column(nullable = false)
    private int quantity;

    @Column(name = "price_at_purchase", precision = 19, scale = 4, nullable = false)
    private BigDecimal priceAtPurchase;

    @Column(name = "subtotal", precision = 19, scale = 4, nullable = false)
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false, length = 50)
    private OrderItemStatus itemStatus = OrderItemStatus.PENDING_SELLER_CONFIRMATION;

    @Column(name = "tracking_number", length = 255)
    private String trackingNumber;

    @Column(name = "shipping_carrier", length = 100)
    private String shippingCarrier;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
}
