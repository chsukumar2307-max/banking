package com.bank.dto.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CheckCustomerRequestTest {
    @Test
    void testSettersAndGetters() {
        CheckCustomerRequest req = new CheckCustomerRequest();
        req.setUsername("user1");
        req.setMobileNumber("1234567890");
        assertEquals("user1", req.getUsername());
        assertEquals("1234567890", req.getMobileNumber());
    }

    @Test
    void testToStringAndEquals() {
        CheckCustomerRequest req1 = new CheckCustomerRequest();
        req1.setUsername("user1");
        req1.setMobileNumber("1234567890");
        CheckCustomerRequest req2 = new CheckCustomerRequest();
        req2.setUsername("user1");
        req2.setMobileNumber("1234567890");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        assertTrue(req1.toString().contains("user1"));
    }
}

