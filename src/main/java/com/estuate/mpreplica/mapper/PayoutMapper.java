package com.estuate.mpreplica.mapper;

import com.estuate.mpreplica.dto.PayoutDto;
import com.estuate.mpreplica.entity.Payout;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayoutMapper {
    @Mapping(source = "sellerProfile.id", target = "sellerProfileId")
    @Mapping(source = "sellerProfile.name", target = "sellerName")
    PayoutDto toDto(Payout payout);
}

