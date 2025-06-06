package com.estuate.mpreplica.service;

import com.estuate.mpreplica.entity.*;
import com.estuate.mpreplica.enums.NotificationType;
import com.estuate.mpreplica.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.List;
import java.util.stream.Collectors;

@Service("loggingNotificationService")
public class LoggingNotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingNotificationServiceImpl.class);

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    // These properties would be used if integrating with a real comms API
    @Value("${boost.comms.api.url:#{null}}") // Default to null if not set
    private String boostCommsApiUrl;

    @Value("${boost.comms.api.key:#{null}}") // Default to null if not set
    private String boostCommsApiKey;

    private static final String STATUS_SENT_SIMULATED = "SENT_SIMULATED";

    @Async("taskExecutor")
    @Override
    public void sendIdMeActionRequired(SellerProfile seller, String verificationUrl) {
        String subject = "Action Required: Complete Your ID.me Verification";
        String message = String.format("Dear %s, please complete your ID.me verification to proceed with your seller account. Verification URL: %s",
                seller.getName(), verificationUrl);
        logger.info("SIMULATING SEND NOTIFICATION (ID.me Action Required) to {}: Subject: '{}'", seller.getUser().getEmail(), subject);
        logNotification(seller.getUser(), seller, seller.getUser().getEmail(), NotificationType.IDME_ACTION_REQUIRED, subject, message, STATUS_SENT_SIMULATED, "idme-" + seller.getId());
    }

    @Async("taskExecutor")
    @Override
    public void sendIdMeApprovedPendingLms(SellerProfile seller) {
        String subject = "ID.me Verification Approved";
        String message = String.format("Dear %s, your ID.me verification has been successfully approved. The next step is to complete the LMS training.",
                seller.getName());
        logger.info("SIMULATING SEND NOTIFICATION (ID.me Approved, LMS Pending) to {}: Subject: '{}'", seller.getUser().getEmail(), subject);
        logNotification(seller.getUser(), seller, seller.getUser().getEmail(), NotificationType.IDME_APPROVED_PENDING_LMS, subject, message, STATUS_SENT_SIMULATED, "idme-approved-" + seller.getId());
    }

    @Async("taskExecutor")
    @Override
    public void sendIdMeDeclined(SellerProfile seller, String reason) {
        String subject = "ID.me Verification Declined";
        String message = String.format("Dear %s, unfortunately, your ID.me verification was declined. Reason: %s",
                seller.getName(), StringUtils.hasText(reason) ? reason : "Not provided.");
        logger.info("SIMULATING SEND NOTIFICATION (ID.me Declined) to {}: Subject: '{}'", seller.getUser().getEmail(), subject);
        logNotification(seller.getUser(), seller, seller.getUser().getEmail(), NotificationType.IDME_DECLINED, subject, message, STATUS_SENT_SIMULATED, "idme-declined-" + seller.getId());
    }

    @Async("taskExecutor")
    @Override
    public void sendLmsInvitation(SellerProfile seller, String lmsUrl) {
        String subject = "Invitation to Complete LMS Training";
        String message = String.format("Dear %s, please complete the required LMS training to activate your seller account. Training URL: %s",
                seller.getName(), lmsUrl);
        logger.info("SIMULATING SEND NOTIFICATION (LMS Invitation) to {}: Subject: '{}'", seller.getUser().getEmail(), subject);
        logNotification(seller.getUser(), seller, seller.getUser().getEmail(), NotificationType.LMS_INVITATION, subject, message, STATUS_SENT_SIMULATED, "lms-invite-" + seller.getId());
    }

    @Async("taskExecutor")
    @Override
    public void sendLmsCompletedSellerActive(SellerProfile seller) {
        String subject = "LMS Training Completed - Account Active!";
        String message = String.format("Dear %s, congratulations! You have successfully completed the LMS training. Your seller account is now active.",
                seller.getName());
        logger.info("SIMULATING SEND NOTIFICATION (LMS Completed, Seller Active) to {}: Subject: '{}'", seller.getUser().getEmail(), subject);
        logNotification(seller.getUser(), seller, seller.getUser().getEmail(), NotificationType.LMS_COMPLETED_SELLER_ACTIVE, subject, message, STATUS_SENT_SIMULATED, "lms-complete-" + seller.getId());
    }

    @Async("taskExecutor")
    @Override
    public void sendLmsFailed(SellerProfile seller, String reason) {
        String subject = "LMS Training Not Completed Successfully";
        String message = String.format("Dear %s, we regret to inform you that your LMS training was not completed successfully. Reason: %s",
                seller.getName(), StringUtils.hasText(reason) ? reason : "Not provided.");
        logger.info("SIMULATING SEND NOTIFICATION (LMS Failed) to {}: Subject: '{}'", seller.getUser().getEmail(), subject);
        logNotification(seller.getUser(), seller, seller.getUser().getEmail(), NotificationType.LMS_FAILED, subject, message, STATUS_SENT_SIMULATED, "lms-failed-" + seller.getId());
    }

    @Async("taskExecutor")
    @Override
    public void sendSellerStatusManuallyUpdated(SellerProfile seller, String adminUsername) {
        String subject = "Your Seller Account Status Has Been Updated";
        String message = String.format("Dear %s, your seller account status has been updated by administrator %s to %s. Reason: %s",
                seller.getName(), adminUsername, seller.getOverallStatus(), StringUtils.hasText(seller.getStatusReason()) ? seller.getStatusReason() : "N/A");
        logger.info("SIMULATING SEND NOTIFICATION (Seller Status Manually Updated) to {}: Subject: '{}'", seller.getUser().getEmail(), subject);
        logNotification(seller.getUser(), seller, seller.getUser().getEmail(), NotificationType.SELLER_STATUS_MANUALLY_UPDATED, subject, message, STATUS_SENT_SIMULATED, "seller-status-update-" + seller.getId());
    }

    @Async("taskExecutor")
    @Override
    public void sendNewOrderItemsToSeller(User sellerUser, SellerProfile sellerProfile, Order order, List<OrderItem> items) {
        String subject = String.format("New Items for Order #%s for Your Store: %s", order.getId(), sellerProfile.getName());
        String itemDetails = items.stream()
                .map(item -> String.format("- %d x %s (SKU: %s) - Item ID: %d, Price: %.2f %s",
                        item.getQuantity(),
                        item.getSellerProductAssignment().getProduct().getName(),
                        item.getSellerProductAssignment().getProduct().getSku(),
                        item.getId(),
                        item.getPriceAtPurchase(),
                        order.getCurrency()))
                .collect(Collectors.joining("\n"));
        String message = String.format("Dear %s,\n\nYou have new items in Order #%s from customer %s that require your attention.\n\nItems to fulfill:\n%s\n\nOrder Placed: %s\nShipping Address:\n%s\n\nPlease review and confirm these items in the seller portal.",
                sellerProfile.getName(),
                order.getId(),
                order.getCustomerName(),
                itemDetails,
                order.getCreatedAt().toLocalDate().toString(),
                order.getShippingAddress());
        logger.info("SIMULATING SEND NOTIFICATION (New Order Items for Seller) to {}: Subject: '{}'", sellerUser.getEmail(), subject);
        logNotification(sellerUser, sellerProfile, sellerUser.getEmail(), NotificationType.NEW_ORDER_ITEMS_FOR_SELLER, subject, message, STATUS_SENT_SIMULATED, "order-" + order.getId() + "-seller-" + sellerProfile.getId());
    }

    @Async("taskExecutor")
    @Override
    public void sendOrderItemConfirmedToCustomer(Order order, OrderItem orderItem) {
        String subject = String.format("Item Confirmed in Your Order #%d", order.getId());
        String message = String.format("Dear %s,\n\nThe item '%s' (Quantity: %d) in your order #%d has been confirmed by the seller %s and is being prepared for shipment.",
                order.getCustomerName(),
                orderItem.getSellerProductAssignment().getProduct().getName(),
                orderItem.getQuantity(),
                order.getId(),
                orderItem.getSellerProductAssignment().getSellerProfile().getName());
        logger.info("SIMULATING SEND NOTIFICATION (Order Item Confirmed by Seller) to {}: Subject: '{}'", order.getCustomerEmail(), subject);
        logNotification(null, null, order.getCustomerEmail(), NotificationType.ORDER_ITEM_CONFIRMED_BY_SELLER_TO_CUSTOMER, subject, message, STATUS_SENT_SIMULATED, "order-" + order.getId() + "-item-" + orderItem.getId() + "-confirmed");
    }

    @Async("taskExecutor")
    @Override
    public void sendOrderItemShippedToCustomer(Order order, OrderItem orderItem) {
        String subject = String.format("An Item from Your Order #%d Has Shipped!", order.getId());
        String trackingInfo = StringUtils.hasText(orderItem.getTrackingNumber()) ?
                String.format("You can track your shipment using %s with tracking number: %s.",
                        StringUtils.hasText(orderItem.getShippingCarrier()) ? orderItem.getShippingCarrier() : "the carrier",
                        orderItem.getTrackingNumber())
                : "Tracking information will be updated soon.";
        String estimatedDelivery = orderItem.getEstimatedDeliveryDate() != null ?
                String.format("Estimated delivery date: %s.", orderItem.getEstimatedDeliveryDate().toLocalDate().toString())
                : "Estimated delivery date: Not available.";

        String message = String.format("Dear %s,\n\nGreat news! The item '%s' (Quantity: %d) from your order #%d, sold by %s, has been shipped.\n\n%s\n%s",
                order.getCustomerName(),
                orderItem.getSellerProductAssignment().getProduct().getName(),
                orderItem.getQuantity(),
                order.getId(),
                orderItem.getSellerProductAssignment().getSellerProfile().getName(),
                trackingInfo,
                estimatedDelivery);
        logger.info("SIMULATING SEND NOTIFICATION (Order Item Shipped) to {}: Subject: '{}'", order.getCustomerEmail(), subject);
        logNotification(null, null, order.getCustomerEmail(), NotificationType.ORDER_ITEM_SHIPPED_TO_CUSTOMER, subject, message, STATUS_SENT_SIMULATED, "order-" + order.getId() + "-item-" + orderItem.getId() + "-shipped");
    }

    @Async("taskExecutor")
    @Override
    public void sendOrderFullyShippedToCustomer(Order order) {
        String subject = String.format("Good News! All Items in Your Order #%d Have Shipped", order.getId());
        String message = String.format("Dear %s,\n\nAll items in your order #%d have now been shipped. You can track individual items via previous notifications or your order details page.",
                order.getCustomerName(), order.getId());
        logger.info("SIMULATING SEND NOTIFICATION (Order Fully Shipped) to {}: Subject: '{}'", order.getCustomerEmail(), subject);
        logNotification(null, null, order.getCustomerEmail(), NotificationType.ORDER_FULLY_SHIPPED_TO_CUSTOMER, subject, message, STATUS_SENT_SIMULATED, "order-" + order.getId() + "-fully-shipped");
    }

    @Async("taskExecutor")
    @Override
    public void sendOrderDeliveredToCustomer(Order order) {
        String subject = String.format("Your Order #%d Has Been Delivered!", order.getId());
        String message = String.format("Dear %s,\n\nWe're pleased to inform you that your order #%d has been successfully delivered. We hope you enjoy your purchase!",
                order.getCustomerName(), order.getId());
        logger.info("SIMULATING SEND NOTIFICATION (Order Delivered) to {}: Subject: '{}'", order.getCustomerEmail(), subject);
        logNotification(null, null, order.getCustomerEmail(), NotificationType.ORDER_DELIVERED_TO_CUSTOMER, subject, message, STATUS_SENT_SIMULATED, "order-" + order.getId() + "-delivered");
    }

    @Async("taskExecutor")
    @Override
    public void sendOrderCancelledToCustomer(Order order, String reason) {
        String subject = String.format("Important Update: Your Order #%d Has Been Cancelled", order.getId());
        String message = String.format("Dear %s,\n\nWe regret to inform you that your order #%d has been cancelled. Reason: %s\n\nIf you have any questions, please contact our support team.",
                order.getCustomerName(), order.getId(), StringUtils.hasText(reason) ? reason : "No specific reason provided.");
        logger.info("SIMULATING SEND NOTIFICATION (Order Cancelled) to {}: Subject: '{}'", order.getCustomerEmail(), subject);
        logNotification(null, null, order.getCustomerEmail(), NotificationType.ORDER_CANCELLED_TO_CUSTOMER, subject, message, STATUS_SENT_SIMULATED, "order-" + order.getId() + "-cancelled");
    }

    @Async("taskExecutor")
    @Override
    public void sendPaymentSuccessToCustomer(Order order) {
        String subject = String.format("Payment Confirmation for Your Order #%d", order.getId());
        String message = String.format("Dear %s,\n\nThis confirms that your payment for order #%d, totaling %s %s, has been successfully processed. We'll notify you once your items are shipped.",
                order.getCustomerName(), order.getId(), order.getTotalAmount().toString(), order.getCurrency());
        logger.info("SIMULATING SEND NOTIFICATION (Payment Successful) to {}: Subject: '{}'", order.getCustomerEmail(), subject);
        logNotification(null, null, order.getCustomerEmail(), NotificationType.ORDER_PAYMENT_SUCCESSFUL_TO_CUSTOMER, subject, message, STATUS_SENT_SIMULATED, "order-" + order.getId() + "-payment-success");
    }

    @Async("taskExecutor")
    @Override
    public void sendPaymentFailedToCustomer(Order order, String reason) {
        String subject = String.format("Payment Issue with Your Order #%d", order.getId());
        String message = String.format("Dear %s,\n\nThere was an issue processing the payment for your order #%d. Reason: %s\nPlease update your payment information or contact support to resolve this.",
                order.getCustomerName(), order.getId(), StringUtils.hasText(reason) ? reason : "Payment processing failed.");
        logger.info("SIMULATING SEND NOTIFICATION (Payment Failed) to {}: Subject: '{}'", order.getCustomerEmail(), subject);
        logNotification(null, null, order.getCustomerEmail(), NotificationType.ORDER_PAYMENT_FAILED_TO_CUSTOMER, subject, message, STATUS_SENT_SIMULATED, "order-" + order.getId() + "-payment-failed");
    }

    @Async("taskExecutor")
    @Override
    public void sendOrderStatusUpdateToCustomer(Order order, String customMessage) {
        String subject = String.format("Update on Your Order #%d", order.getId());
        String message = String.format("Dear %s,\n\nHere's an update on your order #%d.\nCurrent Order Status: %s.\n%s",
                order.getCustomerName(),
                order.getId(),
                order.getOrderStatus().toString().replace("_", " "), // More readable status
                StringUtils.hasText(customMessage) ? customMessage : "Thank you for your patience.");
        logger.info("SIMULATING SEND NOTIFICATION (Order Status Update) to {}: Subject: '{}'", order.getCustomerEmail(), subject);
        logNotification(null, null, order.getCustomerEmail(), NotificationType.ORDER_STATUS_UPDATE_TO_CUSTOMER, subject, message, STATUS_SENT_SIMULATED, "order-" + order.getId() + "-status-update");
    }

    @Override
    public void logNotification(User recipientUser, SellerProfile recipientSellerProfile, String recipientEmail,
                                NotificationType notificationType, String subject, String message, String status, String externalTrackingId) {
        try {
            NotificationLog log = new NotificationLog();
            log.setUser(recipientUser); // Can be null if notification is not for a registered user (e.g., customer email)
            log.setSellerProfile(recipientSellerProfile); // Can be null
            log.setRecipientEmail(recipientEmail);
            log.setNotificationType(notificationType);
            log.setSubject(subject);
            log.setMessage(message); // Potentially large, ensure DB column can handle it
            log.setStatus(status);
            log.setExternalTrackingId(externalTrackingId);
            // Timestamp is usually handled by @CreationTimestamp in the entity
            notificationLogRepository.save(log);
        } catch (Exception e) {
            logger.error("Failed to log notification for recipient {}: {}", recipientEmail, e.getMessage(), e);
            // Depending on requirements, might re-throw or handle otherwise
        }
    }
}
