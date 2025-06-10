package com.estuate.mpreplica.mapper;

import com.estuate.mpreplica.dto.SellerPayoutProfileDto;
import com.estuate.mpreplica.entity.SellerPayoutProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for converting between SellerPayoutProfile entity and its DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SellerPayoutProfileMapper {

    @Mapping(source = "sellerProfile.id", target = "sellerProfileId")
    SellerPayoutProfileDto toDto(SellerPayoutProfile entity);
}
