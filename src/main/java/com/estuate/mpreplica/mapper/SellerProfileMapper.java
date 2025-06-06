package com.estuate.mpreplica.mapper;

import com.estuate.mpreplica.dto.SellerProfileDto;
import com.estuate.mpreplica.entity.SellerProfile;
import com.estuate.mpreplica.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SellerProfileMapper {
    @Mapping(source = "user", target = "userId", qualifiedByName = "userToUserId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.email", target = "userEmail")
    SellerProfileDto toDto(SellerProfile sellerProfile);
    @Named("userToUserId")
    default Long userToUserId(User user) {
        return user != null ? user.getId() : null;
    }
}

