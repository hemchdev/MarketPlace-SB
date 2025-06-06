package com.estuate.mpreplica.controller;

import com.estuate.mpreplica.dto.MessageResponseDto;
import com.estuate.mpreplica.dto.OrderCreateDto;
import com.estuate.mpreplica.dto.OrderResponseDto;
import com.estuate.mpreplica.exception.InsufficientStockException;
import com.estuate.mpreplica.exception.InvalidOperationException;
import com.estuate.mpreplica.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderCreateDto orderCreateDto) {
        logger.info("Seller creating order for customer: {}", orderCreateDto.getCustomerEmail());
        try {
            OrderResponseDto createdOrder = orderService.createOrder(orderCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (InsufficientStockException | InvalidOperationException | IllegalArgumentException e) {
            logger.warn("Order creation by seller failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        } catch (AccessDeniedException e) {
            logger.warn("Order creation access denied for seller: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDto(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during order creation by seller for customer {}: {}", orderCreateDto.getCustomerEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDto("An unexpected error occurred while creating the order. Please try again."));
        }
    }
}