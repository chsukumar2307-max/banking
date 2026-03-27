package com.bank.dto.request;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TransferRequestTest {
    private final Validator validator;

    public TransferRequestTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validRequest_passesValidation() {
        TransferRequest req = new TransferRequest();
        req.setFromAccount("ACC1");
        req.setToAccount("ACC2");
        req.setAmount(BigDecimal.TEN);
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankFromAccount_failsValidation() {
        TransferRequest req = new TransferRequest();
        req.setFromAccount("");
        req.setToAccount("ACC2");
        req.setAmount(BigDecimal.TEN);
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fromAccount")));
    }

    @Test
    void blankToAccount_failsValidation() {
        TransferRequest req = new TransferRequest();
        req.setFromAccount("ACC1");
        req.setToAccount("");
        req.setAmount(BigDecimal.TEN);
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("toAccount")));
    }

    @Test
    void nullAmount_failsValidation() {
        TransferRequest req = new TransferRequest();
        req.setFromAccount("ACC1");
        req.setToAccount("ACC2");
        req.setAmount(null);
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void negativeAmount_failsValidation() {
        TransferRequest req = new TransferRequest();
        req.setFromAccount("ACC1");
        req.setToAccount("ACC2");
        req.setAmount(BigDecimal.valueOf(-1));
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void testLombokMethods() {
        TransferRequest req1 = new TransferRequest();
        req1.setFromAccount("ACC1");
        req1.setToAccount("ACC2");
        req1.setAmount(BigDecimal.TEN);
        TransferRequest req2 = new TransferRequest();
        req2.setFromAccount("ACC1");
        req2.setToAccount("ACC2");
        req2.setAmount(BigDecimal.TEN);
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        assertTrue(req1.toString().contains("ACC1"));
    }
}
