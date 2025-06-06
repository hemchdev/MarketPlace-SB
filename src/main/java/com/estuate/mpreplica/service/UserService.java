package com.estuate.mpreplica.service;


import com.estuate.mpreplica.dto.UserRegistrationDto;
import com.estuate.mpreplica.dto.UserResponseDto;
import com.estuate.mpreplica.entity.Role;
import com.estuate.mpreplica.entity.User;
import com.estuate.mpreplica.enums.RoleName;
import com.estuate.mpreplica.exception.ResourceNotFoundException;
import com.estuate.mpreplica.mapper.UserMapper;
import com.estuate.mpreplica.repository.RoleRepository;
import com.estuate.mpreplica.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public UserResponseDto registerUser(UserRegistrationDto dto, RoleName defaultRole) {
        Set<RoleName> roleNames = new HashSet<>();
        if (dto.getRoles() == null || dto.getRoles().isEmpty()) {
            roleNames.add(defaultRole);
        } else {
            roleNames.addAll(dto.getRoles());
        }
        return createUser(dto, roleNames);
    }

    @Transactional
    public UserResponseDto createUser(UserRegistrationDto dto, Set<RoleName> roleNames) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered.");
        }

        User user = userMapper.userRegistrationDtoToUser(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
            throw new IllegalArgumentException("At least one role must be specified for the user.");
        }

        for (RoleName roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role '" + roleName + "' not found in database."));
            roles.add(role);
        }
        user.setRoles(roles);
        user.setActive(true); // New users are active by default

        User savedUser = userRepository.save(user);
        return userMapper.userToUserResponseDto(savedUser);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.userToUserResponseDto(user);
    }
}
