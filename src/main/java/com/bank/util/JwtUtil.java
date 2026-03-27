package com.bank.util;
import com.bank.exception.JwtParseException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtUtil {
	private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
	private final SecretKey key;
	private final String issuer;
	private final long expirationMinutes;
	private final long refreshTokenExpirationMinutes;

	private static final int MIN_SECRET_LENGTH_BYTES = 32; // 256 bits

	public JwtUtil(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.issuer:com.bank}") String issuer,
			@Value("${app.jwt.expiration-minutes:60}") long expirationMinutes,
			@Value("${app.jwt.refresh-expiration-minutes:10080}") long refreshTokenExpirationMinutes // default 7 days
	) {
		byte[] bytes;
		try {
			bytes = Decoders.BASE64.decode(secret);
		} catch (Exception ignored) {
			bytes = secret.getBytes();
		}
		if (bytes.length < MIN_SECRET_LENGTH_BYTES) {
			throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 bytes) for security.");
		}
		this.key = Keys.hmacShaKeyFor(bytes);
		this.issuer = issuer;
		this.expirationMinutes = expirationMinutes;
		this.refreshTokenExpirationMinutes = refreshTokenExpirationMinutes;
	}

	public String generateToken(String subject, Map<String, Object> extraClaims) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(expirationMinutes * 60);
		return Jwts.builder()
				.subject(subject)
				.issuer(issuer)
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.claims(extraClaims)
				.signWith(key)
				.compact();
	}

	public Claims parseClaims(String token) {
		try {
			return Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("Failed to parse JWT claims for token [{}]: {}", token, e.getMessage(), e);
			throw new JwtParseException("Invalid JWT token for subject extraction: " + e.getMessage(), e);
		}
	}

	public String extractUsername(String token) {
		try {
			return parseClaims(token).getSubject();
		} catch (Exception e) {
			log.warn("Failed to extract username from JWT: {}", e.getMessage());
			return null;
		}
	}

	public boolean isTokenValid(String token) {
		Claims c = parseClaims(token);
		return c.getExpiration() != null && c.getExpiration().after(new Date());
	}

	// Refresh token expiration is now configurable via constructor
	public String generateRefreshToken(String subject) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(refreshTokenExpirationMinutes * 60);
		return Jwts.builder()
				.subject(subject)
				.issuer(issuer)
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.signWith(key)
				.compact();
	}

	/**
	 * Checks if the refresh token is invalid (expired or malformed).
	 * @param token the refresh token
	 * @return true if the token is invalid, false if valid
	 */
	public boolean isRefreshTokenInvalid(String token) {
		try {
			Claims claims = parseClaims(token);
			return claims.getExpiration() == null || !claims.getExpiration().after(new Date());
		} catch (Exception e) {
			return true;
		}
	}
}

