package com.bank.security;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.junit.jupiter.api.Assertions.*;
import com.bank.BankingBackendApplication;

/**
 * Test for SecurityConfig to ensure all security-related beans are present in the application context.
 * This test is sufficient for configuration classes as per SonarQube and Spring Boot best practices.
 */
@SpringBootTest(classes = BankingBackendApplication.class)
class SecurityConfigTest {
	@Autowired
	@SuppressWarnings("unused") // Required for bean lookup in test
	private ApplicationContext context;

	@MockBean
	@SuppressWarnings("unused") // Required for context loading
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	@MockBean
	@SuppressWarnings("unused") // Required for context loading
	private CustomerUserDetailsService customerUserDetailsService;

	@Test
	@DisplayName("SecurityConfig: All required security beans are present in context")
	void securityBeans_arePresent() {
		assertAll(
			() -> assertNotNull(context.getBean(SecurityFilterChain.class), "SecurityFilterChain bean should be present"),
			() -> assertNotNull(context.getBean(PasswordEncoder.class), "PasswordEncoder bean should be present"),
			() -> assertNotNull(context.getBean(DaoAuthenticationProvider.class), "DaoAuthenticationProvider bean should be present"),
			() -> assertNotNull(context.getBean(AuthenticationManager.class), "AuthenticationManager bean should be present")
		);
	}
}
