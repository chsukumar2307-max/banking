package com.bank.util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;

class OtpGeneratorTest {
    @Test
    void generate_defaultLength_isSixDigits() {
        OtpGenerator generator = new OtpGenerator(6);
        String otp = generator.generate();
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void generate_minLength_isFourDigits() {
        OtpGenerator generator = new OtpGenerator(2); // Should clamp to 4
        String otp = generator.generate();
        assertEquals(4, otp.length());
        assertTrue(otp.matches("\\d{4}"));
    }

    @Test
    void generate_maxLength_isTenDigits() {
        OtpGenerator generator = new OtpGenerator(20); // Should clamp to 10
        String otp = generator.generate();
        assertEquals(10, otp.length());
        assertTrue(otp.matches("\\d{10}"));
    }

    @Test
    void generate_multipleCalls_areRandom() {
        OtpGenerator generator = new OtpGenerator(6);
        Set<String> otps = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            otps.add(generator.generate());
        }
        // Should have high uniqueness
        assertTrue(otps.size() > 15);
    }
}

