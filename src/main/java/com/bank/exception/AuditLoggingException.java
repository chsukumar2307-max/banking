package com.bank.exception;


public class AuditLoggingException extends RuntimeException {
    public AuditLoggingException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuditLoggingException(String message) {
        super(message);
    }

    public AuditLoggingException(Throwable cause) {
        super(cause);
    }
}
