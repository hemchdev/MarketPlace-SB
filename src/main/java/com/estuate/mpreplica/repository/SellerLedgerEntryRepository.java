package com.estuate.mpreplica.repository;

import com.estuate.mpreplica.entity.SellerLedgerEntry;
import com.estuate.mpreplica.enums.SellerLedgerEntryType;
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

    /**
     * Calculates the sum of ledger entry amounts for a specific seller and a given list of entry types.
     * COALESCE ensures that 0 is returned instead of null if no entries are found.
     * @param sellerProfileId The ID of the seller's profile.
     * @param entryTypes A list of SellerLedgerEntryType enums to sum.
     * @return The total sum as a BigDecimal.
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM SellerLedgerEntry e WHERE e.sellerProfile.id = :sellerProfileId AND e.entryType IN :entryTypes")
    BigDecimal findTotalAmountBySellerProfileIdAndEntryTypeIn(@Param("sellerProfileId") Long sellerProfileId, @Param("entryTypes") List<SellerLedgerEntryType> entryTypes);
}
