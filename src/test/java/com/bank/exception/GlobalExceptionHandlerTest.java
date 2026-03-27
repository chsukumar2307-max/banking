package com.bank.exception;
import com.bank.dto.response.ApiErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.servlet.http.HttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test-uri");
    }

    @Test
    void handleAccountNotFound() {
        AccountNotFoundException ex = new AccountNotFoundException("Account not found");
        ResponseEntity<ApiErrorResponse> response = handler.handleAccountNotFound(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Account not found", response.getBody().getMessage());
        assertEquals("/test-uri", response.getBody().getPath());
    }

    @Test
    void handleInsufficient() {
        InsufficientBalanceException ex = new InsufficientBalanceException("Insufficient balance");
        ResponseEntity<ApiErrorResponse> response = handler.handleInsufficient(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Insufficient balance", response.getBody().getMessage());
    }

    @Test
    void handleInvalidOtp() {
        InvalidOtpException ex = new InvalidOtpException("Invalid OTP");
        ResponseEntity<ApiErrorResponse> response = handler.handleInvalidOtp(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid OTP", response.getBody().getMessage());
    }

    @Test
    void handleUnauthorized() {
        UnauthorizedAccessException ex = new UnauthorizedAccessException("Unauthorized");
        ResponseEntity<ApiErrorResponse> response = handler.handleUnauthorized(ex, request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody().getMessage());
    }

    @Test
    void handleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        org.springframework.validation.FieldError fieldError = new org.springframework.validation.FieldError("objectName", "field", "must not be blank");
        java.util.List<org.springframework.validation.FieldError> fieldErrors = java.util.Collections.singletonList(fieldError);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("field"));
        assertEquals("must not be blank", response.getBody().get("field"));
    }

    @Test
    void handleGeneric() {
        Exception ex = new Exception("Generic error");
        ResponseEntity<ApiErrorResponse> response = handler.handleGeneric(ex, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        // The handler always returns 'Internal server error' as the message
        assertEquals("Internal server error", response.getBody().getMessage());
    }
}
