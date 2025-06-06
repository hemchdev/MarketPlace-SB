package com.estuate.mpreplica.controller;

import com.estuate.mpreplica.dto.*;
import com.estuate.mpreplica.enums.RoleName;
import com.estuate.mpreplica.security.UserDetailsImpl;
import com.estuate.mpreplica.security.jwt.JwtUtils;
import com.estuate.mpreplica.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponseDto(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/register/operator")
    public ResponseEntity<?> registerOperator(@Valid @RequestBody UserRegistrationDto registrationDto) {
        // Ensure this registration is specifically for an OPERATOR
        if (registrationDto.getRoles() == null || !registrationDto.getRoles().contains(RoleName.OPERATOR)) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("Error: Registration must specify OPERATOR role for this endpoint."));
        }
        // Ensure only OPERATOR role is provided
        if (registrationDto.getRoles().size() > 1 || !registrationDto.getRoles().equals(Set.of(RoleName.OPERATOR))) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("Error: Operator registration can only have the OPERATOR role."));
        }

        try {
            // The userService.createUser will handle validation for existing username/email
            userService.createUser(registrationDto, Set.of(RoleName.OPERATOR));
            return ResponseEntity.ok(new MessageResponseDto("Operator registered successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }
}