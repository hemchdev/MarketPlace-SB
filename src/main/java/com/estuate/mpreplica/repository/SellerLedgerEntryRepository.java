package com.estuate.mpreplica.repository;

import com.estuate.mpreplica.entity.SellerLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SellerLedgerEntryRepository extends JpaRepository<SellerLedgerEntry, Long> {
    List<SellerLedgerEntry> findBySellerProfileIdOrderByCreatedAtDesc(Long sellerProfileId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM SellerLedgerEntry e WHERE e.sellerProfile.id = :sellerProfileId")
    BigDecimal getBalanceForSeller(@Param("sellerProfileId") Long sellerProfileId);
}
