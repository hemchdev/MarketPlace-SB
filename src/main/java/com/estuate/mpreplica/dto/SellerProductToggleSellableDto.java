package com.estuate.mpreplica.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class SellerProductToggleSellableDto {

    @NotNull(message = "isSellableBySeller field cannot be null")
    private Boolean isSellableBySeller;
}
