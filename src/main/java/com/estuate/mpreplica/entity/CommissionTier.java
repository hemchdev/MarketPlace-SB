package com.estuate.mpreplica.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents a commission tier based on seller performance.
 * Operators can define multiple tiers (e.g., Bronze, Silver, Gold), and the system
 * will automatically apply the best-matching tier's commission rate to a seller
 * based on their rating.
 */
@Entity
@Table(name = "commission_tiers")
@Getter
@Setter
@NoArgsConstructor
public class CommissionTier extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "tier_name", nullable = false, unique = true, length = 100)
    private String tierName;

    @NotNull(message = "Minimum rating required cannot be null.")
    @Column(name = "min_rating_required", nullable = false)
    private Double minRatingRequired;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "1.0", inclusive = true)
    @Digits(integer = 1, fraction = 4)
    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}

