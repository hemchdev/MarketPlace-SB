package com.estuate.mpreplica.service;

import com.estuate.mpreplica.dto.PlatformConfigurationDto;
import com.estuate.mpreplica.entity.PlatformConfiguration;
import com.estuate.mpreplica.exception.ResourceNotFoundException;
import com.estuate.mpreplica.repository.PlatformConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PlatformConfigurationService {

    public static final String BASE_COMMISSION_RATE_KEY = "BASE_COMMISSION_RATE";

    @Autowired
    private PlatformConfigurationRepository configurationRepository;

    @Transactional
    public PlatformConfiguration updateConfiguration(PlatformConfigurationDto dto) {
        PlatformConfiguration config = configurationRepository.findByConfigKey(dto.getConfigKey())
                .orElse(new PlatformConfiguration(dto.getConfigKey(), dto.getConfigValue(), dto.getDescription()));

        config.setConfigValue(dto.getConfigValue());
        if (dto.getDescription() != null) {
            config.setDescription(dto.getDescription());
        }
        return configurationRepository.save(config);
    }

    public PlatformConfiguration getConfiguration(String key) {
        return configurationRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("PlatformConfiguration", "key", key));
    }

    public List<PlatformConfiguration> getAllConfigurations() {
        return configurationRepository.findAll();
    }

    public BigDecimal getBaseCommissionRate() {
        try {
            PlatformConfiguration config = getConfiguration(BASE_COMMISSION_RATE_KEY);
            return new BigDecimal(config.getConfigValue());
        } catch (ResourceNotFoundException | NumberFormatException e) {
            // Log a critical error and return a safe default if the config is missing or invalid
            // This prevents the entire system from failing if the config is not set.
            // In a real system, this might trigger an alert to administrators.
            return new BigDecimal("0.01"); // Safe default of 1%
        }
    }
}