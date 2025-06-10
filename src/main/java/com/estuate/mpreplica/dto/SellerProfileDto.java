package com.estuate.mpreplica.dto;

import com.estuate.mpreplica.enums.IdMeStatus;
import com.estuate.mpreplica.enums.LmsStatus;
import com.estuate.mpreplica.enums.SellerOverallStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SellerProfileDto {
    private Long id;
    private Long userId;
    private String username;
    private String userEmail;
    private String name;
    private String contactPhone;
    private String address;
    private IdMeStatus idMeStatus;
    private String idMeExternalId;
    private String idMeVerificationUrl;
    private String idMeVerificationDetailsLink;
    private LmsStatus lmsStatus;
    private String lmsExternalId;
    private String lmsInvitationUrl;
    private String lmsCourseName;
    private String lmsCompletionDateDetails;
    private SellerOverallStatus overallStatus;
    private String statusReason;
    private Double currentRating;
    private BigDecimal commissionRateOverride;
    private String payPalEmail; // RESTORED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}

