package com.riaydev.bankingapp.Exceptions;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ErrorResponse {
    private int statusCode;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(int statusCode, String message, LocalDateTime timestamp) {
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = timestamp;
    }

    
}

