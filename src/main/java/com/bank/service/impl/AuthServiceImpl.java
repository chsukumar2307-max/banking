package com.bank.service.impl;
import com.bank.dto.response.CheckCustomerResponse;
import com.bank.dto.response.LoginResponse;
import com.bank.dto.response.MessageResponse;
import com.bank.entity.Customer;
import com.bank.enums.CustomerStatus;
import com.bank.exception.UnauthorizedAccessException;
import com.bank.repository.CustomerRepository;
import com.bank.util.JwtUtil;
import com.bank.service.AuthService;
import com.bank.service.OtpService;
import com.bank.service.RefreshTokenService;
import com.bank.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

/**
 * Service for authentication, registration, and token management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customerRepository;
    private final OtpService otpService;
    private final AccountNumberGenerator accountNumberGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenBlacklistService accessTokenBlacklistService;

    @Value("${app.refresh-token.expiry-seconds:604800}") // 7 days default
    private long refreshTokenExpirySeconds;

    @Value("${app.account.max-account-generation-attempts:10}")
    private int maxAccountGenerationAttempts;
    private static final int DEFAULT_MAX_ACCOUNT_GENERATION_ATTEMPTS = 10;

    /**
     * Check if a customer exists by username and mobile number.
     */

        @Override
        public CheckCustomerResponse checkCustomer(String username, String mobileNumber) {
            if (isBlank(username) || isBlank(mobileNumber)) {
                log.warn("Username or mobile number is blank in checkCustomer.");
                return new CheckCustomerResponse(false, "Username or mobile number is blank.");
            }
            boolean exists = customerRepository.existsByUsernameAndMobileNumber(username, mobileNumber);
            if (exists) {
                return new CheckCustomerResponse(true, "Customer exists. You can login.");
            }
            return new CheckCustomerResponse(false, "Customer not found. Please complete OTP verification to create account.");
        }

        @Override
        public void generateOtp(String username, String mobileNumber) {
            log.info("Generating OTP for username={} mobile={}", username, mobileNumber);
            otpService.generateOtp(username, mobileNumber);
        }

        @Override
        @Transactional
        public void verifyOtpAndCreateAccount(String username, String mobileNumber, String otp, String rawPassword) {
            otpService.verifyOtp(username, mobileNumber, otp);
            if (customerRepository.existsByUsernameAndMobileNumber(username, mobileNumber)) {
                log.info("Customer already exists after OTP verification: {}", username);
                throw new IllegalStateException("Customer already exists after OTP verification");
            }
            if (isBlank(rawPassword)) {
                log.warn("Password is required for account creation: username={}", username);
                throw new IllegalArgumentException("Password is required");
            }
            String accountNumber = generateUniqueAccountNumber();
            Customer customer = Customer.builder()
                    .username(username)
                    .mobileNumber(mobileNumber)
                    .password(passwordEncoder.encode(rawPassword))
                    .accountNumber(accountNumber)
                    .balance(BigDecimal.ZERO)
                    .status(CustomerStatus.ACTIVE)
                    .build();
            customerRepository.save(customer);
            log.info("Customer created successfully username={} accountNumber={}", username, accountNumber);
        }

        @Override
        public LoginResponse login(String username, String password) {
            if (isBlank(username) || isBlank(password)) {
                log.warn("Login attempt with blank username or password");
                throw new UnauthorizedAccessException("Username or password is blank");
            }
            authenticate(username, password);
            Customer customer = customerRepository.findByUsername(username)
                    .orElseThrow(() -> new UnauthorizedAccessException("Invalid username or password"));
            String token = jwtUtil.generateToken(
                    username,
                    Map.of("accountNumber", customer.getAccountNumber())
            );
            String refreshToken = jwtUtil.generateRefreshToken(username);
            refreshTokenService.storeRefreshToken(username, refreshToken, refreshTokenExpirySeconds);
            return new LoginResponse(token, refreshToken, customer.getUsername(), customer.getAccountNumber());
        }

        @Override
        public MessageResponse logout(String refreshToken, String accessToken) {
            if (isBlank(refreshToken)) {
                log.warn("Logout attempt with blank refresh token");
                return new MessageResponse("Invalid refresh token");
            }
            if (jwtUtil.isRefreshTokenInvalid(refreshToken)) {
                log.warn("Logout attempt with invalid refresh token");
                return new MessageResponse("Invalid refresh token");
            }
            String username = jwtUtil.extractUsername(refreshToken);
            String storedToken = refreshTokenService.getRefreshToken(username);
            if (storedToken == null || !storedToken.equals(refreshToken)) {
                log.warn("Refresh token not found or mismatched for username={}", username);
                return new MessageResponse("Refresh token not found or mismatched");
            }
            refreshTokenService.deleteRefreshToken(username);
            if (!isBlank(accessToken)) {
                accessTokenBlacklistService.blacklistToken(accessToken);
            }
            log.info("Logout successful for username={}", username);
            return new MessageResponse("Logout successful. Tokens expired.");
        }

        // --- Private helpers for code smell reduction ---

        private boolean isBlank(String value) {
            return !StringUtils.hasText(value);
        }

        private void authenticate(String username, String password) {
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(username, password));
                log.info("Login attempt for username {}", username);
            } catch (AuthenticationException e) {
                log.warn("Authentication failed for username {}: {}", username, e.getMessage());
                throw new UnauthorizedAccessException("Invalid username or password");
            }
        }

        private String generateUniqueAccountNumber() {
            int attempts = 0;
            String accountNumber;
            int maxAttempts = maxAccountGenerationAttempts > 0 ? maxAccountGenerationAttempts : DEFAULT_MAX_ACCOUNT_GENERATION_ATTEMPTS;
            do {
                accountNumber = accountNumberGenerator.generate();
                attempts++;
            } while (customerRepository.existsByAccountNumber(accountNumber) && attempts < maxAttempts);
            if (customerRepository.existsByAccountNumber(accountNumber)) {
                log.error("Account number generation failed after {} attempts", attempts);
                throw new IllegalStateException("Account number generation failed");
            }
            return accountNumber;
        }
    }