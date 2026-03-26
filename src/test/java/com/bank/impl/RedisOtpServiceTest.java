package com.bank.impl;
import com.bank.entity.OtpDetails;
import com.bank.exception.InvalidOtpException;
import com.bank.repository.OtpDetailsRepository;
import com.bank.service.SmsService;
import com.bank.service.impl.RedisOtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("resource")
class RedisOtpServiceTest {
    
            @Test
            void generateOtp_nullOrBlankUsernameOrMobile_throwsInvalidOtpException() {
                assertThrows(InvalidOtpException.class, () -> redisOtpService.generateOtp(null, "123"));
                assertThrows(InvalidOtpException.class, () -> redisOtpService.generateOtp("", "123"));
                assertThrows(InvalidOtpException.class, () -> redisOtpService.generateOtp("user", null));
                assertThrows(InvalidOtpException.class, () -> redisOtpService.generateOtp("user", ""));
            }

            @Test
            void verifyOtp_nullOrBlankUsernameOrMobileOrOtp_throwsInvalidOtpException() {
                assertThrows(InvalidOtpException.class, () -> redisOtpService.verifyOtp(null, "123", "1234"));
                assertThrows(InvalidOtpException.class, () -> redisOtpService.verifyOtp("", "123", "1234"));
                assertThrows(InvalidOtpException.class, () -> redisOtpService.verifyOtp("user", null, "1234"));
                assertThrows(InvalidOtpException.class, () -> redisOtpService.verifyOtp("user", "", "1234"));
                assertThrows(InvalidOtpException.class, () -> redisOtpService.verifyOtp("user", "123", null));
                assertThrows(InvalidOtpException.class, () -> redisOtpService.verifyOtp("user", "123", ""));
            }

            @Test
            void generateOtp_smsServiceThrows_throwsOtpPersistenceException() {
                when(redisTemplate.hasKey(anyString())).thenReturn(false);
                when(valueOperations.get(any())).thenReturn(null);
                when(otpDetailsRepository.save(any(OtpDetails.class))).thenAnswer(i -> i.getArgument(0));
                doThrow(new RuntimeException("SMS error")).when(smsService).sendOtp(anyString(), anyString());
                doNothing().when(otpDetailsRepository).flush();
                assertThrows(RedisOtpService.OtpPersistenceException.class, () -> redisOtpService.generateOtp("user", "123"));
            }

            @Test
            void generateOtp_redisTemplateOpsForValueThrows_throwsException() {
                when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis error"));
                Exception ex = assertThrows(RuntimeException.class, () -> redisOtpService.generateOtp("user", "123"));
                assertTrue(ex.getMessage().contains("Redis error"));
            }

            @Test
            void generateOtp_otpDetailsRepositoryFlushThrows_throwsOtpPersistenceException() {
                when(redisTemplate.hasKey(anyString())).thenReturn(false);
                when(valueOperations.get(any())).thenReturn(null);
                when(otpDetailsRepository.save(any(OtpDetails.class))).thenAnswer(i -> i.getArgument(0));
                doThrow(new RuntimeException("Flush error")).when(otpDetailsRepository).flush();
                Exception ex = assertThrows(RedisOtpService.OtpPersistenceException.class, () -> redisOtpService.generateOtp("user", "123"));
                assertTrue(ex.getMessage().contains("Failed to store OTP for user"));
            }

