package com.estuate.mpreplica.dto;

import com.estuate.mpreplica.enums.IdMeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import jakarta.validation.constraints.*;


@Data
public class IdMeWebhookPayload {

    @NotNull(message = "Seller profile ID cannot be null")
    private Long sellerProfileId;

    @NotBlank(message = "ID.me external ID cannot be blank")
    private String idMeExternalId;

    @NotNull(message = "Status cannot be null")
    private IdMeStatus status;

    private String reason; // Optional

    @Size(max = 2048, message = "Verification details link cannot exceed 2048 characters")
    private String verificationDetailsLink; // Optional
}
