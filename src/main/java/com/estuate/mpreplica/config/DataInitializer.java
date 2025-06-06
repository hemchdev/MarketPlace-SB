package com.estuate.mpreplica.config;

import com.estuate.mpreplica.dto.UserRegistrationDto;
import com.estuate.mpreplica.entity.Role;
import com.estuate.mpreplica.enums.RoleName;
import com.estuate.mpreplica.repository.RoleRepository;
import com.estuate.mpreplica.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    // If you want to create an initial operator, you can autowire UserService and PasswordEncoder
     @Autowired
     private UserService userService;
     @Autowired
     private PasswordEncoder passwordEncoder;
     @Value("${initial.operator.username}")
     private String initialOperatorUsername;
     @Value("${initial.operator.email}")
     private String initialOperatorEmail;
     @Value("${initial.operator.password}")
     private String initialOperatorPassword;


    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing roles...");
        initializeRole(RoleName.OPERATOR);
        initializeRole(RoleName.SELLER);
        initializeRole(RoleName.CUSTOMER); // If you plan to use it
        logger.info("Roles initialized.");

        // Example of creating an initial operator user if not exists
//         This is often better handled by a dedicated setup script or manual process for security.
         if (!userService.findByUsername(initialOperatorUsername).isPresent()) {
             logger.info("Creating initial operator user: {}", initialOperatorUsername);
             UserRegistrationDto operatorDto = new UserRegistrationDto();
             operatorDto.setUsername(initialOperatorUsername);
             operatorDto.setEmail(initialOperatorEmail);
             operatorDto.setPassword(initialOperatorPassword); // Password will be encoded by UserService
             Set<RoleName> roles = new HashSet<>();
             roles.add(RoleName.OPERATOR);
             operatorDto.setRoles(roles);
             try {
                 userService.createUser(operatorDto, roles);
                 logger.info("Initial operator user {} created successfully.", initialOperatorUsername);
             } catch (Exception e) {
                 logger.error("Failed to create initial operator user {}: {}", initialOperatorUsername, e.getMessage());
             }
         }
    }

    private void initializeRole(RoleName roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role(roleName);
            roleRepository.save(role);
            logger.info("Created role: {}", roleName);
        }
    }
}