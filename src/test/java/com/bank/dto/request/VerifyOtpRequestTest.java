package com.bank.dto.request;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VerifyOtpRequestTest {
    private final Validator validator;

    public VerifyOtpRequestTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequest_passesValidation() {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setUsername("user1");
        req.setMobileNumber("+123456789012");
        req.setOtp("123456");
        req.setPassword("pass");
        Set<ConstraintViolation<VerifyOtpRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankFields_failValidation() {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setUsername("");
        req.setMobileNumber("");
        req.setOtp("");
        req.setPassword("");
        Set<ConstraintViolation<VerifyOtpRequest>> violations = validator.validate(req);
        assertEquals(4, violations.size());
    }

    @Test
    void testLombokMethods() {
        VerifyOtpRequest req1 = new VerifyOtpRequest();
        req1.setUsername("user1");
        req1.setMobileNumber("+123456789012");
        req1.setOtp("123456");
        req1.setPassword("pass");
        VerifyOtpRequest req2 = new VerifyOtpRequest();
        req2.setUsername("user1");
        req2.setMobileNumber("+123456789012");
        req2.setOtp("123456");
        req2.setPassword("pass");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        assertTrue(req1.toString().contains("user1"));
    }
}

