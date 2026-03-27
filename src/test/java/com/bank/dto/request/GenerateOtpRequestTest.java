package com.bank.dto.request;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GenerateOtpRequestTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validRequest_passesValidation() {
        GenerateOtpRequest req = new GenerateOtpRequest();
        req.setUsername("user1");
        req.setMobileNumber("+123456789012");
        Set<ConstraintViolation<GenerateOtpRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void blankUsername_failsValidation() {
        GenerateOtpRequest req = new GenerateOtpRequest();
        req.setUsername("   "); // Use whitespace to test @NotBlank
        req.setMobileNumber("+123456789012");
        Set<ConstraintViolation<GenerateOtpRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "Violations should not be empty for blank username");
        assertTrue(violations.stream().anyMatch(v ->
            "username".equals(v.getPropertyPath().toString()) &&
            v.getMessage().toLowerCase().contains("blank")
        ), "Username should fail @NotBlank validation");
    }

    @Test
    void blankMobileNumber_failsValidation() {
        GenerateOtpRequest req = new GenerateOtpRequest();
        req.setUsername("user1");
        req.setMobileNumber("");
        Set<ConstraintViolation<GenerateOtpRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "Violations should not be empty for blank mobile number");
        assertTrue(violations.stream().anyMatch(v -> "mobileNumber".equals(v.getPropertyPath().toString())), "Mobile number should fail @NotBlank validation");
    }

    @Test
    void invalidMobileNumberPattern_failsValidation() {
        GenerateOtpRequest req = new GenerateOtpRequest();
        req.setUsername("user1");
        req.setMobileNumber("123456789012"); // missing +
        Set<ConstraintViolation<GenerateOtpRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "Violations should not be empty for invalid mobile number pattern");
        assertTrue(violations.stream().anyMatch(v -> "mobileNumber".equals(v.getPropertyPath().toString())), "Mobile number should fail @Pattern validation");
    }

    @Test
    void equalsAndHashCode() {
        GenerateOtpRequest req1 = new GenerateOtpRequest();
        req1.setUsername("user1");
        req1.setMobileNumber("+123456789012");
        GenerateOtpRequest req2 = new GenerateOtpRequest();
        req2.setUsername("user1");
        req2.setMobileNumber("+123456789012");
        assertEquals(req1, req2, "Objects with same values should be equal");
        assertEquals(req1.hashCode(), req2.hashCode(), "Hash codes should match for equal objects");
    }

    @Test
    void toStringWorks() {
        GenerateOtpRequest req = new GenerateOtpRequest();
        req.setUsername("user1");
        req.setMobileNumber("+123456789012");
        String str = req.toString();
        assertAll(
            () -> assertTrue(str.contains("user1"), "toString should contain username"),
            () -> assertTrue(str.contains("+123456789012"), "toString should contain mobile number")
        );
    }
}
