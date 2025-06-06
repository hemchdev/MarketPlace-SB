package com.estuate.mpreplica.service;

import com.estuate.mpreplica.dto.ProductCreateDto;
import com.estuate.mpreplica.dto.ProductDto;
import com.estuate.mpreplica.entity.Product;
import com.estuate.mpreplica.exception.ResourceNotFoundException;
import com.estuate.mpreplica.mapper.ProductMapper;
import com.estuate.mpreplica.repository.ProductRepository;
import com.estuate.mpreplica.repository.UserRepository;
import com.estuate.mpreplica.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository; // Optional: if you need to fetch operator User entity

    @Autowired
    private ProductMapper productMapper;

    private UserDetailsImpl getAuthenticatedUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        throw new IllegalStateException("Operator details cannot be determined. User not authenticated or principal is not UserDetailsImpl.");
    }

    private Long getCurrentOperatorId() {
        return getAuthenticatedUserDetails().getId();
    }

    private String getCurrentOperatorUsername() {
        return getAuthenticatedUserDetails().getUsername();
    }

    @Transactional
    public ProductDto createProduct(ProductCreateDto dto) {
        if (productRepository.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + dto.getSku() + "' already exists.");
        }

        Product product = productMapper.productCreateDtoToProduct(dto);
        Long operatorId = getCurrentOperatorId();
        String operatorUsername = getCurrentOperatorUsername();

        product.setCreatedByOperatorId(operatorId);
        product.setUpdatedByOperatorId(operatorId);
        // product.setCreatedByUsername(operatorUsername); // If you add these fields to Product entity
        // product.setUpdatedByUsername(operatorUsername);

        Product savedProduct = productRepository.save(product);
        logger.info("Product created - ID: {}, SKU: {} by Operator: {} (ID: {})",
                savedProduct.getId(), savedProduct.getSku(), operatorUsername, operatorId);
        return productMapper.productToProductDto(savedProduct);
    }

    public ProductDto getProductById(Long id) {
        Product product = getProductEntityById(id);
        return productMapper.productToProductDto(product);
    }

    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    public List<ProductDto> getAllProducts() {
        // Consider pagination for large datasets
        return productRepository.findAll().stream()
                .map(productMapper::productToProductDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDto updateProduct(Long productId, ProductCreateDto dto) {
        Product product = getProductEntityById(productId);
        Long operatorId = getCurrentOperatorId();
        String operatorUsername = getCurrentOperatorUsername();

        // Check if SKU is being changed and if the new SKU already exists for another product
        if (!product.getSku().equalsIgnoreCase(dto.getSku()) && productRepository.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("Cannot update SKU to '" + dto.getSku() + "' as it is already in use by another product.");
        }

        productMapper.updateProductFromDto(dto, product); // Update fields from DTO
        product.setUpdatedByOperatorId(operatorId);
        // product.setUpdatedByUsername(operatorUsername);

        Product updatedProduct = productRepository.save(product);
        logger.info("Product updated - ID: {}, SKU: {} by Operator: {} (ID: {})",
                updatedProduct.getId(), updatedProduct.getSku(), operatorUsername, operatorId);
        return productMapper.productToProductDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductEntityById(id); // Ensures product exists before attempting delete
        Long operatorId = getCurrentOperatorId();
        String operatorUsername = getCurrentOperatorUsername();

        // Add checks here if product is part of any assignments or orders before deletion
        // e.g., if (assignmentRepository.existsByProductId(id)) throw new IllegalStateException("Cannot delete product with active assignments");

        productRepository.delete(product);
        logger.info("Product deleted - ID: {} by Operator: {} (ID: {})",
                id, operatorUsername, operatorId);
    }
}
