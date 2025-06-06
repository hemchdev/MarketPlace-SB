package com.estuate.mpreplica.controller;

import com.estuate.mpreplica.dto.*;
import com.estuate.mpreplica.service.OrderService;
import com.estuate.mpreplica.service.SellerProductAssignmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/sellers")
@PreAuthorize("hasAuthority('SELLER')")
public class SellerController {
    private static final Logger logger = LoggerFactory.getLogger(SellerController.class);
    @Autowired private SellerProductAssignmentService assignmentService;
    @Autowired private OrderService orderService;

    // === Product Management ===
    @GetMapping("/my-products")
    public ResponseEntity<List<SellerProductAssignmentDto>> getMyAssignedProducts() {
        return ResponseEntity.ok(assignmentService.getMyAssignedProducts());
    }

    @PutMapping("/my-products/{assignmentId}/stock")
    public ResponseEntity<?> updateMyStock(@PathVariable Long assignmentId, @Valid @RequestBody SellerStockUpdateDto dto) {
        try {
            return ResponseEntity.ok(assignmentService.updateMyStock(assignmentId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }


    @PutMapping("/my-products/{assignmentId}/toggle-sellable")
    public ResponseEntity<?> toggleMyProductSellable(@PathVariable Long assignmentId, @Valid @RequestBody SellerProductToggleSellableDto dto) {
        try {
            return ResponseEntity.ok(assignmentService.toggleMyProductSellable(assignmentId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }

    // === Order Management ===
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponseDto>> getMyOrders() {
        return ResponseEntity.ok(orderService.getOrdersForSeller());
    }

    @PutMapping("/my-orders/{orderItemId}/fulfill")
    public ResponseEntity<?> fulfillOrderItem(@PathVariable Long orderItemId, @Valid @RequestBody OrderItemFulfillmentDto dto) {
        try {
            return ResponseEntity.ok(orderService.fulfillOrderItemBySeller(orderItemId, dto));
        } catch (IllegalArgumentException | IllegalStateException | AccessDeniedException e) {
            logger.warn("Seller failed to fulfill order item ID {}: {}", orderItemId, e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }
}

