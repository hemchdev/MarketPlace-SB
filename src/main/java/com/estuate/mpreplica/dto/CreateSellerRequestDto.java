package com.estuate.mpreplica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for an operator to create a new seller user and their associated profile.
 * The 'rating' is now a Double, and 'payPalEmail' has been removed as it's
 * handled separately by the seller in their payout profile setup.
 */
@Data
public class CreateSellerRequestDto {

    @NotNull
    @Valid
    private UserRegistrationDto user;

    @NotBlank
    private String name;

    private String contactPhone;

    private String address;

    @Min(value = 0, message = "Rating must be at least 0")
    @Max(value = 5, message = "Rating must be at most 5")
    private Double rating; // Optional on creation, will use default if null

    @Email(message = "PayPal email should be valid.")
    @Size(max = 255)
    private String payPalEmail;
}
