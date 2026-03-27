package com.bank.integration;
import com.bank.dto.request.*;
import com.bank.dto.response.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest {
    private static MySQLContainer<?> mysql;
    private static GenericContainer<?> redis;

    @LocalServerPort
    @SuppressWarnings("unused")
    private int port;

    private static final RestTemplate restTemplate = new RestTemplate();

    @BeforeAll
    static void setUpContainers() {
        try {
            mysql = new MySQLContainer<>("mysql:8.3.0")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");
            redis = new GenericContainer<>("redis:7").withExposedPorts(6379);
            mysql.start();
            redis.start();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Docker is not available or containers could not be started: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDownContainers() {
        if (mysql != null) mysql.stop();
        if (redis != null) redis.stop();
    }

    @DynamicPropertySource
    @SuppressWarnings("unused")
    static void registerProperties(DynamicPropertyRegistry registry) {
        if (mysql != null && mysql.isRunning() && redis != null && redis.isRunning()) {
            registry.add("spring.datasource.url", mysql::getJdbcUrl);
            registry.add("spring.datasource.username", mysql::getUsername);
            registry.add("spring.datasource.password", mysql::getPassword);
            registry.add("spring.data.redis.host", redis::getHost);
            registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        }
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @Order(1)
    void checkCustomer_and_generateOtp_and_verifyOtp_flow() {
        // 1. Check customer (should not exist)
        CheckCustomerRequest checkReq = new CheckCustomerRequest();
        checkReq.setUsername("testuser");
        checkReq.setMobileNumber("+123456789012");
        ResponseEntity<CheckCustomerResponse> checkResp = restTemplate.postForEntity(url("/api/auth/check-customer"), checkReq, CheckCustomerResponse.class);
        assertEquals(HttpStatus.OK, checkResp.getStatusCode());
        assertNotNull(checkResp.getBody());
        assertFalse(checkResp.getBody().isExists());

        // 2. Generate OTP
        GenerateOtpRequest otpReq = new GenerateOtpRequest();
        otpReq.setUsername("testuser");
        otpReq.setMobileNumber("+123456789012");
        ResponseEntity<MessageResponse> otpResp = restTemplate.postForEntity(url("/api/auth/generate-otp"), otpReq, MessageResponse.class);
        assertEquals(HttpStatus.OK, otpResp.getStatusCode());
        assertNotNull(otpResp.getBody());
        assertTrue(otpResp.getBody().getMessage().toLowerCase().contains("otp"));

        // 3. Verify OTP and create account (simulate OTP as '123456')
        VerifyOtpRequest verifyReq = new VerifyOtpRequest();
        verifyReq.setUsername("testuser");
        verifyReq.setMobileNumber("+123456789012");
        verifyReq.setOtp("123456");
        verifyReq.setPassword("password");
        ResponseEntity<MessageResponse> verifyResp = restTemplate.postForEntity(url("/api/auth/verify-otp"), verifyReq, MessageResponse.class);
        assertEquals(HttpStatus.OK, verifyResp.getStatusCode());
        assertNotNull(verifyResp.getBody());
        assertTrue(verifyResp.getBody().getMessage().toLowerCase().contains("verified"));
    }

    @Test
    @Order(2)
    void login_and_refreshToken_and_logout_flow() {
        // 1. Login
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("testuser");
        loginReq.setPassword("password");
        ResponseEntity<LoginResponse> loginResp = restTemplate.postForEntity(url("/api/auth/login"), loginReq, LoginResponse.class);
        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        assertNotNull(loginResp.getBody());
        assertNotNull(loginResp.getBody().getToken());
        assertNotNull(loginResp.getBody().getRefreshToken());
        String refreshToken = loginResp.getBody().getRefreshToken();
        String accessToken = loginResp.getBody().getToken();

        // 2. Refresh token
        RefreshTokenRequest refreshReq = new RefreshTokenRequest();
        refreshReq.setRefreshToken(refreshToken);
        ResponseEntity<LoginResponse> refreshResp = restTemplate.postForEntity(url("/api/auth/refresh-token"), refreshReq, LoginResponse.class);
        assertEquals(HttpStatus.OK, refreshResp.getStatusCode());
        assertNotNull(refreshResp.getBody());
        assertNotNull(refreshResp.getBody().getToken());

        // 3. Logout
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        org.springframework.http.HttpEntity<RefreshTokenRequest> logoutEntity = new org.springframework.http.HttpEntity<>(refreshReq, headers);
        ResponseEntity<MessageResponse> logoutResp = restTemplate.postForEntity(url("/api/auth/logout"), logoutEntity, MessageResponse.class);
        assertEquals(HttpStatus.OK, logoutResp.getStatusCode());
        assertNotNull(logoutResp.getBody());
        assertTrue(logoutResp.getBody().getMessage().toLowerCase().contains("logout"));
    }
}
