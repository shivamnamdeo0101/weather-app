package com.shivam.weather_cache.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.*;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GenericRedisServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @Mock
    private HashOperations<String, Object, Object> hashOps;

    @InjectMocks
    private GenericRedisServiceImpl redisService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        // Mock execute(...) to run the SessionCallback immediately
        when(redisTemplate.execute(any(SessionCallback.class))).thenAnswer(invocation -> {
            SessionCallback<?> callback = invocation.getArgument(0);
            return callback.execute(redisTemplate);
        });

        when(redisTemplate.expire(anyString(), any())).thenReturn(true);
    }

    @Test
    void testSaveWithMeta() {
        redisService.saveWithMeta("weather:Berlin", Collections.singletonMap("temp", 25), true, 2L);

        verify(valueOps).set(eq("weather:Berlin:data"), any());
        verify(hashOps).put(eq("weather:Berlin:meta"), eq("hits"), eq(2L));
        verify(hashOps).put(eq("weather:Berlin:meta"), eq("lastAccess"), any());
        verify(hashOps).put(eq("weather:Berlin:meta"), eq("lastRefresh"), any());
        verify(redisTemplate, times(2)).expire(anyString(), any());
    }

    @Test
    void testGetAndUpdateMeta_cacheHit() {
        when(valueOps.get("weather:Berlin:data")).thenReturn(Collections.singletonMap("temp", 25));

        Object result = redisService.getAndUpdateMeta("weather:Berlin");

        assertNotNull(result);
        assertTrue(result instanceof Map);

        verify(hashOps).increment(eq("weather:Berlin:meta"), eq("hits"), eq(1L));
        verify(hashOps).put(eq("weather:Berlin:meta"), eq("lastAccess"), any());
    }

    @Test
    void testGetAndUpdateMeta_cacheMiss() {
        when(valueOps.get("weather:Paris:data")).thenReturn(null);

        Object result = redisService.getAndUpdateMeta("weather:Paris");
        assertNull(result);

        verify(hashOps, never()).increment(anyString(), any(), anyLong());
        verify(hashOps, never()).put(anyString(), any(), any());
    }

    @Test
    void testGetMeta_returnsEmptyIfNone() {
        when(hashOps.entries("weather:Berlin:meta")).thenReturn(Collections.emptyMap());

        Map<Object, Object> meta = redisService.getMeta("weather:Berlin");
        assertNotNull(meta);
        assertTrue(meta.isEmpty());
    }

    @Test
    void testGetMeta_returnsMap() {
        Map<Object, Object> map = Map.of("hits", 1L, "lastAccess", Instant.now().toEpochMilli());
        when(hashOps.entries("weather:Berlin:meta")).thenReturn(map);

        Map<Object, Object> meta = redisService.getMeta("weather:Berlin");
        assertEquals(2, meta.size());
        assertEquals(1L, meta.get("hits"));
    }

    @Test
    void testGetAllKeys() {
        Set<String> keys = Set.of("weather:Berlin:data", "weather:Paris:data");
        when(redisTemplate.keys("weather:*")).thenReturn(keys);

        Set<String> result = redisService.getAllKeys("weather:*");
        assertEquals(2, result.size());
        assertTrue(result.contains("weather:Berlin:data"));
    }

    @Test
    void testDeleteKey() {
        redisService.deleteKey("weather:Berlin");

        verify(redisTemplate).delete("weather:Berlin:data");
        verify(redisTemplate).delete("weather:Berlin:meta");
    }
}
