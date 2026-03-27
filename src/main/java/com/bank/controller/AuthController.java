package com.bank.controller;

import com.bank.dto.request.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.bank.service.AuthService;
import com.bank.dto.response.LoginResponse;
import com.bank.dto.response.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bank.dto.response.CheckCustomerResponse;
import com.bank.util.JwtUtil;
import com.bank.service.RefreshTokenService;
import org.springframework.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private final AuthService authService;
	private final JwtUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;

	private static final String INVALID_TOKEN_MSG = "Invalid refresh token";
	private static final String NOT_FOUND_MSG = "Refresh token not found or mismatched";
	private static final String LOGOUT_SUCCESS_MSG = "Logout successful. Tokens expired.";
	
	@PostMapping(value = "/check-customer", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CheckCustomerResponse> checkCustomer(@Valid @RequestBody CheckCustomerRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getMobileNumber())) {
            CheckCustomerResponse resp = new CheckCustomerResponse(false, "Username or mobile number is blank.");
            log.info("checkCustomer response: {}", resp);
            return ResponseEntity.badRequest().body(resp);
        }
        CheckCustomerResponse resp = authService.checkCustomer(request.getUsername(), request.getMobileNumber());
        log.info("checkCustomer response: {}", resp);
        return ResponseEntity.ok(resp);
	}

	@PostMapping(value = "/generate-otp", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> generateOtp(@Valid @RequestBody GenerateOtpRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getMobileNumber())) {
            MessageResponse resp = new MessageResponse("Username or mobile number is blank.");
            log.info("generateOtp response: {}", resp);
            return ResponseEntity.badRequest().body(resp);
        }
        authService.generateOtp(request.getUsername(), request.getMobileNumber());
        MessageResponse resp = new MessageResponse("OTP sent successfully");
        log.info("generateOtp response: {}", resp);
        return ResponseEntity.ok(resp);
    }

	@PostMapping(value = "/verify-otp", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getMobileNumber()) || isBlank(request.getOtp()) || isBlank(request.getPassword())) {
            MessageResponse resp = new MessageResponse("All fields are required for OTP verification.");
            log.info("verifyOtp response: {}", resp);
            return ResponseEntity.badRequest().body(resp);
        }
        authService.verifyOtpAndCreateAccount(
                request.getUsername(),
                request.getMobileNumber(),
                request.getOtp(),
                request.getPassword()
        );
        MessageResponse resp = new MessageResponse("OTP verified and customer account created");
        log.info("verifyOtp response: {}", resp);
        return ResponseEntity.ok(resp);
    }

	@PostMapping(value = "/login", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            log.info("login response: bad request");
            return ResponseEntity.badRequest().build();
        }
        LoginResponse resp = authService.login(request.getUsername(), request.getPassword());
        log.info("login response: {}", resp);
        return ResponseEntity.ok(resp);
	}

	@PostMapping(value = "/refresh-token", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
		String refreshToken = request != null ? request.getRefreshToken() : null;
		if (isBlank(refreshToken) || jwtUtil.isRefreshTokenInvalid(refreshToken)) {
			log.info("refreshToken response: unauthorized");
			return ResponseEntity.status(401).build();
		}
		String username = jwtUtil.extractUsername(refreshToken);
		String storedToken = refreshTokenService.getRefreshToken(username);
		if (storedToken == null || !storedToken.equals(refreshToken)) {
			log.info("refreshToken response: unauthorized");
			return ResponseEntity.status(401).build();
		}
		String newAccessToken = jwtUtil.generateToken(username, Map.of());
		LoginResponse resp = new LoginResponse(newAccessToken, refreshToken, username, null);
		log.info("refreshToken response: {}", resp);
		return ResponseEntity.ok(resp);
	}

	@PostMapping(value = "/logout", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                  @RequestBody RefreshTokenRequest request) {
        String accessToken = extractAccessToken(authorization);
        String refreshToken = request != null ? request.getRefreshToken() : null;
        MessageResponse response = authService.logout(refreshToken, accessToken);
        log.info("logout response: {}", response);
        if (response.getMessage().toLowerCase().contains("invalid") || response.getMessage().toLowerCase().contains("not found")) {
            return ResponseEntity.status(401).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // Change from private to package-private for testability
    boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    String extractAccessToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
