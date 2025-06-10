package com.estuate.mpreplica.mapper;

import com.estuate.mpreplica.dto.SellerLedgerEntryDto;
import com.estuate.mpreplica.entity.SellerLedgerEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps the SellerLedgerEntry entity to its DTO.
 * This version relies exclusively on Spring for dependency injection.
 */
@Mapper(componentModel = "spring")
public interface SellerLedgerEntryMapper {


    @Mapping(source = "sellerProfile.id", target = "sellerProfileId")
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "payout.id", target = "payoutId")
    SellerLedgerEntryDto toDto(SellerLedgerEntry entry);
}
