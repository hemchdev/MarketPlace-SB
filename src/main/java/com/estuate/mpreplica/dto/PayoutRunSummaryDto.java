package com.estuate.mpreplica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayoutRunSummaryDto {
    private int payoutsInitiated;
    private int payoutsSkipped;
    private int payoutsFailed;
    private BigDecimal totalAmountInitiated;
    private String message;
}

