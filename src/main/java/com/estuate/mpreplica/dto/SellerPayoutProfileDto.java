package com.estuate.mpreplica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data Transfer Object for handling a seller's payout profile information.
 * Used by sellers to submit or update their payment details (e.g., PayPal email).
 */
@Data
public class SellerPayoutProfileDto {

    private Long id;

    private Long sellerProfileId;

    @NotBlank(message = "PSP identifier (e.g., PayPal email) cannot be blank.")
    @Email(message = "A valid email address is required for the PSP identifier.")
    private String pspIdentifier;

    private boolean isEnabled;
}
