package com.bank.dto.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CheckCustomerResponseTest {
    @Test
    void allArgsConstructorAndGetters() {
        CheckCustomerResponse resp = new CheckCustomerResponse(true, "Exists");
        assertTrue(resp.isExists());
        assertEquals("Exists", resp.getMessage());
    }

    @Test
    void equalsAndHashCode() {
        CheckCustomerResponse r1 = new CheckCustomerResponse(false, "msg");
        CheckCustomerResponse r2 = new CheckCustomerResponse(false, "msg");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void toStringShouldContainFields() {
        CheckCustomerResponse resp = new CheckCustomerResponse(true, "Test");
        String str = resp.toString();
        assertTrue(str.contains("true"));
        assertTrue(str.contains("Test"));
    }
}

