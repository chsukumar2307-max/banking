package com.bank.dto.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {
    @Test
    void testSettersAndGetters() {
        LoginRequest req = new LoginRequest();
        req.setUsername("user1");
        req.setPassword("pass1");
        assertEquals("user1", req.getUsername());
        assertEquals("pass1", req.getPassword());
    }

    @Test
    void testToStringAndEquals() {
        LoginRequest req1 = new LoginRequest();
        req1.setUsername("user1");
        req1.setPassword("pass1");
        LoginRequest req2 = new LoginRequest();
        req2.setUsername("user1");
        req2.setPassword("pass1");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        assertTrue(req1.toString().contains("user1"));
    }
}

