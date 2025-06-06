package com.estuate.mpreplica.dto;

import com.estuate.mpreplica.enums.RoleName;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Set;

@Data
public class UserRegistrationDto {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    private Set<RoleName> roles;
}
