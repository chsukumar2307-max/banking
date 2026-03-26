package com.bank.impl;
import com.bank.security.JwtUtil;
import com.bank.service.impl.AccessTokenBlacklistService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Duration;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AccessTokenBlacklistServiceTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private AccessTokenBlacklistService blacklistService;

    private final String blacklistPrefix = "test:blacklisted:";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        blacklistService = new AccessTokenBlacklistService(redisTemplate, jwtUtil);
        // Set the blacklistPrefix field via reflection for testing
        java.lang.reflect.Field prefixField = blacklistService.getClass().getDeclaredField("blacklistPrefix");
        prefixField.setAccessible(true);
        prefixField.set(blacklistService, blacklistPrefix);
    }

    @Test
    void blacklistToken_validToken_setsInRedis() {
        String token = "validToken";
        Date expiration = new Date(System.currentTimeMillis() + 10000);
        Claims claims = mock(Claims.class);
        when(claims.getExpiration()).thenReturn(expiration);
        when(jwtUtil.parseClaims(token)).thenReturn(claims);

        blacklistService.blacklistToken(token);

        verify(valueOperations).set(eq(blacklistPrefix + token), eq("1"), any(Duration.class));
    }

    @Test
    void blacklistToken_nullOrBlankToken_doesNothing() {
        blacklistService.blacklistToken(null);
        blacklistService.blacklistToken("");
        verifyNoInteractions(valueOperations);
    }

    @Test
    void blacklistToken_expiredToken_doesNotSet() {
        String token = "expiredToken";
        Date expiration = new Date(System.currentTimeMillis() - 10000);
        Claims claims = mock(Claims.class);
        when(claims.getExpiration()).thenReturn(expiration);
        when(jwtUtil.parseClaims(token)).thenReturn(claims);

        blacklistService.blacklistToken(token);
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void blacklistToken_noExpiration_doesNotSet() {
        String token = "noExpToken";
        Claims claims = mock(Claims.class);
        when(claims.getExpiration()).thenReturn(null);
        when(jwtUtil.parseClaims(token)).thenReturn(claims);
        blacklistService.blacklistToken(token);
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void isTokenBlacklisted_nullOrBlankToken_returnsFalse() {
        assertFalse(blacklistService.isTokenBlacklisted(null));
        assertFalse(blacklistService.isTokenBlacklisted(" "));
    }

    @Test
    void isTokenBlacklisted_redisReturnsTrue_returnsTrue() {
        String token = "blacklistedToken";
        when(redisTemplate.hasKey(blacklistPrefix + token)).thenReturn(true);
        assertTrue(blacklistService.isTokenBlacklisted(token));
    }

    @Test
    void isTokenBlacklisted_redisReturnsFalse_returnsFalse() {
        String token = "notBlacklistedToken";
        when(redisTemplate.hasKey(blacklistPrefix + token)).thenReturn(false);
        assertFalse(blacklistService.isTokenBlacklisted(token));
    }

    @Test
    void isTokenBlacklisted_redisThrowsException_returnsFalse() {
        String token = "errorToken";
        when(redisTemplate.hasKey(blacklistPrefix + token)).thenThrow(new RuntimeException("Redis error"));
        assertFalse(blacklistService.isTokenBlacklisted(token));
    }
}
