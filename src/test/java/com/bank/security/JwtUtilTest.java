package com.bank.security;
import com.bank.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private JwtUtil jwtUtil;
    private final String secret = "testsecretkeytestsecretkeytestsecretkeytestsecretkey";
    private final String issuer = "test-issuer";
    private final long refreshExpirationMinutes = 7 * 24 * 60; // 7 days

    @BeforeEach
    void setUp() {
        long expirationMinutes = 1;
        jwtUtil = new JwtUtil(secret, issuer, expirationMinutes, refreshExpirationMinutes);
    }

    @Test
    void generateAndParseToken_success() {
        String token = jwtUtil.generateToken("user", Map.of("role", "admin"));
        assertNotNull(token);
        Claims claims = jwtUtil.parseClaims(token);
        assertEquals("user", claims.getSubject());
        assertEquals("admin", claims.get("role"));
        assertEquals(issuer, claims.getIssuer());
    }

    @Test
    void extractUsername_returnsSubject() {
        String token = jwtUtil.generateToken("user", Map.of());
        assertEquals("user", jwtUtil.extractUsername(token));
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("user", Map.of());
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_expiredToken_throwsException() {
        JwtUtil shortLivedJwtUtil = new JwtUtil(secret, issuer, 0, refreshExpirationMinutes); // expires immediately
        String token = shortLivedJwtUtil.generateToken("user", Map.of());
        assertThrows(com.bank.exception.JwtParseException.class, () -> shortLivedJwtUtil.isTokenValid(token));
    }

    @Test
    void generateRefreshToken_success() {
        String token = jwtUtil.generateRefreshToken("user");
        assertNotNull(token);
        Claims claims = jwtUtil.parseClaims(token);
        assertEquals("user", claims.getSubject());
        assertEquals(issuer, claims.getIssuer());
    }
}
