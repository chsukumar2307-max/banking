package com.bank.impl;
import com.bank.dto.response.CheckCustomerResponse;
import com.bank.dto.response.LoginResponse;
import com.bank.dto.response.MessageResponse;
import com.bank.entity.Customer;
import com.bank.enums.CustomerStatus;
import com.bank.exception.UnauthorizedAccessException;
import com.bank.repository.CustomerRepository;
import com.bank.util.JwtUtil;
import com.bank.service.OtpService;
import com.bank.service.RefreshTokenService;
import com.bank.service.impl.AccessTokenBlacklistService;
import com.bank.service.impl.AuthServiceImpl;
import com.bank.util.AccountNumberGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private OtpService otpService;
    @Mock
    private AccountNumberGenerator accountNumberGenerator;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AccessTokenBlacklistService accessTokenBlacklistService;
    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    void checkCustomer_exists() {
        when(customerRepository.existsByUsernameAndMobileNumber(anyString(), anyString())).thenReturn(true);
        CheckCustomerResponse response = authServiceImpl.checkCustomer("user", "123");
        assertTrue(response.isExists());
        assertEquals("Customer exists. You can login.", response.getMessage());
    }

    @Test
    void checkCustomer_notExists() {
        when(customerRepository.existsByUsernameAndMobileNumber(anyString(), anyString())).thenReturn(false);
        CheckCustomerResponse response = authServiceImpl.checkCustomer("user", "123");
        assertFalse(response.isExists());
        assertEquals("Customer not found. Please complete OTP verification to create account.", response.getMessage());
    }

    @Test
    void generateOtp_delegatesToOtpService() {
        doNothing().when(otpService).generateOtp(anyString(), anyString());
        authServiceImpl.generateOtp("user", "123");
        verify(otpService, times(1)).generateOtp("user", "123");
    }

    @Test
    void verifyOtpAndCreateAccount_createsNewCustomer() {
        when(customerRepository.existsByUsernameAndMobileNumber(anyString(), anyString())).thenReturn(false);
        when(customerRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountNumberGenerator.generate()).thenReturn("ACC123");
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        doNothing().when(otpService).verifyOtp(anyString(), anyString(), anyString());
        Customer customer = Customer.builder()
                .username("user").mobileNumber("123").password("encoded")
                .accountNumber("ACC123").balance(BigDecimal.ZERO).status(CustomerStatus.ACTIVE).build();
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        assertDoesNotThrow(() -> authServiceImpl.verifyOtpAndCreateAccount("user", "123", "otp", "pass"));
    }

    @Test
    void verifyOtpAndCreateAccount_existingCustomerAfterOtp_throwsException() {
        when(customerRepository.existsByUsernameAndMobileNumber(anyString(), anyString())).thenReturn(true);
        doNothing().when(otpService).verifyOtp(anyString(), anyString(), anyString());
        assertThrows(IllegalStateException.class, () -> authServiceImpl.verifyOtpAndCreateAccount("user", "1234567890", "otp", "password"));
    }

    @Test
    void verifyOtpAndCreateAccount_nullPassword() {
        when(customerRepository.existsByUsernameAndMobileNumber(anyString(), anyString())).thenReturn(false);
        doNothing().when(otpService).verifyOtp(anyString(), anyString(), anyString());
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                authServiceImpl.verifyOtpAndCreateAccount("user", "123", "otp", null));
        assertEquals("Password is required", ex.getMessage());
    }

    @Test
    void verifyOtpAndCreateAccount_accountNumberGenerationFails() {
        when(customerRepository.existsByUsernameAndMobileNumber(anyString(), anyString())).thenReturn(false);
        when(accountNumberGenerator.generate()).thenReturn("ACC123");
        when(customerRepository.existsByAccountNumber(anyString())).thenReturn(true);
        doNothing().when(otpService).verifyOtp(anyString(), anyString(), anyString());
        Exception ex = assertThrows(IllegalStateException.class, () ->
                authServiceImpl.verifyOtpAndCreateAccount("user", "123", "otp", "pass"));
        assertEquals("Account number generation failed", ex.getMessage());
    }

    @Test
    void login_success() {
        when(customerRepository.findByUsername(anyString())).thenReturn(Optional.of(Customer.builder().username("user").accountNumber("ACC123").build()));
        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh");
        doNothing().when(refreshTokenService).storeRefreshToken(anyString(), anyString(), anyLong());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        LoginResponse response = authServiceImpl.login("user", "pass");
        assertEquals("token", response.getToken());
        assertEquals("refresh", response.getRefreshToken());
        assertEquals("user", response.getUsername());
        assertEquals("ACC123", response.getAccountNumber());
    }

    @Test
    void login_invalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid username or password"));
        Exception ex = assertThrows(UnauthorizedAccessException.class, () -> authServiceImpl.login("user", "pass"));
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void login_userNotFound() {
        when(customerRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        Exception ex = assertThrows(UnauthorizedAccessException.class, () -> authServiceImpl.login("user", "pass"));
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void logout_invalidRefreshToken() {
        when(jwtUtil.isRefreshTokenInvalid(anyString())).thenReturn(true);
        MessageResponse response = authServiceImpl.logout("refresh", "access");
        assertEquals("Invalid refresh token", response.getMessage());
    }

    @Test
    void logout_refreshTokenNotFoundOrMismatched() {
        when(jwtUtil.isRefreshTokenInvalid(anyString())).thenReturn(false);
        when(jwtUtil.extractUsername(anyString())).thenReturn("user");
        when(refreshTokenService.getRefreshToken(anyString())).thenReturn(null);
        MessageResponse response = authServiceImpl.logout("refresh", "access");
        assertEquals("Refresh token not found or mismatched", response.getMessage());
    }

    @Test
    void logout_success() {
        when(jwtUtil.isRefreshTokenInvalid(anyString())).thenReturn(false);
        when(jwtUtil.extractUsername(anyString())).thenReturn("user");
        when(refreshTokenService.getRefreshToken(anyString())).thenReturn("refresh");
        doNothing().when(refreshTokenService).deleteRefreshToken(anyString());
        doNothing().when(accessTokenBlacklistService).blacklistToken(anyString());
        MessageResponse response = authServiceImpl.logout("refresh", "access");
        assertEquals("Logout successful. Tokens expired.", response.getMessage());
    }
}
