package com.estuate.mpreplica.dto;

import jakarta.validation.constraints.Min;

import lombok.Data;

@Data
public class SellerProductAssignmentUpdateDto {
    @Min(0) private Integer stockQuantity;
    private Boolean isSellableBySeller;
}

