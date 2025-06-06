package com.estuate.mpreplica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "platform_configuration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlatformConfiguration {

    @Id
    @Column(name = "config_key", length = 100)
    private String configKey; // e.g., "BASE_COMMISSION_RATE"

    @Column(name = "config_value", nullable = false, length = 255)
    private String configValue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}


