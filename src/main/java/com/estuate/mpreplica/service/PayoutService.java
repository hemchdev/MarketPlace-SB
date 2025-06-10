package com.estuate.mpreplica.service;

import com.estuate.mpreplica.dto.*;
import com.estuate.mpreplica.entity.*;
import com.estuate.mpreplica.enums.PayoutStatus;
import com.estuate.mpreplica.enums.SellerLedgerEntryType;
import com.estuate.mpreplica.events.PayoutCompletedEvent;
import com.estuate.mpreplica.exception.InvalidOperationException;
import com.estuate.mpreplica.exception.ResourceNotFoundException;
import com.estuate.mpreplica.mapper.PayoutMapper;
import com.estuate.mpreplica.mapper.SellerLedgerEntryMapper;
import com.estuate.mpreplica.repository.OrderRepository;
import com.estuate.mpreplica.repository.PayoutRepository;
import com.estuate.mpreplica.repository.SellerLedgerEntryRepository;
import com.estuate.mpreplica.repository.SellerProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PayoutService {
    private static final Logger logger = LoggerFactory.getLogger(PayoutService.class);

    @Autowired private SellerLedgerEntryRepository ledgerRepository;
    @Autowired private PayoutRepository payoutRepository;
    @Autowired private SellerProfileRepository sellerProfileRepository;
    @Autowired private OrderRepository orderRepository;

    // CORRECTED: Removed the unnecessary @Qualifier annotation.
    // Spring will now find the single 'sellerLedgerEntryMapperImpl' bean correctly.
    @Autowired private SellerLedgerEntryMapper ledgerMapper;

    @Autowired private PayoutMapper payoutMapper;
    @Autowired private ApplicationEventPublisher eventPublisher;

    @Value("${marketplace.payout.minimum-balance}")
    private BigDecimal minimumPayoutBalance;

    /**
     * Generates a detailed financial summary for a given seller.
     */
    @Transactional(readOnly = true)
    public SellerFinancialSummaryDto getFinancialSummaryForSeller(Long sellerProfileId) {
        SellerProfile seller = sellerProfileRepository.findById(sellerProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerProfile", "id", sellerProfileId));

        SellerFinancialSummaryDto summary = new SellerFinancialSummaryDto();
        summary.setSellerProfileId(seller.getId());
        summary.setSellerName(seller.getName());

        BigDecimal totalEarnings = ledgerRepository.findTotalAmountBySellerProfileIdAndEntryTypeIn(sellerProfileId, List.of(SellerLedgerEntryType.SALE_CREDIT));
        BigDecimal totalCommissions = ledgerRepository.findTotalAmountBySellerProfileIdAndEntryTypeIn(sellerProfileId, List.of(SellerLedgerEntryType.COMMISSION_DEBIT));
        BigDecimal totalPayouts = ledgerRepository.findTotalAmountBySellerProfileIdAndEntryTypeIn(sellerProfileId, List.of(SellerLedgerEntryType.PAYOUT_DEBIT));
        BigDecimal netAdjustments = ledgerRepository.findTotalAmountBySellerProfileIdAndEntryTypeIn(sellerProfileId, List.of(SellerLedgerEntryType.ADJUSTMENT_CREDIT, SellerLedgerEntryType.ADJUSTMENT_DEBIT));

        summary.setTotalEarnings(totalEarnings);
        summary.setTotalCommissions(totalCommissions.abs());
        summary.setTotalPayouts(totalPayouts.abs());
        summary.setNetAdjustments(netAdjustments);
        summary.setCurrentPayableBalance(ledgerRepository.getBalanceForSeller(sellerProfileId));

        payoutRepository.findTopBySellerProfileIdOrderByInitiatedAtDesc(sellerProfileId).ifPresent(lastPayout -> {
            summary.setLastPayoutStatus(lastPayout.getStatus());
            summary.setLastPayoutAmount(lastPayout.getAmount());
            summary.setLastPayoutDate(lastPayout.getInitiatedAt());
            summary.setLastPayoutFailureReason(lastPayout.getFailureReason());
        });

        return summary;
    }

    /**
     * Initiates a payout run for all eligible sellers.
     */
    @Transactional
    public PayoutRunSummaryDto initiatePayoutRunForAllSellers() {
        logger.info("Initiating simulated payout run for ALL eligible sellers.");
        List<SellerProfile> allSellers = sellerProfileRepository.findAll();
        PayoutRunSummaryDto summary = new PayoutRunSummaryDto(0, 0, 0, BigDecimal.ZERO, "Payout run for all sellers completed.");

        for (SellerProfile seller : allSellers) {
            try {
                if (initiatePayoutForSeller(seller) != null) {
                    summary.setPayoutsInitiated(summary.getPayoutsInitiated() + 1);
                } else {
                    summary.setPayoutsSkipped(summary.getPayoutsSkipped() + 1);
                }
            } catch (Exception e) {
                logger.error("Failed to process payout for seller ID {} during bulk run: {}", seller.getId(), e.getMessage());
                summary.setPayoutsFailed(summary.getPayoutsFailed() + 1);
            }
        }
        return summary;
    }

    /**
     * Initiates a payout for a single, specific seller.
     */
    @Transactional
    public Payout initiatePayoutForSeller(SellerProfile seller) {
        if (!StringUtils.hasText(seller.getPayPalEmail())) {
            logger.warn("Skipping payout for seller {} (ID {}): Payment email not set.", seller.getName(), seller.getId());
            return null;
        }

        if (payoutRepository.existsBySellerProfileIdAndStatusIn(seller.getId(), List.of(PayoutStatus.PENDING, PayoutStatus.PROCESSING))) {
            throw new InvalidOperationException("Payout for seller " + seller.getName() + " is already in progress.");
        }

        BigDecimal balance = ledgerRepository.getBalanceForSeller(seller.getId());

        if (balance.compareTo(minimumPayoutBalance) < 0) {
            logger.info("Skipping payout for seller {} (ID {}): Balance {} is below minimum {}.",
                    seller.getName(), seller.getId(), balance, minimumPayoutBalance);
            return null;
        }

        logger.info("Seller {} has a payable balance of {}. Initiating simulated payout.", seller.getName(), balance);
        Payout payout = new Payout();
        payout.setSellerProfile(seller);
        payout.setAmount(balance);
        payout.setStatus(PayoutStatus.PROCESSING);
        Payout savedPayout = payoutRepository.save(payout);
        boolean pspSuccess = Math.random() > 0.1;
        String transactionId = "sim_txn_" + UUID.randomUUID().toString().substring(0, 17);

        if (pspSuccess) {
            savedPayout.setStatus(PayoutStatus.COMPLETED);
            savedPayout.setPspTransactionId(transactionId);
            savedPayout.setCompletedAt(LocalDateTime.now());
            payoutRepository.save(savedPayout);

            SellerLedgerEntry payoutDebit = new SellerLedgerEntry(
                    seller, SellerLedgerEntryType.PAYOUT_DEBIT, balance.negate(),
                    "Simulated Payout processed. Txn ID: " + transactionId
            );
            payoutDebit.setPayout(savedPayout);
            ledgerRepository.save(payoutDebit);

            eventPublisher.publishEvent(new PayoutCompletedEvent(this, savedPayout));
            logger.info("Payout ID {} for seller {} completed successfully.", savedPayout.getId(), seller.getName());
            return savedPayout;
        } else {
            savedPayout.setStatus(PayoutStatus.FAILED);
            savedPayout.setFailureReason("Simulated PSP failure.");
            payoutRepository.save(savedPayout);
            logger.error("Payout ID {} for seller {} failed during simulation.", savedPayout.getId(), seller.getName());
            throw new RuntimeException("Simulated PSP failure for seller " + seller.getName());
        }
    }

    /**
     * Retrieves the full ledger history for a specific seller.
     */
    public List<SellerLedgerEntryDto> getLedgerForSeller(Long sellerProfileId) {
        if (!sellerProfileRepository.existsById(sellerProfileId)) {
            throw new ResourceNotFoundException("SellerProfile", "id", sellerProfileId);
        }
        return ledgerRepository.findBySellerProfileIdOrderByCreatedAtDesc(sellerProfileId).stream()
                .map(ledgerMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all payout records from the system.
     */
    public List<PayoutDto> getAllPayouts() {
        return payoutRepository.findAllWithSellerProfile().stream()
                .map(payoutMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Processes a refund as an operator-driven action.
     */
    @Transactional
    public SellerLedgerEntryDto processRefund(RefundRequestDto refundDto) {
        SellerProfile sellerProfile = sellerProfileRepository.findById(refundDto.getSellerProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("SellerProfile", "id", refundDto.getSellerProfileId()));

        Order order = orderRepository.findById(refundDto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", refundDto.getOrderId()));
        if (refundDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive.");
        }
        String description = String.format("Refund for Order ID %d. Reason: %s",
                refundDto.getOrderId(), refundDto.getReason());
        SellerLedgerEntry refundDebit = new SellerLedgerEntry(
                sellerProfile,
                SellerLedgerEntryType.REFUND_DEBIT,
                refundDto.getAmount().negate(),
                description
        );
        refundDebit.setOrder(order);
        SellerLedgerEntry savedEntry = ledgerRepository.save(refundDebit);
        logger.info("Processed refund debit of {} for Seller ID {}.",
                refundDto.getAmount(), refundDto.getSellerProfileId());
        return ledgerMapper.toDto(savedEntry);
    }
}
