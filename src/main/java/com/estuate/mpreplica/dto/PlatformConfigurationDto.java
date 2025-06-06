package com.estuate.mpreplica.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlatformConfigurationDto {
    @NotBlank
    private String configKey;
    @NotBlank
    private String configValue;
    private String description;
}

