package com.estuate.mpreplica.mapper;

import com.estuate.mpreplica.dto.SellerProductAssignmentDto;
import com.estuate.mpreplica.entity.SellerProductAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SellerProductAssignmentMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "product.basePrice", target = "productBasePrice")
    @Mapping(source = "sellerProfile.id", target = "sellerProfileId")
    @Mapping(source = "sellerProfile.name", target = "sellerName")
    SellerProductAssignmentDto toDto(SellerProductAssignment assignment);
}



