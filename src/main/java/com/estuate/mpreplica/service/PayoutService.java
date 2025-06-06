package com.estuate.mpreplica.service;

import com.estuate.mpreplica.dto.PayoutDto;
import com.estuate.mpreplica.dto.PayoutRunSummaryDto;
import com.estuate.mpreplica.dto.SellerLedgerEntryDto;
import com.estuate.mpreplica.entity.*;
import com.estuate.mpreplica.enums.PayoutStatus;
import com.estuate.mpreplica.enums.SellerLedgerEntryType;
import com.estuate.mpreplica.exception.ResourceNotFoundException;
import com.estuate.mpreplica.mapper.PayoutMapper;
import com.estuate.mpreplica.mapper.SellerLedgerEntryMapper;
import com.estuate.mpreplica.repository.PayoutRepository;
import com.estuate.mpreplica.repository.SellerLedgerEntryRepository;
import com.estuate.mpreplica.repository.SellerProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PayoutService {
    private static final Logger logger = LoggerFactory.getLogger(PayoutService.class);

    @Autowired private SellerLedgerEntryRepository ledgerRepository;
    @Autowired private PayoutRepository payoutRepository;
    @Autowired private SellerProfileRepository sellerProfileRepository;
    @Autowired private SellerLedgerEntryMapper ledgerMapper;
    @Autowired private PayoutMapper payoutMapper;
    @Autowired private PlatformConfigurationService configurationService; // Inject new service


    @Value("${marketplace.payout.minimum-balance}")
    private BigDecimal minimumPayoutBalance;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createLedgerEntriesForDeliveredOrder(Order order) {
        logger.info("Creating ledger entries for delivered Order ID: {}", order.getId());

        // Fetch the global base commission rate once per order processing
        BigDecimal baseCommissionRate = configurationService.getBaseCommissionRate();

        for (OrderItem item : order.getItems()) {
            SellerProfile sellerProfile = item.getSellerProductAssignment().getSellerProfile();
            BigDecimal saleAmount = item.getSubtotal();

            SellerLedgerEntry saleCredit = new SellerLedgerEntry(
                    sellerProfile, SellerLedgerEntryType.SALE_CREDIT, saleAmount,
                    "Sale credit for item ID " + item.getId() + " in order ID " + order.getId()
            );
            saleCredit.setOrder(order);
            ledgerRepository.save(saleCredit);

            // Calculate commission: basePrice * baseCommissionRate * sellerRating
            BigDecimal productBasePrice = item.getSellerProductAssignment().getProduct().getBasePrice();
            Integer sellerRating = sellerProfile.getRating();

            if (sellerRating == null) {
                logger.error("CRITICAL: Rating for seller ID {} is null. Cannot calculate commission for item ID {}.", sellerProfile.getId(), item.getId());
                continue; // Skip commission for this item
            }

            BigDecimal commissionAmount = productBasePrice
                    .multiply(baseCommissionRate)
                    .multiply(BigDecimal.valueOf(sellerRating))
                    .setScale(4, RoundingMode.HALF_UP);

            SellerLedgerEntry commissionDebit = new SellerLedgerEntry(
                    sellerProfile, SellerLedgerEntryType.COMMISSION_DEBIT, commissionAmount.negate(),
                    "Commission for item ID " + item.getId() + " (Base Rate: " + baseCommissionRate + ", Rating: " + sellerRating + ")"
            );
            commissionDebit.setOrder(order);
            ledgerRepository.save(commissionDebit);
            logger.debug("Ledger entries for seller ID {} item ID {}: CREDIT {}, DEBIT {} (Formula: {} * {} * {})",
                    sellerProfile.getId(), item.getId(), saleAmount, commissionAmount.negate(), productBasePrice, baseCommissionRate, sellerRating);
        }
        logger.info("Finished creating ledger entries for Order ID: {}", order.getId());
    }

    @Transactional
    public PayoutRunSummaryDto initiatePayoutRun() {
        logger.info("Initiating manual payout run for all eligible IWCs (Sellers).");
        List<SellerProfile> allSellers = sellerProfileRepository.findAll();
        PayoutRunSummaryDto summary = new PayoutRunSummaryDto(0, 0, 0, BigDecimal.ZERO, "");

        for (SellerProfile seller : allSellers) {
            if (!StringUtils.hasText(seller.getPayPalEmail())) {
                logger.info("Skipping payout for seller {} (ID {}): PayPal email not set.", seller.getName(), seller.getId());
                summary.setPayoutsSkipped(summary.getPayoutsSkipped() + 1);
                continue;
            }
            if (payoutRepository.existsBySellerProfileIdAndStatusIn(seller.getId(),
                    Arrays.asList(PayoutStatus.PENDING, PayoutStatus.PROCESSING))) {
                logger.info("Skipping payout for seller {} (ID {}): Payout already in progress.", seller.getName(), seller.getId());
                summary.setPayoutsSkipped(summary.getPayoutsSkipped() + 1);
                continue;
            }

            BigDecimal balance = ledgerRepository.getBalanceForSeller(seller.getId());
            if (balance.compareTo(minimumPayoutBalance) >= 0) {
                logger.info("Seller {} (ID {}, PayPal: {}) has payable balance {}. Initiating payout.",
                        seller.getName(), seller.getId(), seller.getPayPalEmail(), balance);

                Payout payout = new Payout();
                payout.setSellerProfile(seller);
                payout.setAmount(balance);
                payout.setStatus(PayoutStatus.PENDING);
                Payout savedPayout = payoutRepository.save(payout);

                // Simulate calling PSP (PayPal Sandbox in this conceptual context)
                logger.info("SIMULATING PAYPAL PAYOUT: Transferring {} to seller {} (IWC) via PayPal (Email: {}).",
                        balance, seller.getName(), seller.getPayPalEmail());

                boolean pspSuccess = Math.random() > 0.1; // 90% success rate for simulation
                String pspTransactionId = "paypal_sandbox_txn_" + UUID.randomUUID().toString().substring(0,12);

                if (pspSuccess) {
                    savedPayout.setStatus(PayoutStatus.COMPLETED);
                    savedPayout.setPspTransactionId(pspTransactionId);
                    savedPayout.setCompletedAt(LocalDateTime.now());

                    SellerLedgerEntry payoutDebit = new SellerLedgerEntry(
                            seller, SellerLedgerEntryType.PAYOUT_DEBIT, balance.negate(),
                            "PayPal Payout processed. Txn ID: " + pspTransactionId
                    );
                    payoutDebit.setPayout(savedPayout);
                    ledgerRepository.save(payoutDebit);

                    payoutRepository.save(savedPayout);
                    summary.setPayoutsInitiated(summary.getPayoutsInitiated() + 1);
                    summary.setTotalAmountInitiated(summary.getTotalAmountInitiated().add(balance));
                    logger.info("Payout ID {} for seller {} (ID {}) completed successfully. Txn: {}",
                            savedPayout.getId(), seller.getName(), seller.getId(), pspTransactionId);
                } else {
                    savedPayout.setStatus(PayoutStatus.FAILED);
                    savedPayout.setFailureReason("Simulated PayPal Sandbox failure.");
                    payoutRepository.save(savedPayout);
                    summary.setPayoutsFailed(summary.getPayoutsFailed() + 1);
                    logger.error("Payout ID {} for seller {} (ID {}) failed.",
                            savedPayout.getId(), seller.getName(), seller.getId());
                }
            } else {
                logger.info("Skipping payout for seller {} (ID {}): Balance {} is below minimum {}.",
                        seller.getName(), seller.getId(), balance, minimumPayoutBalance);
                summary.setPayoutsSkipped(summary.getPayoutsSkipped() + 1);
            }
        }

        String message = String.format("Payout run completed. Initiated: %d, Skipped: %d, Failed: %d. Total Amount Initiated: %s",
                summary.getPayoutsInitiated(), summary.getPayoutsSkipped(), summary.getPayoutsFailed(), summary.getTotalAmountInitiated());
        summary.setMessage(message);
        logger.info(message);
        return summary;
    }

    public List<SellerLedgerEntryDto> getLedgerForSeller(Long sellerProfileId) {
        if (!sellerProfileRepository.existsById(sellerProfileId)) {
            throw new ResourceNotFoundException("SellerProfile", "id", sellerProfileId);
        }
        List<SellerLedgerEntry> entries = ledgerRepository.findBySellerProfileIdOrderByCreatedAtDesc(sellerProfileId);
        return entries.stream()
                .map(ledgerMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<PayoutDto> getAllPayouts() {
        List<Payout> payouts = payoutRepository.findAllWithSellerProfile();
        return payouts.stream()
                .map(payoutMapper::toDto)
                .collect(Collectors.toList());
    }
}

