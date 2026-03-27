package com.bank.entity;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        Instant now = Instant.now();
        Transaction t = new Transaction(1L, "ACC1", TransactionType.DEPOSIT, new BigDecimal("100.00"),
                new BigDecimal("200.00"), now, TransactionStatus.SUCCESS, "Statement");
        assertEquals(1L, t.getTransactionId());
        assertEquals("ACC1", t.getAccountNumber());
        assertEquals(TransactionType.DEPOSIT, t.getTransactionType());
        assertEquals(new BigDecimal("100.00"), t.getAmount());
        assertEquals(new BigDecimal("200.00"), t.getBalanceAfterTransaction());
        assertEquals(now, t.getTransactionDate());
        assertEquals(TransactionStatus.SUCCESS, t.getStatus());
        assertEquals("Statement", t.getStatement());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        Transaction t = new Transaction();
        t.setTransactionId(2L);
        t.setAccountNumber("ACC2");
        t.setTransactionType(TransactionType.WITHDRAW);
        t.setAmount(new BigDecimal("50.00"));
        t.setBalanceAfterTransaction(new BigDecimal("150.00"));
        Instant now = Instant.now();
        t.setTransactionDate(now);
        t.setStatus(TransactionStatus.FAILED);
        t.setStatement("Failed statement");
        assertEquals(2L, t.getTransactionId());
        assertEquals("ACC2", t.getAccountNumber());
        assertEquals(TransactionType.WITHDRAW, t.getTransactionType());
        assertEquals(new BigDecimal("50.00"), t.getAmount());
        assertEquals(new BigDecimal("150.00"), t.getBalanceAfterTransaction());
        assertEquals(now, t.getTransactionDate());
        assertEquals(TransactionStatus.FAILED, t.getStatus());
        assertEquals("Failed statement", t.getStatement());
    }

    @Test
    void testBuilder() {
        Instant now = Instant.now();
        Transaction t = Transaction.builder()
                .transactionId(3L)
                .accountNumber("ACC3")
                .transactionType(TransactionType.TRANSFER_OUT)
                .amount(new BigDecimal("300.00"))
                .balanceAfterTransaction(new BigDecimal("400.00"))
                .transactionDate(now)
                .status(TransactionStatus.SUCCESS)
                .statement("Builder statement")
                .build();
        assertEquals(3L, t.getTransactionId());
        assertEquals("ACC3", t.getAccountNumber());
        assertEquals(TransactionType.TRANSFER_OUT, t.getTransactionType());
        assertEquals(new BigDecimal("300.00"), t.getAmount());
        assertEquals(new BigDecimal("400.00"), t.getBalanceAfterTransaction());
        assertEquals(now, t.getTransactionDate());
        assertEquals(TransactionStatus.SUCCESS, t.getStatus());
        assertEquals("Builder statement", t.getStatement());
    }

    @Test
    void testEqualsAndHashCode() {
        Instant now = Instant.now();
        Transaction t1 = new Transaction(1L, "ACC1", TransactionType.DEPOSIT, new BigDecimal("100.00"),
                new BigDecimal("200.00"), now, TransactionStatus.SUCCESS, "Statement");
        Transaction t2 = new Transaction(1L, "ACC1", TransactionType.DEPOSIT, new BigDecimal("100.00"),
                new BigDecimal("200.00"), now, TransactionStatus.SUCCESS, "Statement");
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void testToString() {
        Transaction t = new Transaction();
        t.setTransactionId(4L);
        t.setAccountNumber("ACC4");
        String str = t.toString();
        assertTrue(str.contains("ACC4"));
        assertTrue(str.contains("transactionId=4"));
    }

    @Test
    void testPrePersistSetsDefaults() {
        Transaction t = new Transaction();
        t.onCreate();
        assertNotNull(t.getTransactionDate());
        assertEquals(TransactionStatus.SUCCESS, t.getStatus());
    }
}

