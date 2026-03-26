package com.bank.integration;
import com.bank.dto.request.DepositRequest;
import com.bank.dto.request.WithdrawRequest;
import com.bank.dto.request.TransferRequest;
import com.bank.dto.response.DepositResponse;
import com.bank.dto.response.WithdrawResponse;
import com.bank.dto.response.TransferResponse;
import com.bank.dto.response.BalanceResponse;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.DockerClientFactory;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountIntegrationTest { private static MySQLContainer<?> mysql;
    private static GenericContainer<?> redis;
    @LocalServerPort
    private int port;
    private static RestTemplate restTemplate;

    @BeforeAll
    static void setup() {
        Assumptions.assumeTrue(isDockerAvailable(), "Docker is not available, skipping integration tests.");
        mysql = new MySQLContainer<>("mysql:8.3.0")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        redis = new GenericContainer<>("redis:7").withExposedPorts(6379);
        mysql.start();
        redis.start();
        restTemplate = new RestTemplate();
    }

    @AfterAll
    static void tearDown() {
        if (mysql != null) mysql.stop();
        if (redis != null) redis.stop();
    }

    private static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception e) {
            return false;
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // This will be set in test methods after containers are started
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @Order(1)
    void deposit_and_balance_flow() {
        DepositRequest deposit = new DepositRequest();
        deposit.setAccountNumber("10001");
        deposit.setAmount(BigDecimal.valueOf(1000));
        ResponseEntity<DepositResponse> resp = restTemplate.postForEntity(url("/api/accounts/deposit"), deposit, DepositResponse.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Deposit successful", resp.getBody().getStatus());
        // Check balance
        ResponseEntity<BalanceResponse> balResp = restTemplate.getForEntity(url("/api/accounts/balance/10001"), BalanceResponse.class);
        assertEquals(HttpStatus.OK, balResp.getStatusCode());
        assertNotNull(balResp.getBody());
        assertEquals(BigDecimal.valueOf(1000), balResp.getBody().getBalance());
    }

    @Test
    @Order(2)
    void withdraw_and_transfer_flow() {
        // Withdraw
        WithdrawRequest withdraw = new WithdrawRequest();
        withdraw.setAccountNumber("10001");
        withdraw.setAmount(BigDecimal.valueOf(200));
        ResponseEntity<WithdrawResponse> wResp = restTemplate.postForEntity(url("/api/accounts/withdraw"), withdraw, WithdrawResponse.class);
        assertEquals(HttpStatus.OK, wResp.getStatusCode());
        assertNotNull(wResp.getBody());
        assertEquals("Withdrawal successful", wResp.getBody().getStatus());
        // Transfer
        TransferRequest transfer = new TransferRequest();
        transfer.setFromAccount("10001");
        transfer.setToAccount("10002");
        transfer.setAmount(BigDecimal.valueOf(100));
        ResponseEntity<TransferResponse> tResp = restTemplate.postForEntity(url("/api/accounts/transfer"), transfer, TransferResponse.class);
        assertEquals(HttpStatus.OK, tResp.getStatusCode());
        assertNotNull(tResp.getBody());
        assertEquals("Transfer successful", tResp.getBody().getStatus());
    }
}
