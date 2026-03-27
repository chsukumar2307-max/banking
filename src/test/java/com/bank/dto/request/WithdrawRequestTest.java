package com.bank.dto.request;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WithdrawRequestTest {
    private final Validator validator;

    public WithdrawRequestTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validRequest_passesValidation() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountNumber("ACC123");
        req.setAmount(BigDecimal.TEN);
        Set<ConstraintViolation<WithdrawRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankAccountNumber_failsValidation() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountNumber("");
        req.setAmount(BigDecimal.TEN);
        Set<ConstraintViolation<WithdrawRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber")));
    }

    @Test
    void nullAmount_failsValidation() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountNumber("ACC123");
        req.setAmount(null);
        Set<ConstraintViolation<WithdrawRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void negativeAmount_failsValidation() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountNumber("ACC123");
        req.setAmount(BigDecimal.valueOf(-1));
        Set<ConstraintViolation<WithdrawRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void testLombokMethods() {
        WithdrawRequest req1 = new WithdrawRequest();
        req1.setAccountNumber("ACC123");
        req1.setAmount(BigDecimal.TEN);
        WithdrawRequest req2 = new WithdrawRequest();
        req2.setAccountNumber("ACC123");
        req2.setAmount(BigDecimal.TEN);
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        assertTrue(req1.toString().contains("ACC123"));
    }
}
