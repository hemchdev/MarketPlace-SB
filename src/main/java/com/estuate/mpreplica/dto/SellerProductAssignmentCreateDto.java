package com.estuate.mpreplica.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class SellerProductAssignmentCreateDto {
    @NotNull private Long productId;
    @NotNull @Min(0) private Integer initialStock;
}



