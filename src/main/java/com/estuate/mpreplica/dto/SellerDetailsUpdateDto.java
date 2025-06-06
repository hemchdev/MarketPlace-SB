package com.estuate.mpreplica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerDetailsUpdateDto {
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Email(message = "PayPal email should be valid.")
    @Size(max = 255)
    private String payPalEmail;
}
