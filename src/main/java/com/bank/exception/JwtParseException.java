package com.bank.exception;

/**
 * Custom exception thrown when JWT parsing fails.
 */
public class JwtParseException extends RuntimeException {
    public JwtParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

