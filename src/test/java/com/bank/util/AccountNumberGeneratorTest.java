package com.bank.util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccountNumberGeneratorTest {
    @Test
    void generate_returnsValidAccountNumber() {
        AccountNumberGenerator generator = new AccountNumberGenerator();
        String accountNumber = generator.generate();
        assertNotNull(accountNumber);
        assertTrue(accountNumber.startsWith("ACC"));
        assertEquals(9, accountNumber.length());
        assertDoesNotThrow(() -> Integer.parseInt(accountNumber.substring(3)));
    }

    @Test
    void generate_multipleCalls_uniqueNumbers() {
        AccountNumberGenerator generator = new AccountNumberGenerator();
        String acc1 = generator.generate();
        String acc2 = generator.generate();
        assertNotEquals(acc1, acc2);
    }
}

