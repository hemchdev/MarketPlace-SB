package com.estuate.mpreplica.mapper;

import com.estuate.mpreplica.dto.OrderItemResponseDto;
import com.estuate.mpreplica.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "sellerProductAssignment.id", target = "sellerProductAssignmentId")
    @Mapping(source = "sellerProductAssignment.product.name", target = "productName")
    @Mapping(source = "sellerProductAssignment.product.sku", target = "productSku")
    @Mapping(source = "sellerProductAssignment.sellerProfile.name", target = "sellerName")
    OrderItemResponseDto toDto(OrderItem orderItem);
}


