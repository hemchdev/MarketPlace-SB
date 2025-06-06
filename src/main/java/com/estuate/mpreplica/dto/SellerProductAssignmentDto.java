package com.estuate.mpreplica.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data public class SellerProductAssignmentDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long sellerProfileId;
    private String sellerName;
    private BigDecimal productBasePrice;
    private int stockQuantity;
    private boolean isSellableBySeller;
    private Long assignedByOperatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
