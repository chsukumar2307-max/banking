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
import com.bank.security.JwtUtil;
import com.bank.service.RefreshTokenService;
import org.springframework.http.HttpHeaders;
import java.util.Map;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;

	@PostMapping(value = "/check-customer", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CheckCustomerResponse> checkCustomer(@Valid @RequestBody CheckCustomerRequest request) {

        return ResponseEntity.ok(authService.checkCustomer(request.getUsername(), request.getMobileNumber()));
	}

	@PostMapping(value = "/generate-otp", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> generateOtp(@Valid @RequestBody GenerateOtpRequest request) {
		authService.generateOtp(request.getUsername(), request.getMobileNumber());

        return ResponseEntity.ok(new MessageResponse("OTP sent successfully"));
	}

	@PostMapping(value = "/verify-otp", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
		authService.verifyOtpAndCreateAccount(
				request.getUsername(),
				request.getMobileNumber(),
				request.getOtp(),
				request.getPassword()
		);

        return ResponseEntity.ok(new MessageResponse("OTP verified and customer account created"));
	}

	@PostMapping(value = "/login", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request.getUsername(), request.getPassword()));

	}

	@PostMapping(value = "/refresh-token", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
		String refreshToken = request.getRefreshToken();
		if (jwtUtil.isRefreshTokenInvalid(refreshToken)) {
			return ResponseEntity.status(401).build();
		}
		String username = jwtUtil.extractUsername(refreshToken);
		String storedToken = refreshTokenService.getRefreshToken(username);
		if (storedToken == null || !storedToken.equals(refreshToken)) {
			return ResponseEntity.status(401).build();
		}
		String newAccessToken = jwtUtil.generateToken(username, Map.of());
		return ResponseEntity.ok(new LoginResponse(newAccessToken, refreshToken, username, null));
	}

	@PostMapping(value = "/logout", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                  @RequestBody RefreshTokenRequest request) {
        String accessToken = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }
        MessageResponse response = authService.logout(request.getRefreshToken(), accessToken);
        if (response.getMessage().toLowerCase().contains("invalid") || response.getMessage().toLowerCase().contains("not found")) {
            return ResponseEntity.status(401).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
