package com.bank.config;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.junit.jupiter.api.Assertions.*;

class RedisConfigTest {
    @Test
    void redisTemplateBean_isConfiguredCorrectly() {
        RedisConfig config = new RedisConfig();
        RedisConnectionFactory mockFactory = Mockito.mock(RedisConnectionFactory.class);
        RedisTemplate<String, String> template = config.redisTemplate(mockFactory);

        assertNotNull(template);
        assertSame(mockFactory, template.getConnectionFactory());
        assertInstanceOf(StringRedisSerializer.class, template.getKeySerializer());
        assertInstanceOf(StringRedisSerializer.class, template.getValueSerializer());
        assertInstanceOf(StringRedisSerializer.class, template.getHashKeySerializer());
        assertInstanceOf(StringRedisSerializer.class, template.getHashValueSerializer());
        assertInstanceOf(StringRedisSerializer.class, template.getDefaultSerializer());
    }
}
