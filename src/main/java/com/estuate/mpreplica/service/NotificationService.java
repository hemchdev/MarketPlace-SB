package com.estuate.mpreplica.service;

import com.estuate.mpreplica.entity.Order;
import com.estuate.mpreplica.entity.OrderItem;
import com.estuate.mpreplica.entity.SellerProfile;
import com.estuate.mpreplica.entity.User;
import com.estuate.mpreplica.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    void sendIdMeActionRequired(SellerProfile sellerProfile, String verificationUrl);
    void sendIdMeApprovedPendingLms(SellerProfile sellerProfile);
    void sendIdMeDeclined(SellerProfile sellerProfile, String reason);

    void sendLmsInvitation(SellerProfile sellerProfile, String lmsUrl);
    void sendLmsCompletedSellerActive(SellerProfile sellerProfile);
    void sendLmsFailed(SellerProfile sellerProfile, String reason);

    void sendSellerStatusManuallyUpdated(SellerProfile sellerProfile, String adminUsername);

    void sendNewOrderItemsToSeller(User sellerUser, SellerProfile sellerProfile, Order order, List<OrderItem> items);
    void sendOrderItemConfirmedToCustomer(Order order, OrderItem orderItem);
    void sendOrderItemShippedToCustomer(Order order, OrderItem orderItem);
    void sendOrderFullyShippedToCustomer(Order order);
    void sendOrderDeliveredToCustomer(Order order);
    void sendOrderCancelledToCustomer(Order order, String reason);

    void sendPaymentSuccessToCustomer(Order order);
    void sendPaymentFailedToCustomer(Order order, String reason);
    void sendOrderStatusUpdateToCustomer(Order order, String customMessage);

    void logNotification(
            User recipientUser,
            SellerProfile recipientSellerProfile,
            String recipientEmail,
            NotificationType notificationType,
            String subject,
            String message,
            String status,
            String externalTrackingId
    );
}




