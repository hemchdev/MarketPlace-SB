package com.estuate.mpreplica.service;

import com.estuate.mpreplica.entity.Order;
import com.estuate.mpreplica.entity.OrderItem;
import com.estuate.mpreplica.entity.SellerLedgerEntry;
import com.estuate.mpreplica.entity.SellerProfile;
import com.estuate.mpreplica.enums.SellerLedgerEntryType;
import com.estuate.mpreplica.events.OrderDeliveredEvent;
import com.estuate.mpreplica.repository.SellerLedgerEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service responsible for handling financial remittance logic.
 * It listens for key application events (like OrderDeliveredEvent) and creates
 * the corresponding immutable entries in the seller's ledger.
 */
@Service
public class RemittanceService {

    private static final Logger logger = LoggerFactory.getLogger(RemittanceService.class);

    @Autowired
    private SellerLedgerEntryRepository ledgerRepository;

    @Autowired
    private CommissionService commissionService;

    /**
     * Listens for the OrderDeliveredEvent and processes the remittance for each item in the order.
     * This method runs asynchronously and transactionally to avoid blocking the main thread
     * and ensure atomicity of ledger entries for a given order.
     *
     * @param event The OrderDeliveredEvent containing the finalized order.
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        Order order = event.getOrder();
        logger.info("RemittanceService handling OrderDeliveredEvent for Order ID: {}", order.getId());

        for (OrderItem item : order.getItems()) {
            // We only process items that were actually delivered.
            if (item.getItemStatus() == com.estuate.mpreplica.enums.OrderItemStatus.DELIVERED_TO_CUSTOMER) {
                calculateAndRecordRemittance(item);
            }
        }
        logger.info("Finished processing remittance for Order ID: {}", order.getId());
    }

    /**
     * Calculates and records the financial entries for a single delivered order item.
     * It creates two atomic ledger entries:
     * 1. A SALE_CREDIT for the full subtotal of the item.
     * 2. A COMMISSION_DEBIT calculated using the dynamic CommissionService.
     *
     * @param orderItem The delivered OrderItem to process.
     */
    private void calculateAndRecordRemittance(OrderItem orderItem) {
        SellerProfile sellerProfile = orderItem.getSellerProductAssignment().getSellerProfile();
        BigDecimal saleAmount = orderItem.getSubtotal();
        Order order = orderItem.getOrder();

        // 1. Record the SALE_CREDIT to the seller's ledger.
        SellerLedgerEntry saleCredit = new SellerLedgerEntry(
                sellerProfile,
                SellerLedgerEntryType.SALE_CREDIT,
                saleAmount,
                "Sale credit for item ID " + orderItem.getId() + " in order ID " + order.getId()
        );
        saleCredit.setOrder(order);
        ledgerRepository.save(saleCredit);

        // 2. Get the applicable commission rate and calculate the commission debit.
        BigDecimal applicableRate = commissionService.getApplicableCommissionRate(sellerProfile);

        // Commission is calculated on the price at the time of purchase.
        BigDecimal commissionAmount = orderItem.getPriceAtPurchase()
                .multiply(BigDecimal.valueOf(orderItem.getQuantity())) // total value for this line item
                .multiply(applicableRate)
                .setScale(4, RoundingMode.HALF_UP);

        SellerLedgerEntry commissionDebit = new SellerLedgerEntry(
                sellerProfile,
                SellerLedgerEntryType.COMMISSION_DEBIT,
                commissionAmount.negate(), // Commissions are a debit (negative value)
                "Commission for item ID " + orderItem.getId() + " (Rate: " + applicableRate + ")"
        );
        commissionDebit.setOrder(order);
        ledgerRepository.save(commissionDebit);

        logger.info("Recorded remittance for OrderItem ID {}: SALE_CREDIT of {}, COMMISSION_DEBIT of {}",
                orderItem.getId(), saleAmount, commissionAmount.negate());
    }
}
