package com.bank.controller;
import com.bank.dto.request.*;
import com.bank.dto.response.*;
import com.bank.service.AuthService;
import com.bank.security.JwtUtil;
import com.bank.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bank.exception.InvalidOtpException;
import com.bank.exception.UnauthorizedAccessException;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SuppressWarnings("unused") // Suppress unused field warnings for Spring Boot test injection
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @SuppressWarnings("unused")
    @Autowired
    private MockMvc mockMvc;
    @SuppressWarnings("unused")
    @MockBean
    private AuthService authService;
    @SuppressWarnings("unused")
    @MockBean
    private JwtUtil jwtUtil;
    @SuppressWarnings("unused")
    @MockBean
    private RefreshTokenService refreshTokenService;
    @SuppressWarnings("unused")
    @MockBean
    private com.bank.security.JwtAuthenticationFilter jwtAuthenticationFilter;
    @SuppressWarnings("unused")
    @MockBean
    private com.bank.security.CustomerUserDetailsService customerUserDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void checkCustomer_success() throws Exception {
        CheckCustomerRequest req = new CheckCustomerRequest();
        req.setUsername("user");
        req.setMobileNumber("123");
        when(authService.checkCustomer(anyString(), anyString()))
            .thenReturn(new CheckCustomerResponse(true, "Customer exists. You can login."));
        mockMvc.perform(post("/api/auth/check-customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.message").value("Customer exists. You can login."));
    }

    /**
     * Test checkCustomer with blank username (validation error).
     */
    @Test
    void checkCustomer_blankUsername_returnsBadRequest() throws Exception {
        CheckCustomerRequest req = new CheckCustomerRequest();
        req.setUsername("");
        req.setMobileNumber("123");
        mockMvc.perform(post("/api/auth/check-customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("must not be blank"));
    }

    @Test
    void generateOtp_success() throws Exception {
        GenerateOtpRequest req = new GenerateOtpRequest();
        req.setUsername("user");
        req.setMobileNumber("+123456789012");
        doNothing().when(authService).generateOtp(anyString(), anyString());
        mockMvc.perform(post("/api/auth/generate-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent successfully"));
    }

    /**
     * Test generateOtp with invalid mobile number (validation error).
     */
    @Test
    void generateOtp_invalidMobile_returnsBadRequest() throws Exception {
        GenerateOtpRequest req = new GenerateOtpRequest();
        req.setUsername("user");
        req.setMobileNumber("123"); // Invalid format
        mockMvc.perform(post("/api/auth/generate-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mobileNumber").value("Mobile number must start with + and contain exactly 12 digits"));
    }

    @Test
    void verifyOtp_success() throws Exception {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setUsername("user");
        req.setMobileNumber("+123456789012");
        req.setOtp("123456");
        req.setPassword("pass");
        doNothing().when(authService).verifyOtpAndCreateAccount(anyString(), anyString(), anyString(), anyString());
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP verified and customer account created"));
    }

    /**
     * Test verifyOtp with missing OTP (validation error).
     */
    @Test
    void verifyOtp_missingOtp_returnsBadRequest() throws Exception {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setUsername("user");
        req.setMobileNumber("+123456789012");
        req.setPassword("pass");
        // OTP not set
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.otp").value("must not be blank"));
    }

    /**
     * Test verifyOtp with invalid OTP (validation error).
     */
    @Test
    void verifyOtp_invalidOtp_returnsBadRequest() throws Exception {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setUsername("user");
        req.setMobileNumber("+123456789012");
        req.setOtp("bad");
        req.setPassword("pass");
        doThrow(new InvalidOtpException("Invalid OTP")).when(authService)
            .verifyOtpAndCreateAccount(anyString(), anyString(), anyString(), anyString());
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid OTP"));
    }

    @Test
    void login_success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("user");
        req.setPassword("pass");
        when(authService.login(anyString(), anyString()))
            .thenReturn(new LoginResponse("token", "refresh", "user", "ACC123"));
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.accountNumber").value("ACC123"));
    }

    /**
     * Test login with blank password (validation error).
     */
    @Test
    void login_blankPassword_returnsBadRequest() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("user");
        req.setPassword("");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("must not be blank"));
    }

    /**
     * Test login with invalid credentials (unauthorized).
     */
    @Test
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("user");
        req.setPassword("wrong");
        when(authService.login(anyString(), anyString()))
            .thenThrow(new UnauthorizedAccessException("Invalid username or password"));
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void refreshToken_success() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh");
        when(jwtUtil.extractUsername(anyString())).thenReturn("user");
        when(refreshTokenService.getRefreshToken(anyString())).thenReturn("refresh");
        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("newToken");
        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("newToken"));
    }

    /**
     * Test refreshToken with mismatched stored token (service returns 401).
     */
    @Test
    void refreshToken_mismatchedStoredToken_returnsUnauthorized() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh");
        when(jwtUtil.extractUsername(anyString())).thenReturn("user");
        when(refreshTokenService.getRefreshToken(anyString())).thenReturn("other");
        doThrow(new IllegalArgumentException("Invalid refresh token")).when(authService).logout(anyString(), anyString());
        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized()); // Accepts default error structure
    }

    @Test
    void refreshToken_invalidToken_returnsUnauthorized() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("badtoken");
        when(jwtUtil.isRefreshTokenInvalid("badtoken")).thenReturn(true);
        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_success() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh");
        when(authService.logout(anyString(), anyString())).thenReturn(new MessageResponse("Logout successful. Tokens expired."));
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful. Tokens expired."));
    }

    @Test
    void logout_invalidToken() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh");
        when(authService.logout(anyString(), anyString())).thenReturn(new MessageResponse("Invalid refresh token"));
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    void logout_noAuthorizationHeader_returnsOk() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh");
        when(authService.logout(eq("refresh"), isNull())).thenReturn(new MessageResponse("Logout successful. Tokens expired."));
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "") // Explicitly set empty header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful. Tokens expired."));
    }

    @Test
    void logout_nonBearerAuthorizationHeader_returnsOk() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh");
        when(authService.logout(eq("refresh"), isNull())).thenReturn(new MessageResponse("Logout successful. Tokens expired."));
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "NotBearer sometoken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful. Tokens expired."));
    }

    @Test
    void refreshToken_storedTokenNull_returnsUnauthorized() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh");
        when(jwtUtil.isRefreshTokenInvalid("refresh")).thenReturn(false);
        when(jwtUtil.extractUsername("refresh")).thenReturn("user");
        when(refreshTokenService.getRefreshToken("user")).thenReturn(null);
        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_messageContainsNotFound_returnsUnauthorized() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh");
        when(authService.logout(anyString(), anyString())).thenReturn(new MessageResponse("Refresh token not found"));
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token not found"));
    }

    // Additional endpoint tests will be added here for full coverage
}
