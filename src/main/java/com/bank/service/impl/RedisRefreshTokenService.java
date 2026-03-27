package com.bank.service.impl;
import com.bank.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.concurrent.TimeUnit;
/**
 * Service for managing refresh tokens in Redis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRefreshTokenService implements RefreshTokenService {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.refresh-token.prefix:refresh_token:}")
    public String prefix; // package-private for test injection

    private static final String ERROR_BLANK_INPUT = "Username or refresh token is blank, not storing in Redis.";

    @Override
    public void storeRefreshToken(String username, String refreshToken, long expirySeconds) {
        if (!isValid(username, refreshToken)) {
            log.warn(ERROR_BLANK_INPUT + " (username: '{}', token: '{}')", username, refreshToken);
            throw new IllegalArgumentException(ERROR_BLANK_INPUT);
        }
        try {
            redisTemplate.opsForValue().set(buildKey(username), refreshToken, expirySeconds, TimeUnit.SECONDS);
            log.info("Stored refresh token for user '{}'", username);
        } catch (Exception e) {
            log.error("Failed to store refresh token for user '{}': {}", username, e.getMessage(), e);
        }
    }

    @Override
    public String getRefreshToken(String username) {
        if (!StringUtils.hasText(username)) {
            log.warn("Username is blank, cannot get refresh token.");
            return null;
        }
        try {
            return redisTemplate.opsForValue().get(buildKey(username));
        } catch (Exception e) {
            log.error("Failed to get refresh token for user '{}': {}", username, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void deleteRefreshToken(String username) {
        if (!StringUtils.hasText(username)) {
            log.warn("Username is blank, cannot delete refresh token.");
            throw new IllegalArgumentException("Username is blank, cannot delete refresh token.");
        }
        try {
            redisTemplate.delete(buildKey(username));
            log.info("Deleted refresh token for user '{}'", username);
        } catch (Exception e) {
            log.error("Failed to delete refresh token for user '{}': {}", username, e.getMessage(), e);
        }
    }

    private boolean isValid(String username, String refreshToken) {
        return StringUtils.hasText(username) && StringUtils.hasText(refreshToken);
    }

    private String buildKey(String username) {
        return prefix + username;
    }
}
