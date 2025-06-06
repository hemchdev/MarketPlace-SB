package com.estuate.mpreplica.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

