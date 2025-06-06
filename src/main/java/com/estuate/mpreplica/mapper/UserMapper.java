package com.estuate.mpreplica.mapper;


import com.estuate.mpreplica.dto.UserRegistrationDto;
import com.estuate.mpreplica.dto.UserResponseDto;
import com.estuate.mpreplica.entity.Role;
import com.estuate.mpreplica.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User userRegistrationDtoToUser(UserRegistrationDto dto);
    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesSetToStringSet")
    UserResponseDto userToUserResponseDto(User user);
    @Named("rolesSetToStringSet")
    default Set<String> rolesSetToStringSet(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream().map(role -> role.getName().name()).collect(Collectors.toSet());
    }
}


