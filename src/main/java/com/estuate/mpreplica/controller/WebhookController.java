package com.estuate.mpreplica.controller;

import com.estuate.mpreplica.dto.IdMeWebhookPayload;
import com.estuate.mpreplica.dto.LmsWebhookPayload;
import com.estuate.mpreplica.dto.MessageResponseDto;
import com.estuate.mpreplica.exception.ResourceNotFoundException;
import com.estuate.mpreplica.service.SellerProfileService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private SellerProfileService sellerProfileService;

    @Value("${idme.webhook.secret}")
    private String idmeWebhookSecret;

    @Value("${lms.webhook.secret}")
    private String lmsWebhookSecret;

    private static final String WEBHOOK_SECRET_HEADER = "X-Webhook-Secret";

    @PostMapping("/idme/status")
    public ResponseEntity<?> handleIdMeStatusUpdate(@RequestHeader(WEBHOOK_SECRET_HEADER) String secret,
                                                    @Valid @RequestBody IdMeWebhookPayload payload) {
        if (!idmeWebhookSecret.equals(secret)) {
            logger.warn("Unauthorized ID.me webhook attempt. Invalid secret provided.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDto("Error: Invalid webhook secret."));
        }
        try {
            sellerProfileService.handleIdMeStatusUpdate(payload);
            return ResponseEntity.ok(new MessageResponseDto("ID.me status update received and processed."));
        } catch (Exception e) {
            logger.error("Error processing ID.me webhook for sellerProfileId {}: {}", payload.getSellerProfileId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDto("Internal server error processing ID.me webhook."));
        }
    }

    @PostMapping("/lms/completion")
    public ResponseEntity<?> handleLmsCompletionUpdate(@RequestHeader(WEBHOOK_SECRET_HEADER) String secret,
                                                       @Valid @RequestBody LmsWebhookPayload payload) {
        if (!lmsWebhookSecret.equals(secret)) {
            logger.warn("Unauthorized LMS webhook attempt. Invalid secret provided.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDto("Error: Invalid webhook secret."));
        }
        try {
            sellerProfileService.handleLmsCompletionUpdate(payload);
            return ResponseEntity.ok(new MessageResponseDto("LMS completion update received and processed."));
        } catch (Exception e) {
            logger.error("Error processing LMS webhook for sellerProfileId {}: {}", payload.getSellerProfileId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDto("Internal server error processing LMS webhook."));
        }
    }
}