package com.bank.exception;

public class AuditLoggingException extends RuntimeException {
    public AuditLoggingException(String message, Throwable cause) {
        super(message, cause);
    }
}

