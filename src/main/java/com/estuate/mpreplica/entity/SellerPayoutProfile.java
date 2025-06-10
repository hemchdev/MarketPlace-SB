package com.estuate.mpreplica.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores payment-specific information for a seller, decoupled from their main profile.
 * This focuses on PayPal payouts but is designed for future extension to other PSPs.
 */
@Entity
@Table(name = "seller_payout_profiles")
@Getter
@Setter
@NoArgsConstructor
public class SellerPayoutProfile extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_profile_id", nullable = false, unique = true)
    private SellerProfile sellerProfile;

    /**
     * The seller's identifier for the PSP. For PayPal, this is their PayPal email address.
     */
    @NotBlank
    @Email
    @Column(name = "psp_identifier", nullable = false)
    private String pspIdentifier;

    /**
     * The Payment Service Provider this profile is for (e.g., "PAYPAL").
     */
    @NotBlank
    @Column(name = "psp_provider", nullable = false, length = 50)
    private String pspProvider = "PAYPAL";

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    public SellerPayoutProfile(SellerProfile sellerProfile, String pspIdentifier) {
        this.sellerProfile = sellerProfile;
        this.pspIdentifier = pspIdentifier;
    }
}
