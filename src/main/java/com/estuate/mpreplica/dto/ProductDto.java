package com.estuate.mpreplica.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Data
public class ProductDto {

    private Long id;
    private String name;
    private String description;
    private String sku;
    private String category;
    private List<String> imageUrls;
    private BigDecimal basePrice;
    private Map<String, String> attributes;
    private Long createdByOperatorId;
    private Long updatedByOperatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy; // User-friendly name or ID
    private String updatedBy; // User-friendly name or ID
}


