package com.estuate.mpreplica.controller;

import com.estuate.mpreplica.dto.*;
import com.estuate.mpreplica.entity.CommissionTier;
import com.estuate.mpreplica.entity.Payout;
import com.estuate.mpreplica.entity.PlatformConfiguration;
import com.estuate.mpreplica.entity.SellerProfile;
import com.estuate.mpreplica.exception.ResourceNotFoundException;
import com.estuate.mpreplica.mapper.PayoutMapper;
import com.estuate.mpreplica.mapper.SellerProfileMapper;
import com.estuate.mpreplica.repository.CommissionTierRepository;
import com.estuate.mpreplica.repository.SellerProfileRepository;
import com.estuate.mpreplica.service.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/operators")
@PreAuthorize("hasAuthority('OPERATOR')")
public class OperatorController {
    private static final Logger logger = LoggerFactory.getLogger(OperatorController.class);

    @Autowired private SellerProfileService sellerProfileService;
    @Autowired private ProductService productService;
    @Autowired private SellerProductAssignmentService assignmentService;
    @Autowired private OrderService orderService;
    @Autowired private PayoutService payoutService;
    @Autowired private PlatformConfigurationService configurationService;
    @Autowired private CommissionTierRepository commissionTierRepository;
    @Autowired private SellerProfileRepository sellerProfileRepository;
    @Autowired private SellerProfileMapper sellerProfileMapper;
    @Autowired private PayoutMapper payoutMapper;