            // Optionally, test for save() returning null (should not happen, but for completeness)
            @Test
            void generateOtp_otpDetailsRepositorySaveReturnsNull_throwsOtpPersistenceException() {
                when(redisTemplate.hasKey(anyString())).thenReturn(false);
                when(valueOperations.get(any())).thenReturn(null);
                when(otpDetailsRepository.save(any(OtpDetails.class))).thenReturn(null);
                doNothing().when(otpDetailsRepository).flush();
                assertThrows(RedisOtpService.OtpPersistenceException.class, () -> redisOtpService.generateOtp("user", "123"));
            }
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;
    @Mock OtpDetailsRepository otpDetailsRepository;
    @Mock SmsService smsService;
    @InjectMocks
    RedisOtpService redisOtpService;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // Avoid hitting rate limits in tests unless explicitly testing for them
        Field maxOtpAttemptsField = redisOtpService.getClass().getDeclaredField("maxOtpAttempts");
        maxOtpAttemptsField.setAccessible(true);
        maxOtpAttemptsField.set(redisOtpService, 100);
        Field maxFailedVerifyField = redisOtpService.getClass().getDeclaredField("maxFailedVerify");
        maxFailedVerifyField.setAccessible(true);
        maxFailedVerifyField.set(redisOtpService, 100);
    }

    @Test
    void generateOtp_success() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.get(any())).thenReturn(null);
        when(otpDetailsRepository.save(any(OtpDetails.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(smsService).sendOtp(anyString(), anyString());
        doNothing().when(otpDetailsRepository).flush();
        assertDoesNotThrow(() -> redisOtpService.generateOtp("user", "123"));
    }

    @Test
    void generateOtp_alreadySent() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        Exception ex = assertThrows(InvalidOtpException.class, () -> redisOtpService.generateOtp("user", "123"));
        assertEquals("OTP already sent. Please wait.", ex.getMessage());
    }

    @Test
    void generateOtp_tooManyAttempts() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.get(any())).thenReturn("100"); // match maxOtpAttempts
        Exception ex = assertThrows(InvalidOtpException.class, () -> redisOtpService.generateOtp("user", "123"));
        assertEquals("Too many OTP requests. Try again later.", ex.getMessage());
    }

    @Test
    void generateOtp_dbFailure() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.get(any())).thenReturn(null);
        doThrow(new RuntimeException("DB error")).when(otpDetailsRepository).save(any(OtpDetails.class));
        Exception ex = assertThrows(RedisOtpService.OtpPersistenceException.class, () -> redisOtpService.generateOtp("user", "123"));
        assertTrue(ex.getMessage().contains("Failed to store OTP for user"));
    }

    @Test
    void verifyOtp_success() {
        OtpDetails details = new OtpDetails();
        details.setVerified(false);
        details.setExpiryTime(Instant.now().plusSeconds(60));
        details.setOtp("1234");
        when(valueOperations.get(any())).thenReturn("1234");
        when(otpDetailsRepository.findByUsernameAndMobileNumber(anyString(), anyString())).thenReturn(Optional.of(details));
        when(otpDetailsRepository.save(any(OtpDetails.class))).thenReturn(details);
        assertDoesNotThrow(() -> redisOtpService.verifyOtp("user", "123", "1234"));
    }

    @Test
    void verifyOtp_alreadyVerified() {
        OtpDetails details = new OtpDetails();
        details.setVerified(true);
        when(otpDetailsRepository.findByUsernameAndMobileNumber(anyString(), anyString())).thenReturn(Optional.of(details));
        when(valueOperations.get(any())).thenReturn("1234"); // avoid NPE
        Exception ex = assertThrows(InvalidOtpException.class, () -> redisOtpService.verifyOtp("user", "123", "1234"));
        assertEquals("OTP already verified", ex.getMessage());
    }

    @Test
    void verifyOtp_expired() {
        OtpDetails details = new OtpDetails();
        details.setVerified(false);
        details.setExpiryTime(Instant.now().minusSeconds(60));
        when(otpDetailsRepository.findByUsernameAndMobileNumber(anyString(), anyString())).thenReturn(Optional.of(details));
        when(valueOperations.get(any())).thenReturn("1234"); // avoid NPE
        Exception ex = assertThrows(InvalidOtpException.class, () -> redisOtpService.verifyOtp("user", "123", "1234"));
        assertEquals("OTP expired", ex.getMessage());
    }

    @Test
    void verifyOtp_invalidOtp() {
        OtpDetails details = new OtpDetails();
        details.setVerified(false);
        details.setExpiryTime(Instant.now().plusSeconds(60));
        details.setOtp("9999");
        when(valueOperations.get(any())).thenReturn("8888");
        when(otpDetailsRepository.findByUsernameAndMobileNumber(anyString(), anyString())).thenReturn(Optional.of(details));
        // For the fail count, use ArgumentMatchers.argThat
        when(valueOperations.get(argThat(arg -> arg != null && ((String)arg).startsWith("otp_fail:")))).thenReturn(null);
        Exception ex = assertThrows(InvalidOtpException.class, () -> redisOtpService.verifyOtp("user", "123", "1234"));
        assertEquals("Invalid OTP", ex.getMessage());
    }

    @Test
    void verifyOtp_tooManyInvalidAttempts() {
        OtpDetails details = new OtpDetails();
        details.setVerified(false);
        details.setExpiryTime(Instant.now().plusSeconds(60));
        details.setOtp("9999");
        when(valueOperations.get(any())).thenReturn("8888");
        when(otpDetailsRepository.findByUsernameAndMobileNumber(anyString(), anyString())).thenReturn(Optional.of(details));
        when(valueOperations.get(argThat(arg -> arg != null && ((String)arg).startsWith("otp_fail:")))).thenReturn("100"); // match maxFailedVerify
        Exception ex = assertThrows(InvalidOtpException.class, () -> redisOtpService.verifyOtp("user", "123", "1234"));
        assertEquals("Too many invalid OTP attempts", ex.getMessage());
    }

    @Test
    void verifyOtp_notFound() {
        when(otpDetailsRepository.findByUsernameAndMobileNumber(anyString(), anyString())).thenReturn(Optional.empty());
        when(valueOperations.get(any())).thenReturn("1234"); // avoid NPE
        Exception ex = assertThrows(InvalidOtpException.class, () -> redisOtpService.verifyOtp("user", "123", "1234"));
        assertEquals("OTP not found", ex.getMessage());
    }
}
