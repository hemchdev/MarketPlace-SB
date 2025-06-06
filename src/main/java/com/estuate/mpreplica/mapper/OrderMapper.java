package com.estuate.mpreplica.mapper;

import com.estuate.mpreplica.dto.OrderResponseDto;
import com.estuate.mpreplica.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    @Mapping(source = "createdBySellerProfile.id", target = "createdBySellerProfileId")
    @Mapping(source = "createdBySellerProfile.name", target = "createdBySellerName")
    OrderResponseDto toDto(Order order);
}