    // === Seller Management ===
    @PostMapping("/create-seller")
    public ResponseEntity<?> createSeller(@Valid @RequestBody CreateSellerRequestDto request) {
        logger.info("Operator request to create seller: {}", request.getUser().getUsername());
        try {
            SellerProfileDto sellerProfile = sellerProfileService.createSellerWithProfile(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(sellerProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating seller for user {}: {}", request.getUser().getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDto("An unexpected error occurred while creating the seller."));
        }
    }

    @GetMapping("/sellers")
    public ResponseEntity<List<SellerProfileDto>> getAllSellers() {
        return ResponseEntity.ok(sellerProfileService.getAllSellerProfiles());
    }

    @GetMapping("/sellers/{sellerProfileId}")
    public ResponseEntity<SellerProfileDto> getSellerById(@PathVariable Long sellerProfileId) {
        return ResponseEntity.ok(sellerProfileService.getSellerProfileById(sellerProfileId));
    }

    @PutMapping("/sellers/{sellerProfileId}/status")
    public ResponseEntity<?> updateSellerStatus(@PathVariable Long sellerProfileId, @Valid @RequestBody SellerStatusUpdateDto dto) {
        try {
            return ResponseEntity.ok(sellerProfileService.updateSellerOverallStatus(sellerProfileId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }

    @PostMapping("/sellers/{sellerProfileId}/initiate-idme")
    public ResponseEntity<?> initiateIdMe(@PathVariable Long sellerProfileId) {
        try {
            SellerProfileDto updatedProfile = sellerProfileService.initiateIdMeVerification(sellerProfileId);
            return ResponseEntity.ok(new MessageResponseDto("ID.me verification initiated. URL: " + updatedProfile.getIdMeVerificationUrl()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }

    @PutMapping("/sellers/{sellerProfileId}/details")
    public ResponseEntity<?> updateSellerDetails(@PathVariable Long sellerProfileId,
                                                 @Valid @RequestBody SellerDetailsUpdateDto detailsUpdateDto) {
        logger.info("Operator request to update details for seller ID: {}", sellerProfileId);
        SellerProfileDto updatedProfile = sellerProfileService.updateSellerDetails(sellerProfileId, detailsUpdateDto);
        return ResponseEntity.ok(updatedProfile);
    }

    // === Product Catalog Management ===
    @PostMapping("/products")
    public ResponseEntity<?> createMasterProduct(@Valid @RequestBody ProductCreateDto dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getAllMasterProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDto> getMasterProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<?> updateMasterProduct(@PathVariable Long productId, @Valid @RequestBody ProductCreateDto dto) {
        try {
            return ResponseEntity.ok(productService.updateProduct(productId, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> deleteMasterProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(new MessageResponseDto("Product ID " + productId + " deleted successfully."));
    }

    // === Seller Product Assignment Management ===
    @PostMapping("/sellers/{sellerProfileId}/products/assign")
    public ResponseEntity<?> assignProductToSeller(@PathVariable Long sellerProfileId, @Valid @RequestBody SellerProductAssignmentCreateDto dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.assignProductToSeller(sellerProfileId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }

    @GetMapping("/sellers/{sellerProfileId}/products")
    public ResponseEntity<List<SellerProductAssignmentDto>> getAssignedProductsForSeller(@PathVariable Long sellerProfileId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsForSeller(sellerProfileId));
    }

    @PutMapping("/sellers/{sellerProfileId}/products/{assignmentId}")
    public ResponseEntity<?> updateSellerProductAssignmentByOperator(@PathVariable Long sellerProfileId, @PathVariable Long assignmentId, @Valid @RequestBody SellerProductAssignmentUpdateDto dto) {
        try {
            return ResponseEntity.ok(assignmentService.updateAssignmentByOperator(sellerProfileId, assignmentId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }

    // === Order Management ===
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrdersForOperator());
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderByIdForOperator(orderId));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @Valid @RequestBody OperatorOrderStatusUpdateDto dto) {
        try {
            return ResponseEntity.ok(orderService.updateOrderStatusByOperator(orderId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }

    // === Financials, Payouts & Commission Management ===

    @GetMapping("/sellers/{sellerProfileId}/financial-summary")
    public ResponseEntity<SellerFinancialSummaryDto> getSellerFinancialSummary(@PathVariable Long sellerProfileId) {
        logger.info("Operator request for financial summary for seller ID: {}", sellerProfileId);
        SellerFinancialSummaryDto summary = payoutService.getFinancialSummaryForSeller(sellerProfileId);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/payouts/initiate-all")
    public ResponseEntity<PayoutRunSummaryDto> initiatePayoutsForAll() {
        logger.info("Operator request to initiate payout run for ALL sellers.");
        PayoutRunSummaryDto summary = payoutService.initiatePayoutRunForAllSellers();
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/sellers/{sellerProfileId}/payouts/initiate")
    public ResponseEntity<?> initiatePayoutForSeller(@PathVariable Long sellerProfileId) {
        logger.info("Operator request to initiate payout for seller ID: {}", sellerProfileId);
        try {
            SellerProfile seller = sellerProfileService.getSellerProfileEntityById(sellerProfileId);
            Payout payout = payoutService.initiatePayoutForSeller(seller);
            if (payout != null) {
                return ResponseEntity.ok(payoutMapper.toDto(payout));
            } else {
                return ResponseEntity.ok(new MessageResponseDto("Payout skipped. Seller may not be eligible or balance is below minimum."));
            }
        } catch (Exception e) {
            logger.error("Error initiating payout for seller ID {}: {}", sellerProfileId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDto(e.getMessage()));
        }
    }

    @GetMapping("/sellers/{sellerProfileId}/ledger")
    public ResponseEntity<List<SellerLedgerEntryDto>> getSellerLedger(@PathVariable Long sellerProfileId) {
        logger.info("Operator request to view ledger for seller profile ID: {}", sellerProfileId);
        List<SellerLedgerEntryDto> ledgerEntries = payoutService.getLedgerForSeller(sellerProfileId);
        return ResponseEntity.ok(ledgerEntries);
    }

    @GetMapping("/payouts")
    public ResponseEntity<List<PayoutDto>> getAllPayouts() {
        logger.info("Operator request to view all payouts.");
        List<PayoutDto> payouts = payoutService.getAllPayouts();
        return ResponseEntity.ok(payouts);
    }

    @PostMapping("/commissions/tiers")
    public ResponseEntity<CommissionTier> createCommissionTier(@Valid @RequestBody CommissionTier tier) {
        logger.info("Operator creating new commission tier: {}", tier.getTierName());
        CommissionTier savedTier = commissionTierRepository.save(tier);
        return new ResponseEntity<>(savedTier, HttpStatus.CREATED);
    }

    @GetMapping("/commissions/tiers")
    public ResponseEntity<List<CommissionTier>> getAllCommissionTiers() {
        logger.info("Operator fetching all commission tiers.");
        return ResponseEntity.ok(commissionTierRepository.findAll());
    }

    @PutMapping("/commissions/tiers/{tierId}")
    public ResponseEntity<CommissionTier> updateCommissionTier(@PathVariable Long tierId, @Valid @RequestBody CommissionTier tierDetails) {
        logger.info("Operator updating commission tier ID: {}", tierId);
        CommissionTier tier = commissionTierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("CommissionTier", "id", tierId));

        tier.setTierName(tierDetails.getTierName());
        tier.setMinRatingRequired(tierDetails.getMinRatingRequired());
        tier.setCommissionRate(tierDetails.getCommissionRate());
        tier.setActive(tierDetails.isActive());

        return ResponseEntity.ok(commissionTierRepository.save(tier));
    }

    @DeleteMapping("/commissions/tiers/{tierId}")
    public ResponseEntity<?> deleteCommissionTier(@PathVariable Long tierId) {
        logger.info("Operator deleting commission tier ID: {}", tierId);
        commissionTierRepository.deleteById(tierId);
        return ResponseEntity.ok(new MessageResponseDto("Commission tier deleted successfully."));
    }

    @PutMapping("/sellers/{sellerProfileId}/commission-override")
    public ResponseEntity<?> setSellerCommissionOverride(@PathVariable Long sellerProfileId, @RequestBody(required = false) BigDecimal overrideRate) {
        logger.info("Operator setting commission override for seller ID {} to {}", sellerProfileId, overrideRate);
        SellerProfile profile = sellerProfileService.getSellerProfileEntityById(sellerProfileId);
        profile.setCommissionRateOverride(overrideRate);
        SellerProfile updatedProfile = sellerProfileRepository.save(profile);
        return ResponseEntity.ok(sellerProfileMapper.toDto(updatedProfile));
    }

    @PostMapping("/refunds")
    public ResponseEntity<?> issueRefund(@Valid @RequestBody RefundRequestDto refundDto) {
        try {
            logger.info("Operator initiating refund of {} for seller ID {} on order ID {}",
                    refundDto.getAmount(), refundDto.getSellerProfileId(), refundDto.getOrderId());
            SellerLedgerEntryDto ledgerEntry = payoutService.processRefund(refundDto);
            return ResponseEntity.ok(ledgerEntry);
        } catch (Exception e) {
            logger.error("Refund processing failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }

    // === Platform Configuration Management ===
    @GetMapping("/config")
    public ResponseEntity<List<PlatformConfiguration>> getAllPlatformConfigs() {
        return ResponseEntity.ok(configurationService.getAllConfigurations());
    }

    @PutMapping("/config")
    public ResponseEntity<PlatformConfiguration> updatePlatformConfig(@Valid @RequestBody PlatformConfigurationDto dto) {
        logger.info("Operator request to update platform configuration for key: {}", dto.getConfigKey());
        PlatformConfiguration updatedConfig = configurationService.updateConfiguration(dto);
        return ResponseEntity.ok(updatedConfig);
    }
}
