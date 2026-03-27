package com.bank.service.impl;
import com.bank.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class AccessTokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    /**
     * Prefix for blacklisted access tokens in Redis. Injected by Spring.
     * Suppress unused warning as it is injected by Spring.
     */
    @Value("${security.jwt.blacklist-prefix:blacklisted:accessToken:}")
    @SuppressWarnings("unused")
    private String blacklistPrefix;

    public AccessTokenBlacklistService(
            RedisTemplate<String, String> redisTemplate,
            JwtUtil jwtUtil
    ) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Blacklists a JWT access token until its expiration.
     * @param token JWT token to blacklist
     */
    public void blacklistToken(String token) {
        if (!StringUtils.hasText(token)) {
            log.warn("Attempted to blacklist a null or blank token.");
            throw new IllegalArgumentException("Cannot blacklist a null or blank token.");
        }
        try {
            Date expiration = Optional.ofNullable(jwtUtil.parseClaims(token))
                    .map(Claims::getExpiration)
                    .orElse(null);

            if (expiration == null) {
                log.warn("Token does not have an expiration date and cannot be blacklisted: {}", token);
                throw new IllegalArgumentException("Token does not have an expiration date and cannot be blacklisted.");
            }

            long expiryMillis = expiration.getTime() - System.currentTimeMillis();
            if (expiryMillis > 0) {
                String redisKey = buildBlacklistKey(token);
                redisTemplate.opsForValue().set(redisKey, "1", Duration.ofMillis(expiryMillis));
                log.info("Token blacklisted until expiration: {}", expiration);
            } else {
                log.info("Token already expired, not blacklisting.");
            }
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage(), e);
        }
    }

    /**
     * Checks if a JWT access token is blacklisted.
     * @param token JWT token to check
     * @return true if blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            String redisKey = buildBlacklistKey(token);
            Boolean result = redisTemplate.hasKey(redisKey);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Failed to check if token is blacklisted: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Builds the Redis key for a blacklisted token.
     * @param token JWT token
     * @return Redis key string
     */
    private String buildBlacklistKey(String token) {
        return blacklistPrefix + token;
    }
}
