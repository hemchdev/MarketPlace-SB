package com.estuate.mpreplica.dto;

import com.estuate.mpreplica.enums.LmsStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LmsWebhookPayload {
    @NotNull(message = "Seller ID (our internal ID) is required")
    private Long sellerProfileId; // Our internal SellerProfile ID

    @NotBlank(message = "LMS External ID is required")
    private String lmsExternalId; // The ID from LMS's system

    @NotNull(message = "LMS Status is required")
    private LmsStatus status; // e.g., COMPLETED, FAILED

    private String courseName;
    private String completionDate; // ISO Date string
    private String reason; // Optional reason from LMS
}

