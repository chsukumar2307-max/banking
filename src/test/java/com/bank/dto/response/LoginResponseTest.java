package com.bank.dto.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginResponseTest {
    @Test
    void allArgsConstructorAndGetters() {
        LoginResponse resp = new LoginResponse("token123", "refresh456", "user1", "acc789");
        assertEquals("token123", resp.getToken());
        assertEquals("refresh456", resp.getRefreshToken());
        assertEquals("user1", resp.getUsername());
        assertEquals("acc789", resp.getAccountNumber());
    }

    @Test
    void equalsAndHashCode() {
        LoginResponse r1 = new LoginResponse("t", "r", "u", "a");
        LoginResponse r2 = new LoginResponse("t", "r", "u", "a");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void toStringShouldContainFields() {
        LoginResponse resp = new LoginResponse("t1", "r1", "u1", "a1");
        String str = resp.toString();
        assertTrue(str.contains("t1"));
        assertTrue(str.contains("r1"));
        assertTrue(str.contains("u1"));
        assertTrue(str.contains("a1"));
    }
}

