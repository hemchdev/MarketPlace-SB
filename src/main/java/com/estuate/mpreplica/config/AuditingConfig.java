package com.estuate.mpreplica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                // For system-initiated actions like webhooks, you might want a specific identifier
                if (requestIsWebhook()) { // Hypothetical method to check if current context is a webhook
                    return Optional.of("WEBHOOK_SYSTEM");
                }
                return Optional.of("SYSTEM");
            }
            if (authentication.getPrincipal() instanceof UserDetails) {
                return Optional.of(((UserDetails) authentication.getPrincipal()).getUsername());
            }
            return Optional.of(authentication.getPrincipal().toString());
        };
    }

    // This is a placeholder. In a real app, you'd have a more robust way to determine this.
    // For example, by checking a specific attribute set on the request by a webhook filter.
    private boolean requestIsWebhook() {
        // Some logic to determine if the current request originated from a webhook
        // For example, check a request attribute or a specific path pattern if not handled by SecurityContext
        return false; // Default to false
    }
}