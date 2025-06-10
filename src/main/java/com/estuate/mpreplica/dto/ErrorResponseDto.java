package com.estuate.mpreplica.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * A standardized DTO for returning API error responses. This provides a consistent
 * structure for all client-facing errors, including validation failures.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't include null fields in the JSON output
public class ErrorResponseDto {

    private LocalDateTime timestamp;
    private int status;
    private String error; // e.g., "Not Found", "Bad Request"
    private String message;
    private String path;

    // Used specifically for validation errors to show issues with specific fields
    private Map<String, String> fieldErrors;

    public ErrorResponseDto(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
