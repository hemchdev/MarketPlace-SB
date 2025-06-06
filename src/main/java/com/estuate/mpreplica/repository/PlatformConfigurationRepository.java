package com.estuate.mpreplica.repository;

import com.estuate.mpreplica.entity.PlatformConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformConfigurationRepository extends JpaRepository<PlatformConfiguration, String> {
    Optional<PlatformConfiguration> findByConfigKey(String configKey);
}

