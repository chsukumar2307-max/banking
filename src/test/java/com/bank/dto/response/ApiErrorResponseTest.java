package com.bank.dto.response;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class ApiErrorResponseTest {
    @Test
    void builderAndGettersShouldWork() {
        Instant now = Instant.now();
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(now)
                .status(404)
                .error("Not Found")
                .message("Resource not found")
                .path("/api/resource")
                .build();

        assertEquals(now, response.getTimestamp());
        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("Resource not found", response.getMessage());
        assertEquals("/api/resource", response.getPath());
    }

    @Test
    void equalsAndHashCodeShouldWork() {
        Instant now = Instant.now();
        ApiErrorResponse r1 = ApiErrorResponse.builder()
                .timestamp(now)
                .status(500)
                .error("Internal Error")
                .message("Something went wrong")
                .path("/api/error")
                .build();
        ApiErrorResponse r2 = ApiErrorResponse.builder()
                .timestamp(now)
                .status(500)
                .error("Internal Error")
                .message("Something went wrong")
                .path("/api/error")
                .build();
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void toStringShouldContainFields() {
        Instant now = Instant.now();
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(now)
                .status(400)
                .error("Bad Request")
                .message("Invalid input")
                .path("/api/test")
                .build();
        String str = response.toString();
        assertTrue(str.contains("400"));
        assertTrue(str.contains("Bad Request"));
        assertTrue(str.contains("Invalid input"));
        assertTrue(str.contains("/api/test"));
    }
}

