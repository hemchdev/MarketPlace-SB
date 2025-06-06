package com.estuate.mpreplica.repository;

import com.estuate.mpreplica.entity.Product;
import com.estuate.mpreplica.entity.SellerProductAssignment;
import com.estuate.mpreplica.entity.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;


@Repository
public interface SellerProductAssignmentRepository extends JpaRepository<SellerProductAssignment, Long> {

    Optional<SellerProductAssignment> findByProductAndSellerProfile(Product product, SellerProfile sellerProfile);

    boolean existsByProductIdAndSellerProfileId(Long productId, Long sellerProfileId);

    @Query("SELECT spa FROM SellerProductAssignment spa " +
            "JOIN FETCH spa.product " +
            "JOIN FETCH spa.sellerProfile sp " +
            "JOIN FETCH sp.user " +
            "WHERE spa.sellerProfile.id = :sellerProfileId")
    List<SellerProductAssignment> findBySellerProfileIdWithDetails(@Param("sellerProfileId") Long sellerProfileId);

    @Query("SELECT spa FROM SellerProductAssignment spa " +
            "JOIN FETCH spa.product p " +
            "JOIN FETCH spa.sellerProfile sp " +
            "JOIN FETCH sp.user u " +
            "WHERE spa.id = :assignmentId")
    Optional<SellerProductAssignment> findByIdWithDetails(@Param("assignmentId") Long assignmentId);

    @Query("SELECT spa FROM SellerProductAssignment spa " +
            "JOIN FETCH spa.product p " +
            "JOIN FETCH spa.sellerProfile sp " +
            "WHERE spa.id = :assignmentId AND sp.user.id = :userId")
    Optional<SellerProductAssignment> findByIdAndSellerUserIdWithDetails(@Param("assignmentId") Long assignmentId, @Param("userId") Long userId);
}