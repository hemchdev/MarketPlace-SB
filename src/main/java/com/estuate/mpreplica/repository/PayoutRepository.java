package com.estuate.mpreplica.repository;

import com.estuate.mpreplica.entity.Payout;
import com.estuate.mpreplica.enums.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {

    boolean existsBySellerProfileIdAndStatusIn(Long sellerProfileId, List<PayoutStatus> statuses);

    @Query("SELECT p FROM Payout p JOIN FETCH p.sellerProfile ORDER BY p.initiatedAt DESC")
    List<Payout> findAllWithSellerProfile();

    /**
     * Finds the most recent payout attempt for a given seller.
     * @param sellerProfileId The ID of the seller's profile.
     * @return An Optional containing the most recent Payout.
     */
    Optional<Payout> findTopBySellerProfileIdOrderByInitiatedAtDesc(Long sellerProfileId);
}
