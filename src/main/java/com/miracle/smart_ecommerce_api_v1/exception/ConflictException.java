package com.miracle.smart_ecommerce_api_v1.exception;

/**
 * Thrown when a conflict occurs (e.g., duplicate resource).
 */
public class ConflictException extends RuntimeException {
    public ConflictException() { super(); }
    public ConflictException(String message) { super(message); }
    public ConflictException(String message, Throwable cause) { super(message, cause); }
}

