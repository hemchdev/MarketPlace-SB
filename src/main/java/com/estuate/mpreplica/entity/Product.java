package com.estuate.mpreplica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(columnNames = "sku"))
@Getter
@Setter
@NoArgsConstructor
public class Product extends Auditable { // Assuming Auditable class exists
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(length = 100)
    private String category;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_image_urls", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", length = 1024)
    private List<String> imageUrls = new ArrayList<>();

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal basePrice;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_attributes", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "attribute_key", length = 100)
    @Column(name = "attribute_value", length = 255)
    private Map<String, String> attributes = new HashMap<>();

    @Column(name = "created_by_operator_id")
    private Long createdByOperatorId;

    @Column(name = "updated_by_operator_id")
    private Long updatedByOperatorId;
}
