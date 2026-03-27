package com.bank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import static org.junit.jupiter.api.Assertions.*;
import com.bank.controller.AccountController;
import com.bank.service.AccountService;
import com.bank.controller.AuthController;
import com.bank.util.JwtAuthenticationFilter;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BankingBackendApplicationTests {
    @Autowired
    @SuppressWarnings("unused") // For SonarQube: context is used in assertions
    private ApplicationContext context;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${local.server.port}")
    private int port;

    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
        assertNotNull(context, "ApplicationContext should be loaded");
    }

    @Test
    void accountControllerBeanExists() {
        // Verifies that AccountController bean is present in the context
        assertNotNull(context.getBean(AccountController.class), "AccountController bean should be present");
    }

    @Test
    void accountServiceBeanExists() {
        // Verifies that AccountService bean is present in the context
        assertNotNull(context.getBean(AccountService.class), "AccountService bean should be present");
    }

    @Test
    void authControllerBeanExists() {
        // Verifies that AuthController bean is present in the context
        assertNotNull(context.getBean(AuthController.class), "AuthController bean should be present");
    }

    @Test
    void jwtAuthenticationFilterBeanExists() {
        // Verifies that JwtAuthenticationFilter bean is present in the context
        assertNotNull(context.getBean(JwtAuthenticationFilter.class), "JwtAuthenticationFilter bean should be present");
    }

    @Test
    void nonExistentBeanThrows() {
        // Verifies that requesting a non-existent bean throws NoSuchBeanDefinitionException
        assertThrows(org.springframework.beans.factory.NoSuchBeanDefinitionException.class,
            () -> context.getBean("nonExistentBean"),
            "Requesting a non-existent bean should throw NoSuchBeanDefinitionException");
    }

    @Test
    void mainClassInstantiable() {
        // Verifies that the main application class can be instantiated
        assertDoesNotThrow(BankingBackendApplication::new, "Main application class should be instantiable");
    }

    @Test
    void balanceEndpointReturnsErrorForDummyAccount() {
        // Integration test: call /api/accounts/balance/{accountNumber} with a dummy account
        String url = "http://localhost:" + port + "/api/accounts/balance/TEST123";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError(),
            "Should return 4xx or 5xx for non-existent account");
    }
}
