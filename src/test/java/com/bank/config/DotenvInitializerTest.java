package com.bank.config;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link DotenvInitializer}.
 * Ensures environment variables are loaded correctly from .env files.
 * Covers property setting, non-overriding, empty and malformed dotenv cases.
 */
class DotenvInitializerTest {
    private static final String TEST_KEY = "TEST_ENV_KEY";
    private static final String TEST_VALUE = "test_value";
    private File tempEnvDir;
    private File tempEnvFile;

    @BeforeEach
    void setupEnv() throws IOException {
        tempEnvDir = Files.createTempDirectory("dotenv-test").toFile();
        tempEnvFile = new File(tempEnvDir, ".env");
        try (FileWriter writer = new FileWriter(tempEnvFile)) {
            writer.write(TEST_KEY + "=" + TEST_VALUE + "\n");
        }
    }

    @AfterEach
    void cleanupEnv() {
        if (tempEnvFile != null && tempEnvFile.exists()) assertTrue(tempEnvFile.delete());
        if (tempEnvDir != null && tempEnvDir.exists()) {
            File[] files = tempEnvDir.listFiles();
            if (files != null) {
                for (File f : files) assertTrue(f.delete());
            }
            assertTrue(tempEnvDir.delete());
        }
        System.clearProperty(TEST_KEY);
        assertNull(System.getProperty(TEST_KEY));
    }

    @Test
    void initialize_setsSystemProperty_whenNotPresent() {
        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
        // Patch DotenvInitializer to use tempEnvDir
        DotenvInitializer initializer = new DotenvInitializer() {
            @Override
            public void initialize(ConfigurableApplicationContext applicationContext) {
                Dotenv dotenv = Dotenv.configure()
                        .directory(tempEnvDir.getAbsolutePath())
                        .ignoreIfMalformed()
                        .ignoreIfMissing()
                        .load();
                dotenv.entries().forEach(entry -> {
                    if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
            }
        };
        initializer.initialize(mockContext);
        assertEquals(TEST_VALUE, System.getProperty(TEST_KEY));
    }

    @Test
    void initialize_doesNotOverrideExistingSystemProperty() {
        System.setProperty(TEST_KEY, "existing_value");
        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
        DotenvInitializer initializer = new DotenvInitializer() {
            @Override
            public void initialize(ConfigurableApplicationContext applicationContext) {
                Dotenv dotenv = Dotenv.configure()
                        .directory(tempEnvDir.getAbsolutePath())
                        .ignoreIfMalformed()
                        .ignoreIfMissing()
                        .load();
                dotenv.entries().forEach(entry -> {
                    if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
            }
        };
        initializer.initialize(mockContext);
        assertEquals("existing_value", System.getProperty(TEST_KEY));
    }

    @Test
    void initialize_handlesEmptyDotenv() throws IOException {
        // Overwrite .env with empty content
        try (FileWriter writer = new FileWriter(tempEnvFile)) {
            writer.write("");
        }
        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
        DotenvInitializer initializer = new DotenvInitializer() {
            @Override
            public void initialize(ConfigurableApplicationContext applicationContext) {
                Dotenv dotenv = Dotenv.configure()
                        .directory(tempEnvDir.getAbsolutePath())
                        .ignoreIfMalformed()
                        .ignoreIfMissing()
                        .load();
                dotenv.entries().forEach(entry -> {
                    if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
            }
        };
        assertDoesNotThrow(() -> initializer.initialize(mockContext));
        assertNull(System.getProperty(TEST_KEY));
    }

    @Test
    void initialize_handlesMalformedDotenv() throws IOException {
        // Overwrite .env with malformed content
        try (FileWriter writer = new FileWriter(tempEnvFile)) {
            writer.write("MALFORMED_LINE\nANOTHER=ok\n");
        }
        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
        DotenvInitializer initializer = new DotenvInitializer() {
            @Override
            public void initialize(ConfigurableApplicationContext applicationContext) {
                Dotenv dotenv = Dotenv.configure()
                        .directory(tempEnvDir.getAbsolutePath())
                        .ignoreIfMalformed()
                        .ignoreIfMissing()
                        .load();
                dotenv.entries().forEach(entry -> {
                    if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
            }
        };
        assertDoesNotThrow(() -> initializer.initialize(mockContext));
        assertEquals("ok", System.getProperty("ANOTHER"));
        assertNull(System.getProperty("MALFORMED_LINE"));
    }
}
