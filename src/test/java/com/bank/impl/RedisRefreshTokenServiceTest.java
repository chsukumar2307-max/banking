package com.bank.impl;
import com.bank.service.impl.RedisRefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RedisRefreshTokenServiceTest {
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;
    @InjectMocks
    RedisRefreshTokenService redisRefreshTokenService;

    @BeforeEach
    @SuppressWarnings("resource") // Suppress warning for not using try-with-resources
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        redisRefreshTokenService.prefix = "refresh_token:";
    }

    @Test
    void storeRefreshToken_success() {
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        assertDoesNotThrow(() -> redisRefreshTokenService.storeRefreshToken("user", "token", 3600));
        verify(valueOperations).set("refresh_token:user", "token", 3600, TimeUnit.SECONDS);
    }

    @Test
    void getRefreshToken_success() {
        when(valueOperations.get(anyString())).thenReturn("token");
        String token = redisRefreshTokenService.getRefreshToken("user");
        assertEquals("token", token);
        verify(valueOperations).get("refresh_token:user");
    }

    @Test
    void deleteRefreshToken_success() {
        when(redisTemplate.delete(anyString())).thenReturn(true);
        assertDoesNotThrow(() -> redisRefreshTokenService.deleteRefreshToken("user"));
        verify(redisTemplate).delete("refresh_token:user");
    }

    @Test
    void storeRefreshToken_blankUsernameOrToken_doesNotStore() {
        redisRefreshTokenService.storeRefreshToken("", "token", 3600);
        redisRefreshTokenService.storeRefreshToken("user", "", 3600);
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void getRefreshToken_blankUsername_returnsNull() {
        assertNull(redisRefreshTokenService.getRefreshToken(""));
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void deleteRefreshToken_blankUsername_doesNothing() {
        redisRefreshTokenService.deleteRefreshToken("");
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void storeRefreshToken_redisThrows_logsError() {
        doThrow(new RuntimeException("Redis error")).when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        assertDoesNotThrow(() -> redisRefreshTokenService.storeRefreshToken("user", "token", 3600));
    }

    @Test
    void getRefreshToken_redisThrows_logsError() {
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis error"));
        assertNull(redisRefreshTokenService.getRefreshToken("user"));
    }

    @Test
    void deleteRefreshToken_redisThrows_logsError() {
        when(redisTemplate.delete(anyString())).thenThrow(new RuntimeException("Redis error"));
        assertDoesNotThrow(() -> redisRefreshTokenService.deleteRefreshToken("user"));
    }
}
