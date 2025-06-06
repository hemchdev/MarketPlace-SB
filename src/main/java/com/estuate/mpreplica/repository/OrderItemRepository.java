package com.estuate.mpreplica.repository;

import com.estuate.mpreplica.entity.Order;
import com.estuate.mpreplica.entity.OrderItem;
import com.estuate.mpreplica.enums.OrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN FETCH oi.order o " +
            "JOIN FETCH oi.sellerProductAssignment spa " +
            "JOIN FETCH spa.product p " +
            "JOIN FETCH spa.sellerProfile sp " +
            "JOIN FETCH sp.user " + // User associated with the seller of the product
            "WHERE oi.id = :orderItemId AND oi.sellerId = :sellerId")
    Optional<OrderItem> findByIdAndSellerIdWithDetails(@Param("orderItemId") Long orderItemId, @Param("sellerId") Long sellerId);

    List<OrderItem> findByOrderIdAndSellerId(Long orderId, Long sellerId);

    List<OrderItem> findByOrderAndItemStatus(Order order, OrderItemStatus status);

    long countByOrderAndItemStatus(Order order, OrderItemStatus status);

    long countByOrderIdAndItemStatusIn(Long orderId, List<OrderItemStatus> statuses);

    long countByOrderId(Long orderId);
}