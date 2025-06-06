package com.estuate.mpreplica.service;

import com.estuate.mpreplica.dto.*;
import com.estuate.mpreplica.entity.Product;
import com.estuate.mpreplica.entity.SellerProductAssignment;
import com.estuate.mpreplica.entity.SellerProfile;
import com.estuate.mpreplica.enums.SellerOverallStatus;
import com.estuate.mpreplica.exception.ResourceNotFoundException;
import com.estuate.mpreplica.mapper.SellerProductAssignmentMapper;
import com.estuate.mpreplica.repository.ProductRepository;
import com.estuate.mpreplica.repository.SellerProductAssignmentRepository;
import com.estuate.mpreplica.repository.SellerProfileRepository;
import com.estuate.mpreplica.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SellerProductAssignmentService {
    private static final Logger logger = LoggerFactory.getLogger(SellerProductAssignmentService.class);

    @Autowired
    private SellerProductAssignmentRepository assignmentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private SellerProfileService sellerProfileService;

    @Autowired
    @Qualifier("sellerProductAssignmentMapperImpl")
    private SellerProductAssignmentMapper assignmentMapper;

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        }
        throw new IllegalStateException("User ID could not be determined from security context.");
    }

    @Transactional
    public SellerProductAssignmentDto assignProductToSeller(Long sellerProfileId, SellerProductAssignmentCreateDto createDto) {
        Product product = productRepository.findById(createDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", createDto.getProductId()));

        SellerProfile sellerProfile = sellerProfileService.getSellerProfileEntityById(sellerProfileId);

        if (sellerProfile.getOverallStatus() != SellerOverallStatus.ACTIVE) {
            throw new IllegalStateException("Cannot assign products to a seller who is not ACTIVE. Current status: " + sellerProfile.getOverallStatus());
        }

        if (assignmentRepository.existsByProductIdAndSellerProfileId(product.getId(), sellerProfile.getId())) {
            throw new IllegalArgumentException("Product ID " + product.getId() + " is already assigned to seller ID " + sellerProfile.getId());
        }

        SellerProductAssignment assignment = new SellerProductAssignment();
        assignment.setProduct(product);
        assignment.setSellerProfile(sellerProfile);
        assignment.setStockQuantity(createDto.getInitialStock());
        assignment.setSellableBySeller(true);
        assignment.setAssignedByOperatorId(getCurrentUserId());

        SellerProductAssignment savedAssignment = assignmentRepository.save(assignment);
        logger.info("Product ID {} assigned to seller profile ID {} by operator ID {}. Assignment ID: {}",
                product.getId(), sellerProfile.getId(), getCurrentUserId(), savedAssignment.getId());
        return assignmentMapper.toDto(savedAssignment);
    }

    public List<SellerProductAssignmentDto> getAssignmentsForSeller(Long sellerProfileId) {
        if (!sellerProfileRepository.existsById(sellerProfileId)) {
            throw new ResourceNotFoundException("SellerProfile", "id", sellerProfileId);
        }
        List<SellerProductAssignment> assignments = assignmentRepository.findBySellerProfileIdWithDetails(sellerProfileId);
        return assignments.stream().map(assignmentMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public SellerProductAssignmentDto updateAssignmentByOperator(Long sellerProfileId, Long assignmentId, SellerProductAssignmentUpdateDto updateDto) {
        SellerProductAssignment assignment = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerProductAssignment", "id", assignmentId));

        if (!assignment.getSellerProfile().getId().equals(sellerProfileId)) {
            throw new IllegalArgumentException("Assignment ID " + assignmentId + " does not belong to seller profile ID " + sellerProfileId);
        }

        boolean updated = false;

        if (updateDto.getStockQuantity() != null) {
            assignment.setStockQuantity(updateDto.getStockQuantity());
            updated = true;
        }
        if (updateDto.getIsSellableBySeller() != null) {
            assignment.setSellableBySeller(updateDto.getIsSellableBySeller());
            updated = true;
        }

        if (updated) {
            SellerProductAssignment savedAssignment = assignmentRepository.save(assignment);
            logger.info("Operator ID {} updated assignment ID {}. Stock: {}, Sellable: {}",
                    getCurrentUserId(), assignmentId, savedAssignment.getStockQuantity(), savedAssignment.isSellableBySeller());
            return assignmentMapper.toDto(savedAssignment);
        }
        return assignmentMapper.toDto(assignment);
    }


    public List<SellerProductAssignmentDto> getMyAssignedProducts() {
        Long currentUserId = getCurrentUserId();
        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerProfile", "userId", currentUserId + " (No profile found for current seller)"));

        List<SellerProductAssignment> assignments = assignmentRepository.findBySellerProfileIdWithDetails(sellerProfile.getId());
        return assignments.stream().map(assignmentMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public SellerProductAssignmentDto updateMyStock(Long assignmentId, SellerStockUpdateDto stockUpdateDto) {
        Long currentUserId = getCurrentUserId();
        SellerProductAssignment assignment = assignmentRepository.findByIdAndSellerUserIdWithDetails(assignmentId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerProductAssignment", "id", assignmentId + " (or not owned by seller)"));

        if (assignment.getSellerProfile().getOverallStatus() != SellerOverallStatus.ACTIVE) {
            throw new IllegalStateException("Cannot update stock. Seller account is not ACTIVE.");
        }

        assignment.setStockQuantity(stockUpdateDto.getStockQuantity());
        SellerProductAssignment savedAssignment = assignmentRepository.save(assignment);
        logger.info("Seller (User ID {}) updated stock for assignment ID {} to {}", currentUserId, assignmentId, savedAssignment.getStockQuantity());
        return assignmentMapper.toDto(savedAssignment);
    }

    @Transactional
    public SellerProductAssignmentDto toggleMyProductSellable(Long assignmentId, SellerProductToggleSellableDto toggleDto) {
        Long currentUserId = getCurrentUserId();
        SellerProductAssignment assignment = assignmentRepository.findByIdAndSellerUserIdWithDetails(assignmentId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerProductAssignment", "id", assignmentId + " (or not owned by seller)"));

        if (assignment.getSellerProfile().getOverallStatus() != SellerOverallStatus.ACTIVE) {
            throw new IllegalStateException("Cannot update sellable status. Seller account is not ACTIVE.");
        }

        assignment.setSellableBySeller(toggleDto.getIsSellableBySeller());
        SellerProductAssignment savedAssignment = assignmentRepository.save(assignment);
        logger.info("Seller (User ID {}) toggled sellable status for assignment ID {} to {}", currentUserId, assignmentId, savedAssignment.isSellableBySeller());
        return assignmentMapper.toDto(savedAssignment);
    }
}