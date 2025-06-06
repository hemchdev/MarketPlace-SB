package com.estuate.mpreplica.mapper;

import com.estuate.mpreplica.dto.SellerLedgerEntryDto;
import com.estuate.mpreplica.entity.SellerLedgerEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SellerLedgerEntryMapper {
    SellerLedgerEntryMapper INSTANCE = Mappers.getMapper(SellerLedgerEntryMapper.class);
    @Mapping(source = "sellerProfile.id", target = "sellerProfileId")
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "payout.id", target = "payoutId")
    SellerLedgerEntryDto toDto(SellerLedgerEntry entry);
}

