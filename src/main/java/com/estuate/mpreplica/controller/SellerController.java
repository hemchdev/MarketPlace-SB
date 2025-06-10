package com.estuate.mpreplica.controller;

import com.estuate.mpreplica.dto.*;
import com.estuate.mpreplica.entity.SellerPayoutProfile;
import com.estuate.mpreplica.entity.SellerProfile;
import com.estuate.mpreplica.exception.ResourceNotFoundException;
import com.estuate.mpreplica.repository.SellerPayoutProfileRepository;
import com.estuate.mpreplica.repository.SellerProfileRepository;
import com.estuate.mpreplica.security.UserDetailsImpl;
import com.estuate.mpreplica.service.OrderService;
import com.estuate.mpreplica.service.SellerProductAssignmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/sellers")
@PreAuthorize("hasAuthority('SELLER')")
public class SellerController {
    private static final Logger logger = LoggerFactory.getLogger(SellerController.class);
    @Autowired private SellerProductAssignmentService assignmentService;
    @Autowired private OrderService orderService;
    @Autowired private SellerProfileRepository sellerProfileRepository;
    @Autowired private SellerPayoutProfileRepository sellerPayoutProfileRepository;

    private UserDetailsImpl getAuthenticatedUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

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

    // === NEW ENDPOINT for Payout Profile ===
    @PostMapping("/my-payout-profile")
    public ResponseEntity<?> setupPayoutProfile(@Valid @RequestBody SellerPayoutProfileDto dto) {
        UserDetailsImpl user = getAuthenticatedUser();
        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("SellerProfile", "user", user.getId()));

        SellerPayoutProfile payoutProfile = sellerPayoutProfileRepository.findBySellerProfileId(sellerProfile.getId())
                .orElse(new SellerPayoutProfile(sellerProfile, dto.getPspIdentifier()));

        payoutProfile.setPspIdentifier(dto.getPspIdentifier());
        payoutProfile.setEnabled(true); // Always enabled on update

        SellerPayoutProfile savedProfile = sellerPayoutProfileRepository.save(payoutProfile);

        // You would create a mapper for this in a real application
        dto.setId(savedProfile.getId());
        dto.setSellerProfileId(savedProfile.getSellerProfile().getId());
        dto.setEnabled(savedProfile.isEnabled());

        return ResponseEntity.ok(dto);
    }
}
