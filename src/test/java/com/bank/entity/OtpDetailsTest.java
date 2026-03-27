package com.bank.entity;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OtpDetailsTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        Instant now = Instant.now();
        OtpDetails o = new OtpDetails(1L, "user", "1234567890", "123456", now, true, now);
        assertEquals(1L, o.getId());
        assertEquals("user", o.getUsername());
        assertEquals("1234567890", o.getMobileNumber());
        assertEquals("123456", o.getOtp());
        assertEquals(now, o.getExpiryTime());
        assertTrue(o.isVerified());
        assertEquals(now, o.getCreatedAt());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        OtpDetails o = new OtpDetails();
        o.setId(2L);
        o.setUsername("user2");
        o.setMobileNumber("0987654321");
        o.setOtp("654321");
        Instant now = Instant.now();
        o.setExpiryTime(now);
        o.setVerified(false);
        o.setCreatedAt(now);
        assertEquals(2L, o.getId());
        assertEquals("user2", o.getUsername());
        assertEquals("0987654321", o.getMobileNumber());
        assertEquals("654321", o.getOtp());
        assertEquals(now, o.getExpiryTime());
        assertFalse(o.isVerified());
        assertEquals(now, o.getCreatedAt());
    }

    @Test
    void testBuilder() {
        Instant now = Instant.now();
        OtpDetails o = OtpDetails.builder()
                .id(3L)
                .username("builder")
                .mobileNumber("1112223333")
                .otp("111222")
                .expiryTime(now)
                .verified(true)
                .createdAt(now)
                .build();
        assertEquals(3L, o.getId());
        assertEquals("builder", o.getUsername());
        assertEquals("1112223333", o.getMobileNumber());
        assertEquals("111222", o.getOtp());
        assertEquals(now, o.getExpiryTime());
        assertTrue(o.isVerified());
        assertEquals(now, o.getCreatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Instant now = Instant.now();
        OtpDetails o1 = new OtpDetails(1L, "user", "1234567890", "123456", now, true, now);
        OtpDetails o2 = new OtpDetails(1L, "user", "1234567890", "123456", now, true, now);
        assertEquals(o1, o2);
        assertEquals(o1.hashCode(), o2.hashCode());
    }

    @Test
    void testToString() {
        OtpDetails o = new OtpDetails();
        o.setId(4L);
        o.setUsername("toString");
        o.setMobileNumber("ACC4");
        String str = o.toString();
        assertTrue(str.contains("toString"));
        assertTrue(str.contains("ACC4"));
    }
}

