package com.estuate.mpreplica.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.hibernate.StaleObjectStateException; // For optimistic locking outside of @Retryable
import org.springframework.dao.OptimisticLockingFailureException; // Spring's wrapper
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        logger.warn("ResourceNotFoundException: {} for request: {}", ex.getMessage(), request.getDescription(false));
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.warn("IllegalArgumentException: {} for request: {}", ex.getMessage(), request.getDescription(false));
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDetails> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        logger.warn("IllegalStateException: {} for request: {}", ex.getMessage(), request.getDescription(false));
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT); // 409 Conflict
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        logger.warn("AccessDeniedException: {} for request: {}", ex.getMessage(), request.getDescription(false));
        ErrorDetails errorDetails = new ErrorDetails(new Date(), "Access Denied: You do not have permission to perform this action.", request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        List<String> globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());

        Map<String, Object> errorsBody = new HashMap<>();
        errorsBody.put("timestamp", new Date());
        errorsBody.put("status", HttpStatus.BAD_REQUEST.value());
        errorsBody.put("error", "Validation Failed");
        if (!fieldErrors.isEmpty()) {
            errorsBody.put("fieldErrors", fieldErrors);
        }
        if (!globalErrors.isEmpty()) {
            errorsBody.put("globalErrors", globalErrors);
        }
        errorsBody.put("path", request.getDescription(false));

        logger.warn("Validation error: {} for request: {}", errorsBody, request.getDescription(false));
        return new ResponseEntity<>(errorsBody, HttpStatus.BAD_REQUEST);
    }

    // Module 4 Custom Exception Handlers
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorDetails> handleInsufficientStockException(InsufficientStockException ex, WebRequest request) {
        logger.warn("InsufficientStockException: {} for request: {}", ex.getMessage(), request.getDescription(false));
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT); // HTTP 409
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorDetails> handleInvalidOperationException(InvalidOperationException ex, WebRequest request) {
        logger.warn("InvalidOperationException: {} for request: {}", ex.getMessage(), request.getDescription(false));
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST); // HTTP 400
    }

    // Optimistic Locking Exception Handler (if not caught by @Retryable or needs specific response)
    @ExceptionHandler({OptimisticLockingFailureException.class, StaleObjectStateException.class})
    public ResponseEntity<ErrorDetails> handleOptimisticLockException(Exception ex, WebRequest request) {
        logger.warn("OptimisticLockingFailureException: {} for request: {}. This might indicate concurrent modification.", ex.getMessage(), request.getDescription(false));
        ErrorDetails errorDetails = new ErrorDetails(new Date(), "The data was modified by another transaction. Please try again.", request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT); // HTTP 409
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unhandled Exception for request {}: {}", request.getDescription(false), ex.getMessage(), ex);
        ErrorDetails errorDetails = new ErrorDetails(new Date(), "An unexpected error occurred. Please try again later or contact support.", request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Simple ErrorDetails DTO (already defined)
    public static class ErrorDetails {
        private Date timestamp;
        private String message;
        private String details;

        public ErrorDetails(Date timestamp, String message, String details) {
            this.timestamp = timestamp;
            this.message = message;
            this.details = details;
        }
        public Date getTimestamp() { return timestamp; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
    }
}
