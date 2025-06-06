package com.estuate.mpreplica.dto;


import com.estuate.mpreplica.enums.SellerLedgerEntryType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SellerLedgerEntryDto {
    private Long id;
    private Long sellerProfileId;
    private SellerLedgerEntryType entryType;
    private BigDecimal amount;
    private Long orderId;
    private Long payoutId;
    private String description;
    private LocalDateTime createdAt;
}

