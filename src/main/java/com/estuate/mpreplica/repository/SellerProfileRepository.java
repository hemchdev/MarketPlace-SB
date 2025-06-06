package com.estuate.mpreplica.repository;


import com.estuate.mpreplica.entity.SellerProfile;
import com.estuate.mpreplica.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findByUser(User user);
    Optional<SellerProfile> findByUserId(Long userId);

    @Query("SELECT sp FROM SellerProfile sp JOIN FETCH sp.user WHERE sp.id = :id")
    Optional<SellerProfile> findByIdWithUser(Long id);
}
