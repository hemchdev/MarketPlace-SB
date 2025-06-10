package com.estuate.mpreplica.service;

import com.estuate.mpreplica.dto.*;
import com.estuate.mpreplica.entity.Order;
import com.estuate.mpreplica.entity.OrderItem;
import com.estuate.mpreplica.entity.SellerProductAssignment;
import com.estuate.mpreplica.entity.SellerProfile;
import com.estuate.mpreplica.enums.OrderItemStatus;
import com.estuate.mpreplica.enums.OrderStatus;
import com.estuate.mpreplica.enums.SellerOverallStatus;
import com.estuate.mpreplica.events.OrderDeliveredEvent;
import com.estuate.mpreplica.exception.InsufficientStockException;
import com.estuate.mpreplica.exception.InvalidOperationException;
import com.estuate.mpreplica.exception.ResourceNotFoundException;
import com.estuate.mpreplica.mapper.OrderItemMapper;
import com.estuate.mpreplica.mapper.OrderMapper;
import com.estuate.mpreplica.repository.*;
import com.estuate.mpreplica.security.UserDetailsImpl;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private SellerProductAssignmentRepository assignmentRepository;
    @Autowired private SellerProfileRepository sellerProfileRepository;
    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private NotificationService notificationService;
    @Autowired private ApplicationEventPublisher eventPublisher; // MODIFIED: For publishing events

    // NOTE: PayoutService is removed as a direct dependency.
    // The link is now through the OrderDeliveredEvent and RemittanceService.
    // @Autowired private PayoutService payoutService;

    @Transactional
    @Retryable(
            value = { OptimisticLockException.class, org.springframework.dao.OptimisticLockingFailureException.class, org.hibernate.StaleObjectStateException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2)
    )
    protected UserDetailsImpl getAuthenticatedUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        throw new AccessDeniedException("User not authenticated or authentication principal is not of type UserDetailsImpl.");
    }

    @Transactional
    @Retryable(
            value = { OptimisticLockException.class, org.springframework.dao.OptimisticLockingFailureException.class, org.hibernate.StaleObjectStateException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2)
    )
    public OrderResponseDto createOrder(OrderCreateDto dto) {
        UserDetailsImpl authenticatedUser = getAuthenticatedUserDetails();
        Long authenticatedUserId = authenticatedUser.getId();
        String authenticatedUsername = authenticatedUser.getUsername();

        SellerProfile creatingSellerProfile = sellerProfileRepository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new InvalidOperationException("Authenticated user " + authenticatedUsername +
                        " (ID: " + authenticatedUserId + ") does not have a valid seller profile. Cannot create order."));

        logger.info("Seller {} (User ID: {}) is creating an order for customer: {}",
                creatingSellerProfile.getName(), authenticatedUserId, dto.getCustomerEmail());

        Order order = new Order();
        order.setCustomerName(dto.getCustomerName());
        order.setCustomerEmail(dto.getCustomerEmail());
        order.setCustomerPhone(dto.getCustomerPhone());
        order.setShippingAddress(dto.getShippingAddress());
        order.setBillingAddress(StringUtils.hasText(dto.getBillingAddress()) ? dto.getBillingAddress() : dto.getShippingAddress());
        order.setCurrency(StringUtils.hasText(dto.getCurrency()) ? dto.getCurrency().toUpperCase() : "USD");
        order.setCreatedBySellerProfile(creatingSellerProfile);
        order.setPaymentId(dto.getPaymentId());
        order.setPaymentStatus(dto.getPaymentStatus());
        order.setPaymentMethodDetails(dto.getPaymentMethodDetails());

        if ("SUCCESS".equalsIgnoreCase(dto.getPaymentStatus()) || "PAID".equalsIgnoreCase(dto.getPaymentStatus()) || "PAID_IN_STORE".equalsIgnoreCase(dto.getPaymentStatus())) {
            order.setOrderStatus(OrderStatus.PAYMENT_RECEIVED);
        } else {
            order.setOrderStatus(OrderStatus.AWAITING_PAYMENT);
        }

        BigDecimal totalOrderAmount = BigDecimal.ZERO;

        for (OrderItemRequestDto itemRequest : dto.getItems()) {
            SellerProductAssignment assignment = assignmentRepository.findByIdWithDetails(itemRequest.getSellerProductAssignmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("SellerProductAssignment", "id", itemRequest.getSellerProductAssignmentId()));

            if (!assignment.getSellerProfile().getId().equals(creatingSellerProfile.getId())) {
                throw new AccessDeniedException("Cannot order item (Assignment ID: " + assignment.getId() +
                        ") as it is not assigned to your seller profile (" + creatingSellerProfile.getName() + "). It belongs to seller: " + assignment.getSellerProfile().getName());
            }

            if (!assignment.isSellableBySeller()) {
                throw new InvalidOperationException("Product '" + assignment.getProduct().getName() + "' (SKU: " +
                        assignment.getProduct().getSku() + ") is currently not marked as sellable by you.");
            }

            if (assignment.getSellerProfile().getOverallStatus() != SellerOverallStatus.ACTIVE) {
                throw new InvalidOperationException("Your seller account (" + creatingSellerProfile.getName() +
                        ") is not currently ACTIVE. Cannot process order. Current status: " + creatingSellerProfile.getOverallStatus());
            }

            if (assignment.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product '" + assignment.getProduct().getName() +
                        "'. Available: " + assignment.getStockQuantity() + ", Requested: " + itemRequest.getQuantity());
            }

            assignment.setStockQuantity(assignment.getStockQuantity() - itemRequest.getQuantity());
            assignmentRepository.saveAndFlush(assignment);

            OrderItem orderItem = new OrderItem();
            orderItem.setSellerProductAssignment(assignment);
            orderItem.setSellerId(creatingSellerProfile.getId());
            orderItem.setQuantity(itemRequest.getQuantity());

            BigDecimal priceAtPurchase = assignment.getProduct().getBasePrice();

            orderItem.setPriceAtPurchase(priceAtPurchase);
            orderItem.setSubtotal(priceAtPurchase.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            orderItem.setItemStatus(OrderItemStatus.PENDING_SELLER_CONFIRMATION);

            order.addItem(orderItem);
            totalOrderAmount = totalOrderAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalOrderAmount);
        Order savedOrder = orderRepository.save(order);

        logger.info("Order ID: {} created successfully by Seller {} for customer {}. Total: {} {}",
                savedOrder.getId(), creatingSellerProfile.getName(), savedOrder.getCustomerEmail(), savedOrder.getTotalAmount(), savedOrder.getCurrency());

        notificationService.sendNewOrderItemsToSeller(creatingSellerProfile.getUser(), creatingSellerProfile, savedOrder, savedOrder.getItems());

        if (order.getOrderStatus() == OrderStatus.PAYMENT_RECEIVED) {
            notificationService.sendPaymentSuccessToCustomer(savedOrder);
        } else if (order.getOrderStatus() == OrderStatus.AWAITING_PAYMENT) {
            notificationService.sendOrderStatusUpdateToCustomer(savedOrder, "Your order has been placed and is awaiting payment.");
        }

        return orderMapper.toDto(savedOrder);
    }

    public OrderResponseDto getOrderByIdForOperator(Long orderId) {
        Order order = orderRepository.findByIdWithFullDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return orderMapper.toDto(order);
    }

    public List<OrderResponseDto> getAllOrdersForOperator() {
        return orderRepository.findAllWithItemsOrderedByDate().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDto updateOrderStatusByOperator(Long orderId, OperatorOrderStatusUpdateDto dto) {
        Order order = orderRepository.findByIdWithFullDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        UserDetailsImpl operatorDetails = getAuthenticatedUserDetails();

        logger.info("Operator {} (ID: {}) updating Order ID {} from status {} to {}. Reason: {}",
                operatorDetails.getUsername(), operatorDetails.getId(), orderId, order.getOrderStatus(), dto.getStatus(), dto.getReason());

        OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(dto.getStatus());
        order.setOrderStatusReason(dto.getReason());

        if (dto.getStatus() == OrderStatus.CANCELLED_BY_OPERATOR) {
            order.getItems().forEach(item -> {
                if (item.getItemStatus() != OrderItemStatus.DELIVERED_TO_CUSTOMER &&
                        item.getItemStatus() != OrderItemStatus.RETURNED_RECEIVED_BY_SELLER &&
                        item.getItemStatus() != OrderItemStatus.CANCELLED_BY_OPERATOR &&
                        item.getItemStatus() != OrderItemStatus.CANCELLED_BY_SELLER) {

                    item.setItemStatus(OrderItemStatus.CANCELLED_BY_OPERATOR);
                    item.setCancellationReason(StringUtils.hasText(dto.getReason()) ? "Cancelled by Operator: " + dto.getReason() : "Cancelled by Operator");

                    if (item.getItemStatus() == OrderItemStatus.CONFIRMED_BY_SELLER || item.getItemStatus() == OrderItemStatus.AWAITING_SHIPMENT) {
                        SellerProductAssignment assignment = item.getSellerProductAssignment();
                        assignment.setStockQuantity(assignment.getStockQuantity() + item.getQuantity());
                        assignmentRepository.save(assignment);
                        logger.info("Stock for Product Assignment ID {} restored by {} due to operator cancellation of OrderItem ID {}",
                                assignment.getId(), item.getQuantity(), item.getId());
                    }
                }
            });
        }

        Order updatedOrder = orderRepository.save(order);

        String notificationMessage = String.format("An operator has updated your order status. Reason: %s",
                StringUtils.hasText(dto.getReason()) ? dto.getReason() : "No specific reason provided.");
        notificationService.sendOrderStatusUpdateToCustomer(updatedOrder, notificationMessage);

        if (updatedOrder.getOrderStatus() == OrderStatus.CANCELLED_BY_OPERATOR && oldStatus != OrderStatus.CANCELLED_BY_OPERATOR) {
            notificationService.sendOrderCancelledToCustomer(updatedOrder, dto.getReason());
        }

        // MODIFICATION: Check for delivered status and publish event
        if (updatedOrder.getOrderStatus() == OrderStatus.DELIVERED && oldStatus != OrderStatus.DELIVERED) {
            eventPublisher.publishEvent(new OrderDeliveredEvent(this, updatedOrder));
            notificationService.sendOrderDeliveredToCustomer(updatedOrder);
        }


        return orderMapper.toDto(updatedOrder);
    }

    public List<OrderResponseDto> getOrdersForSeller() {
        Long sellerUserId = getAuthenticatedUserDetails().getId();
        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerProfile", "userId", sellerUserId + " (No seller profile for current user)"));

        return orderRepository.findOrdersBySellerIdWithFullItems(sellerProfile.getId()).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderItemResponseDto fulfillOrderItemBySeller(Long orderItemId, OrderItemFulfillmentDto dto) {
        UserDetailsImpl sellerUserDetails = getAuthenticatedUserDetails();
        Long sellerUserId = sellerUserDetails.getId();

        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerProfile", "userId", sellerUserId + " (No seller profile)"));

        OrderItem orderItem = orderItemRepository.findByIdAndSellerIdWithDetails(orderItemId, sellerProfile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id",
                        "OrderItem ID " + orderItemId + " not found or not assigned to seller " + sellerProfile.getName()));

        logger.info("Seller {} (User ID: {}) attempting to update OrderItem ID {} from status {} to {}. Tracking: {}",
                sellerProfile.getName(), sellerUserId, orderItemId, orderItem.getItemStatus(), dto.getStatus(), dto.getTrackingNumber());

        OrderItemStatus currentItemStatus = orderItem.getItemStatus();
        OrderItemStatus nextItemStatus = dto.getStatus();

        if (nextItemStatus == OrderItemStatus.CONFIRMED_BY_SELLER) {
            if (currentItemStatus != OrderItemStatus.PENDING_SELLER_CONFIRMATION) {
                throw new InvalidOperationException("Item can only be CONFIRMED if it is currently PENDING_SELLER_CONFIRMATION. Current status: " + currentItemStatus);
            }
        } else if (nextItemStatus == OrderItemStatus.AWAITING_SHIPMENT) {
            if (currentItemStatus != OrderItemStatus.CONFIRMED_BY_SELLER) {
                throw new InvalidOperationException("Item must be CONFIRMED_BY_SELLER to move to AWAITING_SHIPMENT. Current item status: " + currentItemStatus);
            }
            if (orderItem.getOrder().getOrderStatus() != OrderStatus.PAYMENT_RECEIVED && orderItem.getOrder().getOrderStatus() != OrderStatus.PROCESSING_BY_SELLERS) {
                throw new InvalidOperationException("Order must be PAID to move item to AWAITING_SHIPMENT. Current order status: " + orderItem.getOrder().getOrderStatus());
            }
        } else if (nextItemStatus == OrderItemStatus.SHIPPED) {
            if (currentItemStatus != OrderItemStatus.CONFIRMED_BY_SELLER && currentItemStatus != OrderItemStatus.AWAITING_SHIPMENT) {
                throw new InvalidOperationException("Item can only be SHIPPED if it is CONFIRMED_BY_SELLER or AWAITING_SHIPMENT. Current status: " + currentItemStatus);
            }
            if (!StringUtils.hasText(dto.getTrackingNumber())) {
                throw new InvalidOperationException("Tracking number is required when marking an item as SHIPPED.");
            }
            orderItem.setTrackingNumber(dto.getTrackingNumber());
            orderItem.setShippingCarrier(dto.getShippingCarrier());
            orderItem.setEstimatedDeliveryDate(dto.getEstimatedDeliveryDate());
        } else if (nextItemStatus == OrderItemStatus.CANCELLED_BY_SELLER) {
            if (currentItemStatus == OrderItemStatus.SHIPPED || currentItemStatus == OrderItemStatus.DELIVERED_TO_CUSTOMER) {
                throw new InvalidOperationException("Cannot cancel an item that is already SHIPPED or DELIVERED_TO_CUSTOMER. Current status: " + currentItemStatus);
            }
            if (currentItemStatus == OrderItemStatus.CONFIRMED_BY_SELLER || currentItemStatus == OrderItemStatus.AWAITING_SHIPMENT || currentItemStatus == OrderItemStatus.PENDING_SELLER_CONFIRMATION) {
                SellerProductAssignment assignment = orderItem.getSellerProductAssignment();
                assignment.setStockQuantity(assignment.getStockQuantity() + orderItem.getQuantity());
                assignmentRepository.save(assignment);
                logger.info("Stock for Product Assignment ID {} restored by {} due to seller cancellation of OrderItem ID {}.",
                        assignment.getId(), orderItem.getQuantity(), orderItem.getId());
            }
            orderItem.setCancellationReason(StringUtils.hasText(dto.getCancellationReason()) ? dto.getCancellationReason() : "Cancelled by seller " + sellerProfile.getName());
        } else {
            throw new InvalidOperationException("Seller cannot directly set item status to: " + nextItemStatus);
        }

        orderItem.setItemStatus(nextItemStatus);
        OrderItem updatedOrderItem = orderItemRepository.save(orderItem);

        checkAndUpdateOverallOrderStatus(updatedOrderItem.getOrder().getId());

        if (updatedOrderItem.getItemStatus() == OrderItemStatus.CONFIRMED_BY_SELLER) {
            notificationService.sendOrderItemConfirmedToCustomer(updatedOrderItem.getOrder(), updatedOrderItem);
        } else if (updatedOrderItem.getItemStatus() == OrderItemStatus.SHIPPED) {
            notificationService.sendOrderItemShippedToCustomer(updatedOrderItem.getOrder(), updatedOrderItem);
        }
        return orderItemMapper.toDto(updatedOrderItem);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndUpdateOverallOrderStatus(Long orderId) {
        Order order = orderRepository.findByIdWithFullDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId + " (while checking overall status)"));

        List<OrderItem> items = order.getItems();
        if (items.isEmpty()) {
            logger.warn("Order ID {} has no items; cannot determine overall status accurately.", orderId);
            return;
        }

        long totalItems = items.size();
        long confirmedItems = items.stream().filter(i -> i.getItemStatus() == OrderItemStatus.CONFIRMED_BY_SELLER).count();
        long awaitingShipmentItems = items.stream().filter(i -> i.getItemStatus() == OrderItemStatus.AWAITING_SHIPMENT).count();
        long shippedItems = items.stream().filter(i -> i.getItemStatus() == OrderItemStatus.SHIPPED).count();
        long deliveredItems = items.stream().filter(i -> i.getItemStatus() == OrderItemStatus.DELIVERED_TO_CUSTOMER).count();
        long cancelledItems = items.stream().filter(i -> i.getItemStatus() == OrderItemStatus.CANCELLED_BY_SELLER || i.getItemStatus() == OrderItemStatus.CANCELLED_BY_OPERATOR).count();
        long pendingConfirmationItems = items.stream().filter(i -> i.getItemStatus() == OrderItemStatus.PENDING_SELLER_CONFIRMATION).count();

        OrderStatus currentOverallStatus = order.getOrderStatus();
        OrderStatus newOverallStatus = currentOverallStatus;

        long activeItems = totalItems - cancelledItems;

        if (activeItems == 0 && totalItems > 0) {
            newOverallStatus = (currentOverallStatus == OrderStatus.CANCELLED_BY_OPERATOR) ? OrderStatus.CANCELLED_BY_OPERATOR : OrderStatus.CANCELLED_BY_SELLER;
        } else if (activeItems > 0) {
            if (deliveredItems == activeItems) {
                newOverallStatus = OrderStatus.DELIVERED;
            } else if ((shippedItems + deliveredItems) == activeItems) {
                newOverallStatus = OrderStatus.SHIPPED;
            } else if (deliveredItems > 0 && (shippedItems + deliveredItems) < activeItems) {
                newOverallStatus = OrderStatus.PARTIALLY_DELIVERED;
            } else if (shippedItems > 0 && (shippedItems < activeItems)) {
                newOverallStatus = OrderStatus.PARTIALLY_SHIPPED;
            } else if ((confirmedItems + awaitingShipmentItems) == activeItems &&
                    (order.getOrderStatus() == OrderStatus.PAYMENT_RECEIVED || order.getOrderStatus() == OrderStatus.PROCESSING_BY_SELLERS)) {
                newOverallStatus = OrderStatus.PROCESSING_BY_SELLERS;
            } else if (pendingConfirmationItems > 0 &&
                    (order.getOrderStatus() == OrderStatus.PAYMENT_RECEIVED || order.getOrderStatus() == OrderStatus.PROCESSING_BY_SELLERS)) {
                if (confirmedItems > 0 || awaitingShipmentItems > 0 || shippedItems > 0 || deliveredItems > 0) {
                    newOverallStatus = OrderStatus.PROCESSING_BY_SELLERS;
                } else {
                    newOverallStatus = OrderStatus.PAYMENT_RECEIVED;
                }
            }
        }

        if (newOverallStatus != currentOverallStatus) {
            order.setOrderStatus(newOverallStatus);
            orderRepository.save(order);
            logger.info("Order ID {} overall status automatically updated from {} to {}", order.getId(), currentOverallStatus, newOverallStatus);

            // MODIFICATION: Logic to publish event on DELIVERED status
            if (newOverallStatus == OrderStatus.DELIVERED) {
                eventPublisher.publishEvent(new OrderDeliveredEvent(this, order));
                notificationService.sendOrderDeliveredToCustomer(order);
            } else if (newOverallStatus == OrderStatus.SHIPPED) {
                notificationService.sendOrderFullyShippedToCustomer(order);
            } else if (newOverallStatus == OrderStatus.CANCELLED_BY_SELLER || newOverallStatus == OrderStatus.CANCELLED_BY_OPERATOR) {
                notificationService.sendOrderStatusUpdateToCustomer(order, "The overall status of your order has been updated due to cancellation.");
            }
            else {
                notificationService.sendOrderStatusUpdateToCustomer(order, "The overall status of your order has been updated.");
            }
        }
    }
}
