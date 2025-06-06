package com.estuate.mpreplica.dto;

import com.estuate.mpreplica.enums.SellerOverallStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class SellerStatusUpdateDto {

    @NotNull(message = "Overall status cannot be null")
    private SellerOverallStatus overallStatus;

    private String reason; // Reason can be optional
}
