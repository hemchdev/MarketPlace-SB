package com.estuate.mpreplica.repository;

import com.estuate.mpreplica.entity.Payout;
import com.estuate.mpreplica.enums.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {
    boolean existsBySellerProfileIdAndStatusIn(Long sellerProfileId, List<PayoutStatus> statuses);

    @Query("SELECT p FROM Payout p JOIN FETCH p.sellerProfile ORDER BY p.initiatedAt DESC")
    List<Payout> findAllWithSellerProfile();
}
