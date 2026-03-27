package com.bank.dto.response;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class TransactionResponseTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        Instant now = Instant.now();
        TransactionResponse resp = new TransactionResponse(
                1L, "ACC123", TransactionType.DEPOSIT, new BigDecimal("100.00"),
                new BigDecimal("200.00"), now, TransactionStatus.SUCCESS, "Statement");
        assertEquals(1L, resp.getTransactionId());
        assertEquals("ACC123", resp.getAccountNumber());
        assertEquals(TransactionType.DEPOSIT, resp.getTransactionType());
        assertEquals(new BigDecimal("100.00"), resp.getAmount());
        assertEquals(new BigDecimal("200.00"), resp.getBalanceAfterTransaction());
        assertEquals(now, resp.getTransactionDate());
        assertEquals(TransactionStatus.SUCCESS, resp.getStatus());
        assertEquals("Statement", resp.getStatement());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        TransactionResponse resp = new TransactionResponse();
        resp.setTransactionId(2L);
        resp.setAccountNumber("ACC456");
        resp.setTransactionType(TransactionType.WITHDRAW); // Use WITHDRAW instead of WITHDRAWAL
        resp.setAmount(new BigDecimal("50.00"));
        resp.setBalanceAfterTransaction(new BigDecimal("150.00"));
        Instant now = Instant.now();
        resp.setTransactionDate(now);
        resp.setStatus(TransactionStatus.FAILED);
        resp.setStatement("Failed statement");
        assertEquals(2L, resp.getTransactionId());
        assertEquals("ACC456", resp.getAccountNumber());
        assertEquals(TransactionType.WITHDRAW, resp.getTransactionType()); // Use WITHDRAW
        assertEquals(new BigDecimal("50.00"), resp.getAmount());
        assertEquals(new BigDecimal("150.00"), resp.getBalanceAfterTransaction());
        assertEquals(now, resp.getTransactionDate());
        assertEquals(TransactionStatus.FAILED, resp.getStatus());
        assertEquals("Failed statement", resp.getStatement());
    }

    @Test
    void testEqualsAndHashCode() {
        Instant now = Instant.now();
        TransactionResponse resp1 = new TransactionResponse(1L, "ACC123", TransactionType.DEPOSIT, new BigDecimal("100.00"),
                new BigDecimal("200.00"), now, TransactionStatus.SUCCESS, "Statement");
        TransactionResponse resp2 = new TransactionResponse(1L, "ACC123", TransactionType.DEPOSIT, new BigDecimal("100.00"),
                new BigDecimal("200.00"), now, TransactionStatus.SUCCESS, "Statement");
        assertEquals(resp1, resp2);
        assertEquals(resp1.hashCode(), resp2.hashCode());
    }

    @Test
    void testToString() {
        TransactionResponse resp = new TransactionResponse();
        resp.setTransactionId(3L);
        resp.setAccountNumber("ACC789");
        String str = resp.toString();
        assertTrue(str.contains("ACC789"));
        assertTrue(str.contains("transactionId=3"));
    }
}
