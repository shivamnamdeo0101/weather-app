package com.shivam.weather_cache.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@Slf4j
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.username}")
    private String username;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.timeout}")
    private long timeout;

    @Value("${spring.redis.lettuce.pool.max-active}")
    private int maxPoolActive;

    @Value("${spring.redis.lettuce.pool.min-idle}")
    private int minPoolIdle;

    @Value("${spring.redis.lettuce.pool.max-idle}")
    private int maxPoolIdle;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        try {
            log.info("Initializing LettuceConnectionFactory → host={}, port={}, timeout={}s", host, port, timeout);

            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(host);
            config.setUsername(username);
            config.setPort(port);
            config.setPassword(password);

            // 🔹 Pool config
            GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
            poolConfig.setMaxTotal(maxPoolActive);
            poolConfig.setMaxIdle(maxPoolIdle);
            poolConfig.setMinIdle(minPoolIdle);

            LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                    .commandTimeout(Duration.ofSeconds(timeout))
                    .poolConfig(poolConfig)
                    .clientResources(DefaultClientResources.create())
                    .clientOptions(ClientOptions.builder()
                            .autoReconnect(true)
                            .pingBeforeActivateConnection(true)
                            .build())
                    .build();



            LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig);
            factory.afterPropertiesSet();
            log.info("Successfully connected to Redis at {}:{}", host, port);

            return factory;

        } catch (RedisConnectionException | RedisConnectionFailureException ex) {
            log.error("Failed to connect to Redis at {}:{}. Check host/port/credentials.", host, port, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while initializing Redis connection", ex);
            throw ex;
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        try {
            template.setConnectionFactory(connectionFactory);

            // Keys as String
            template.setKeySerializer(new StringRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());

            // Values as JSON (generic, no DTO needed)
            template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
            template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

            template.afterPropertiesSet();
            log.info("RedisTemplate initialized successfully with JSON serializer");

        } catch (DataAccessException ex) {
            log.error("RedisTemplate initialization failed due to DataAccessException", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while setting up RedisTemplate", ex);
            throw ex;
        }
        return template;
    }
}
