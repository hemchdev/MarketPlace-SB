package com.estuate.mpreplica.dto;

import com.estuate.mpreplica.enums.PayoutStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayoutDto {
    private Long id;
    private Long sellerProfileId;
    private String sellerName; // Was sellerBusinessName
    private BigDecimal amount;
    private PayoutStatus status;
    private String pspTransactionId;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
    private String failureReason;
}
