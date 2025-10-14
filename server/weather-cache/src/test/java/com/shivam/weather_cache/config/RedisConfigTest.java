package com.shivam.weather_cache.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisConfigTest {

    private RedisConfig redisConfig;

    @BeforeEach
    void setUp() throws Exception {
        redisConfig = new RedisConfig();

        // Use reflection to set private fields
        setField(redisConfig, "host", "localhost");
        setField(redisConfig, "port", 6379);
        setField(redisConfig, "timeout", 5L);
        setField(redisConfig, "username", "");
        setField(redisConfig, "password", "");
        setField(redisConfig, "maxPoolActive", 10);
        setField(redisConfig, "minPoolIdle", 1);
        setField(redisConfig, "maxPoolIdle", 5);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = RedisConfig.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testRedisTemplateCreation() {
        // Mock LettuceConnectionFactory
        LettuceConnectionFactory factory = mock(LettuceConnectionFactory.class);

        RedisTemplate<String, Object> template = redisConfig.redisTemplate(factory);

        assertNotNull(template);
        assertEquals(factory, template.getConnectionFactory());
        assertNotNull(template.getKeySerializer());
        assertNotNull(template.getValueSerializer());
    }

    @Test
    void testRedisConnectionFactoryCreation() {
        // Mocking real connection not required; just test that method returns non-null
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory();
        assertNotNull(factory);
    }
}
