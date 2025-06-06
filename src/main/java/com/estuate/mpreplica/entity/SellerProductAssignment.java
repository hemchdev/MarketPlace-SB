package com.estuate.mpreplica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "seller_product_assignments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "seller_profile_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class SellerProductAssignment extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_profile_id", nullable = false)
    private SellerProfile sellerProfile;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity = 0;

    @Column(name = "is_sellable_by_seller", nullable = false)
    private boolean isSellableBySeller = true;

    @Column(name = "assigned_by_operator_id")
    private Long assignedByOperatorId;

    // This initialization is critical to prevent the NullPointerException
    @Version
    private Long version = 0L;
}
