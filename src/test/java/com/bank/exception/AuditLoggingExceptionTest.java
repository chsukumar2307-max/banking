package com.bank.exception;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuditLoggingExceptionTest {
    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Test message";
        Throwable cause = new RuntimeException("Cause");
        AuditLoggingException ex = new AuditLoggingException(message, cause);
        assertEquals(message, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testConstructorWithMessageOnly() {
        String message = "Only message";
        AuditLoggingException ex = new AuditLoggingException(message);
        assertEquals(message, ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testConstructorWithCauseOnly() {
        Throwable cause = new RuntimeException("Only cause");
        AuditLoggingException ex = new AuditLoggingException(cause);
        assertEquals(cause, ex.getCause());
        assertEquals("java.lang.RuntimeException: Only cause", ex.getMessage());
    }
}
