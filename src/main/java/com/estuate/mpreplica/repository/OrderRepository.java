package com.estuate.mpreplica.repository;

import com.estuate.mpreplica.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.items oi " +
            "LEFT JOIN FETCH oi.sellerProductAssignment spa " +
            "LEFT JOIN FETCH spa.product p " +
            "LEFT JOIN FETCH spa.sellerProfile sp " +
            "LEFT JOIN FETCH sp.user " + // User associated with the seller profile of the item
            "LEFT JOIN FETCH o.createdBySellerProfile cbsp " + // Seller profile who created the order (if applicable)
            // "LEFT JOIN FETCH cbsp.user" // If you need user details of order creator seller
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithFullDetails(@Param("orderId") Long orderId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items " +
            "LEFT JOIN FETCH o.createdBySellerProfile " + // Seller who might have created the order
            "ORDER BY o.createdAt DESC")
    List<Order> findAllWithItemsOrderedByDate();

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.items oi " + // Ensures only orders with items matching sellerId are returned
            "LEFT JOIN FETCH o.items detailedItems " + // Fetches all items for the matched orders
            "LEFT JOIN FETCH detailedItems.sellerProductAssignment d_spa " +
            "LEFT JOIN FETCH d_spa.product " +
            "LEFT JOIN FETCH d_spa.sellerProfile " + // Seller of the item
            "LEFT JOIN FETCH o.createdBySellerProfile " + // Seller who created the order (if any)
            "WHERE oi.sellerId = :sellerId " + // Filter by seller ID of the order item
            "ORDER BY o.createdAt DESC")
    List<Order> findOrdersBySellerIdWithFullItems(@Param("sellerId") Long sellerId);
}
