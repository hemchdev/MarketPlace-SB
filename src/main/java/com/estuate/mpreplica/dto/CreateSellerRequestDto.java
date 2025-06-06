package com.estuate.mpreplica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal; // Not used but kept for consistency if needed later

@Data
public class CreateSellerRequestDto {
    @NotNull @Valid private UserRegistrationDto user;
    @NotBlank private String name;
    private String contactPhone;
    private String address;

    // ADDED: rating field to match service logic
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating; // Optional on creation, will use default if null

    @Email(message = "PayPal email should be valid.")
    @Size(max = 255)
    private String payPalEmail;
}


