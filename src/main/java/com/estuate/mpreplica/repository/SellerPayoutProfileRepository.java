package com.estuate.mpreplica.repository;

import com.estuate.mpreplica.entity.SellerPayoutProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerPayoutProfileRepository extends JpaRepository<SellerPayoutProfile, Long> {
    Optional<SellerPayoutProfile> findBySellerProfileId(Long sellerProfileId);
}
