package com.bank.dto.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageResponseTest {
    @Test
    void allArgsConstructorAndGetters() {
        MessageResponse resp = new MessageResponse("Hello");
        assertEquals("Hello", resp.getMessage());
    }

    @Test
    void equalsAndHashCode() {
        MessageResponse r1 = new MessageResponse("msg");
        MessageResponse r2 = new MessageResponse("msg");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void toStringShouldContainMessage() {
        MessageResponse resp = new MessageResponse("Test");
        assertTrue(resp.toString().contains("Test"));
    }
}

