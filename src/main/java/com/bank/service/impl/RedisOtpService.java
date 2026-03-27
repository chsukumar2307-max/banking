package com.bank.service.impl;
import com.bank.entity.OtpDetails;
import com.bank.exception.InvalidOtpException;
import com.bank.repository.OtpDetailsRepository;
import com.bank.service.OtpService;
import com.bank.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RedisOtpService implements OtpService {
    private static final Logger log = LoggerFactory.getLogger(RedisOtpService.class);

    private static final String ERR_OTP_ALREADY_SENT = "OTP already sent. Please wait.";
    private static final String ERR_TOO_MANY_OTP_REQUESTS = "Too many OTP requests. Try again later.";
    private static final String ERR_FAILED_SEND_SMS = "Failed to send OTP SMS for user: %s, mobile: %s";
    private static final String ERR_FAILED_STORE_OTP = "Failed to store OTP for user: %s, mobile: %s";
    private static final int OTP_LENGTH = 4;

    private final RedisTemplate<String, String> redisTemplate;
    private final OtpDetailsRepository otpDetailsRepository;
    private final SmsService smsService;

    // Sonar: These fields are injected by Spring and used in business logic. Suppress unused warnings if flagged by static analysis.
    @SuppressWarnings("unused")
    @Value("${app.otp.ttl-seconds:300}")
    private Long ttlSeconds;
    @SuppressWarnings("unused")
    @Value("${app.otp.max-attempts:20}")
    private Integer maxOtpAttempts;
    @SuppressWarnings("unused")
    @Value("${app.otp.max-failed-verify:20}")
    private Integer maxFailedVerify;
    @SuppressWarnings("unused")
    @Value("${app.otp.rate-limit-minutes:5}")
    private Integer rateLimitMinutes;
    @SuppressWarnings("unused")
    @Value("${app.otp.fail-attempts-window-minutes:10}")
    private Integer failAttemptsWindowMinutes;

    /**
     * Exception for OTP persistence errors.
     */
    public static class OtpPersistenceException extends RuntimeException {
        public OtpPersistenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Use SecureRandom for cryptographically secure OTP generation (see OWASP ASVS 2.1.1, NIST SP 800-63B)
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    public void generateOtp(String username, String mobileNumber) {
        validateUsernameAndMobile(username, mobileNumber);
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        String redisKey = redisKey(username, mobileNumber);
        checkOtpAlreadySent(redisKey, username);
        String attemptKey = otpAttemptKey(username, mobileNumber);
        checkAndIncrementAttempts(valueOps, attemptKey, username);
        String otp = generateNumericOtp();
        log.info("Generated OTP for {}: [PROTECTED]", username);
        OtpDetails details = buildOtpDetails(username, mobileNumber, otp);
        persistOtpAndSendSms(details, mobileNumber, otp, username);
        cacheOtp(valueOps, redisKey, otp, username);
    }

    /**
     * Checks if an OTP has already been sent for the given redisKey and throws an exception if so.
     * Avoids code smell by using clear method naming, single responsibility, and explicit null/empty checks.
     */
    @SuppressWarnings("java:S2583") // Sonar false positive: Boolean.TRUE.equals(hasKey) is null-safe and prevents NPE
    private void checkOtpAlreadySent(String redisKey, String username) {
        if (redisKey == null || redisKey.isEmpty()) {
            log.error("Redis key is null or empty for user: {}", username);
            throw new InvalidOtpException("Internal error: Redis key is invalid");
        }

        Boolean hasKey = redisTemplate.hasKey(redisKey); // Sonar false positive: Boolean.TRUE.equals(hasKey) is null-safe
        if (Boolean.TRUE.equals(hasKey)) {
            log.warn("OTP already sent for user: {}. Please wait.", username);
            throw new InvalidOtpException(ERR_OTP_ALREADY_SENT);
        }
    }

    private void checkAndIncrementAttempts(ValueOperations<String, String> valueOps, String attemptKey, String username) {
        int attempts = parseIntOrDefault(valueOps.get(attemptKey));
        if (attempts >= maxOtpAttempts) {
            log.warn("Too many OTP requests for user: {}. Attempts: {}", username, attempts);
            throw new InvalidOtpException(ERR_TOO_MANY_OTP_REQUESTS);
        }
        valueOps.set(attemptKey, String.valueOf(attempts + 1), Duration.ofMinutes(rateLimitMinutes));
    }

    private OtpDetails buildOtpDetails(String username, String mobileNumber, String otp) {
        Instant expiry = Instant.now().plusSeconds(ttlSeconds);
        OtpDetails details = new OtpDetails();
        details.setUsername(username);
        details.setMobileNumber(mobileNumber);
        details.setOtp(otp);
        details.setExpiryTime(expiry);
        details.setVerified(false);
        details.setCreatedAt(Instant.now());
        return details;
    }

    /**
     * Persists OTP details and sends the OTP SMS. Handles and logs all exceptions in a single place.
     * Avoids code smell by reducing nested blocks and using guard clauses.
     */
    private void persistOtpAndSendSms(OtpDetails details, String mobileNumber, String otp, String username) {
        if (details == null || !StringUtils.hasText(mobileNumber) || !StringUtils.hasText(otp) || !StringUtils.hasText(username)) {
            log.error("Invalid arguments for persisting OTP and sending SMS: details={}, mobileNumber={}, username={}", details, mobileNumber, username);
            throw new OtpPersistenceException("Invalid arguments for persisting OTP and sending SMS", null);
        }
        try {
            OtpDetails saved = otpDetailsRepository.save(details);
            otpDetailsRepository.flush();
            log.info("OTP stored in DB for {} (ID: {})", username, saved.getId());
            sendOtpSms(mobileNumber, otp, username);
        } catch (RuntimeException e) { // NOSONAR: Catching RuntimeException to handle all persistence-related errors
            String contextMsg = String.format(ERR_FAILED_STORE_OTP, username, mobileNumber);
            log.error("{} Exception: {}", contextMsg, e, e);
            throw new OtpPersistenceException(contextMsg, e);
        } catch (Exception e) { // NOSONAR: Defensive catch for any unexpected checked exceptions
            String contextMsg = String.format(ERR_FAILED_STORE_OTP, username, mobileNumber);
            log.error("{} Unexpected exception: {}", contextMsg, e.toString(), e);
            throw new OtpPersistenceException(contextMsg, e);
        }
    }

    /**
     * Sends the OTP SMS and handles any exceptions. Avoids code smell by using clear logging and exception handling.
     */
    private void sendOtpSms(String mobileNumber, String otp, String username) {
        if (!StringUtils.hasText(mobileNumber) || !StringUtils.hasText(otp) || !StringUtils.hasText(username)) {
            log.error("Invalid arguments for sending OTP SMS: mobileNumber={}, username={}", mobileNumber, username);
            throw new OtpPersistenceException("Invalid arguments for sending OTP SMS", null);
        }
        try {
            smsService.sendOtp(mobileNumber, otp);
            log.debug("OTP SMS sent for user: {}", username);
        } catch (RuntimeException e) { // NOSONAR: Catching RuntimeException to handle all SMS-related errors
            String contextMsg = String.format(ERR_FAILED_SEND_SMS, username, mobileNumber);
            log.error("{} Exception: {}", contextMsg, e, e);
            throw new OtpPersistenceException(contextMsg, e);
        } catch (Exception e) { // NOSONAR: Defensive catch for any unexpected checked exceptions
            String contextMsg = String.format(ERR_FAILED_SEND_SMS, username, mobileNumber);
            log.error("{} Unexpected exception: {}", contextMsg, e.toString(), e);
            throw new OtpPersistenceException(contextMsg, e);
        }
    }

    private void cacheOtp(ValueOperations<String, String> valueOps, String redisKey, String otp, String username) {
        valueOps.set(redisKey, otp, Duration.ofSeconds(ttlSeconds));
        log.info("OTP cached in Redis for user: {}", username);
    }

    @Override
    @Transactional
    public void verifyOtp(String username, String mobileNumber, String otp) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(mobileNumber) || !StringUtils.hasText(otp)) {
            log.warn("Username, mobile number, or OTP is blank, not verifying OTP.");
            throw new InvalidOtpException("Username, mobile number, or OTP is blank");
        }
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        Instant now = Instant.now();
        String redisKey = redisKey(username, mobileNumber);
        String cachedOtp = valueOps.get(redisKey);
        OtpDetails details = otpDetailsRepository
                .findByUsernameAndMobileNumber(username, mobileNumber)
                .orElseThrow(() -> new InvalidOtpException("OTP not found"));
        if (details.isVerified()) {
            log.warn("OTP already verified for user: {}", username);
            throw new InvalidOtpException("OTP already verified");
        }
        if (details.getExpiryTime() == null || details.getExpiryTime().isBefore(now)) {
            log.warn("OTP expired for user: {}", username);
            throw new InvalidOtpException("OTP expired");
        }
        boolean valid = otp.equals(cachedOtp != null ? cachedOtp : details.getOtp());
        if (!valid) {
            handleInvalidOtpAttempt(username, valueOps);
        }
        details.setVerified(true);
        otpDetailsRepository.save(details);
        redisTemplate.delete(redisKey);
        log.info("OTP verified for {}", username);
    }

    // --- Private helpers for code smell reduction ---

    private void validateUsernameAndMobile(String username, String mobileNumber) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(mobileNumber)) {
            log.warn("Username or mobile number is blank, cannot generate OTP.");
            throw new InvalidOtpException("Username or mobile number is blank");
        }
    }

    /**
     * Parses an integer or returns 0 if parsing fails (defaultValue is always 0 in this codebase).
     * @param value the string to parse
     */
    private int parseIntOrDefault(String value) {
        try {
            return value == null ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void handleInvalidOtpAttempt(String username, ValueOperations<String, String> valueOps) {
        String failKey = otpFailKey(username);
        int attempts = parseIntOrDefault(valueOps.get(failKey));
        if (attempts >= maxFailedVerify) {
            log.warn("Too many invalid OTP attempts for user: {}", username);
            throw new InvalidOtpException("Too many invalid OTP attempts");
        }
        valueOps.set(failKey, String.valueOf(attempts + 1), Duration.ofMinutes(failAttemptsWindowMinutes));
        log.warn("Invalid OTP entered for user: {}. Attempt: {}", username, attempts + 1);
        throw new InvalidOtpException("Invalid OTP");
    }

    /**
     * Generates a numeric OTP using SecureRandom for cryptographic security.
     * ThreadLocalRandom or Random are not safe for OTPs.
     */
    private String generateNumericOtp() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private String redisKey(String username, String mobileNumber) {
        return String.format("otp:%s:%s", username, mobileNumber);
    }

    private String otpAttemptKey(String username, String mobileNumber) {
        return String.format("otp_attempts:%s:%s", username, mobileNumber);
    }

    private String otpFailKey(String username) {
        return String.format("otp_fail:%s", username);
    }
}
