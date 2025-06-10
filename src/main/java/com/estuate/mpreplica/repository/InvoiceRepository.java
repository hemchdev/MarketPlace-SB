package com.estuate.mpreplica.repository;

import com.estuate.mpreplica.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Finds all invoices associated with a specific seller via the payout record.
    @org.springframework.data.jpa.repository.Query("SELECT i FROM Invoice i WHERE i.payout.sellerProfile.id = :sellerProfileId ORDER BY i.generatedAt DESC")
    List<Invoice> findBySellerProfileId(Long sellerProfileId);
}
