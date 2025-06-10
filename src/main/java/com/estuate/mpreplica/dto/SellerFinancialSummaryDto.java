package com.estuate.mpreplica.dto;

import com.estuate.mpreplica.enums.PayoutStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A Data Transfer Object that provides a comprehensive financial summary for a single seller.
 * This includes aggregated lifetime earnings, commissions, payouts, and current balance.
 */
@Data
@NoArgsConstructor
public class SellerFinancialSummaryDto {

    private Long sellerProfileId;
    private String sellerName;

    /**
     * The total gross revenue credited to the seller from all sales.
     */
    private BigDecimal totalEarnings;

    /**
     * The total amount of commissions debited from the seller.
     * (Presented as a positive value for readability).
     */
    private BigDecimal totalCommissions;

    /**
     * The total amount successfully paid out to the seller.
     * (Presented as a positive value for readability).
     */
    private BigDecimal totalPayouts;

    /**
     * The net total of all manual adjustments (credits - debits).
     */
    private BigDecimal netAdjustments;

    /**
     * The final current balance available for the next payout.
     * This is the result of (Earnings + Adjustments) - Commissions - Payouts.
     */
    private BigDecimal currentPayableBalance;

    // --- Last Payout Information ---
    private PayoutStatus lastPayoutStatus;
    private BigDecimal lastPayoutAmount;
    private LocalDateTime lastPayoutDate;
    private String lastPayoutFailureReason;
}
