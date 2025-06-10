package com.estuate.mpreplica.repository;

import com.estuate.mpreplica.entity.CommissionTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommissionTierRepository extends JpaRepository<CommissionTier, Long> {
    /**
     * Finds all commission tiers that are currently active.
     * @return A list of active commission tiers.
     */
    List<CommissionTier> findByIsActiveTrue();
}
