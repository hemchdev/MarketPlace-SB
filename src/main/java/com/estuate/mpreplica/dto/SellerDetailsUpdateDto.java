package com.estuate.mpreplica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for an operator to update a seller's core details.
 * This version now correctly includes both rating and payPalEmail.
 */
@Data
public class SellerDetailsUpdateDto {

    @Min(value = 0, message = "Rating must be at least 0")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    /**
     * RESTORED: Allows operator to update the seller's payment email.
     */
    @Email(message = "PayPal email should be valid.")
    @Size(max = 255)
    private String payPalEmail;
}

