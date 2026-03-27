package com.bank.audit;

import com.bank.exception.AuditLoggingException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditLoggingAspectTest {
    @Test
    void testExceptionIsLoggedAndRethrownWithContext() throws Throwable {
        // Arrange
        AuditLoggingAspect aspect = new AuditLoggingAspect();
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        MethodSignature sig = mock(MethodSignature.class);
        // Use the real annotated method
        java.lang.reflect.Method realMethod = this.getClass().getDeclaredMethod("dummyMethod");
        when(pjp.getSignature()).thenReturn(sig);
        when(sig.getMethod()).thenReturn(realMethod);
        when(sig.getDeclaringType()).thenReturn(this.getClass());
        when(sig.getName()).thenReturn("dummyMethod");
        when(pjp.getArgs()).thenReturn(new Object[] {"testArg"});
        // Simulate exception thrown by proceed
        doThrow(new IllegalArgumentException("fail"))
                .when(pjp).proceed();

        // Act & Assert
        AuditLoggingException ex = assertThrows(AuditLoggingException.class, () -> aspect.around(pjp));
        assertTrue(ex.getMessage().contains("Audit failure"));
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    }

    // Dummy method for reflection
    // This method is intentionally left blank for use as a reflection target in tests.
    // It is not meant to be called directly.
    @Auditable(action = "TEST_ACTION")
    public void dummyMethod() {
        // No implementation needed. Used only for reflection in tests.
        // SonarQube: This method is intentionally empty.
    }

    @Test
    void resolvePrincipal_returnsAnonymousWhenAuthNullOrNotAuthenticated() {
        AuditLoggingAspect aspect = new AuditLoggingAspect();
        // Mock SecurityContextHolder to return null authentication
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(null);
        assertEquals("anonymous", aspect.resolvePrincipal());
        // Mock authentication not authenticated
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        assertEquals("anonymous", aspect.resolvePrincipal());
    }

    @Test
    void resolvePrincipal_returnsNameWhenAuthenticated() {
        AuditLoggingAspect aspect = new AuditLoggingAspect();
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user1");
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        assertEquals("user1", aspect.resolvePrincipal());
    }

    @Test
    void safeArgs_handlesNullArrayAndNullElementAndPasswordRedaction() {
        AuditLoggingAspect aspect = new AuditLoggingAspect();
        // args == null
        assertEquals("[]", aspect.safeArgs(null));
        // args contains null
        assertTrue(aspect.safeArgs(new Object[]{null, "abc"}).contains("null"));
        // args contains password string
        assertTrue(aspect.safeArgs(new Object[]{"myPassword123"}).contains("[REDACTED]"));
    }
}
