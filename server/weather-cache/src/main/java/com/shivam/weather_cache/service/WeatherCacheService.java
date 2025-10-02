package com.shivam.weather_cache.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherCacheService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8080")
            .build();

    private static final Duration CACHE_TTL = Duration.ofSeconds(10);
    private static final Duration INFLIGHT_CACHE_DURATION = Duration.ofSeconds(20);

    private final ConcurrentHashMap<String, Mono<String>> inflight = new ConcurrentHashMap<>();

    public Mono<String> getForecast(String city) {
        final String cacheKey = "weather:forecast:" + city.toLowerCase();

        // Redis read first
        Mono<String> redisReadMono = reactiveRedisTemplate.opsForValue()
                .get(cacheKey)
                .doOnNext(value -> log.info("[CACHE HIT] Key={}", cacheKey))
                .onErrorResume(e -> {
                    log.warn("[REDIS READ WARNING] Key={} Error={}", cacheKey, e.getMessage());
                    return Mono.empty();
                });

        // If Redis empty -> HTTP call
        Mono<String> httpMono = inflight.computeIfAbsent(cacheKey, key -> createHttpRequestMono(city, cacheKey));

        // Return Redis hit immediately, else inflight HTTP Mono
        return redisReadMono.switchIfEmpty(httpMono);
    }

    private Mono<String> createHttpRequestMono(String city, String cacheKey) {
        log.info("[INFLIGHT] Creating new request for key={}", cacheKey);

        Mono<String> httpMono = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/weather-svc/forecast")
                        .queryParam("city", city)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5)) // fail fast
                .doOnSubscribe(s -> log.info("[HTTP] Request started for city={}", city))
                .doOnSuccess(resp -> log.info("[HTTP] Request succeeded for city={}", city))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                        log.warn("[HTTP 429] Too Many Requests for city={}", city);
                        return Mono.error(new RuntimeException("Rate limited by Weather API"));
                    }
                    log.error("[HTTP ERROR] city={} Status={} Message={}", city, ex.getStatusCode(), ex.getMessage());
                    return Mono.empty();
                })
                .onErrorResume(ex -> {
                    log.error("[HTTP GENERIC ERROR] city={} Error={}", city, ex.getMessage());
                    return Mono.empty();
                })
                // Fire-and-forget async cache
                .doOnNext(resp -> reactiveRedisTemplate.opsForValue()
                        .set(cacheKey, resp, CACHE_TTL)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(e -> {
                            log.warn("[REDIS WRITE WARNING] Key={} Error={}", cacheKey, e.getMessage());
                            return Mono.empty();
                        })
                        .subscribe()
                );

        // Share result for same-city inflight requests only
        return httpMono.cache(INFLIGHT_CACHE_DURATION)
                .doFinally(signal -> {
                    inflight.remove(cacheKey);
                    log.info("[INFLIGHT] Removed key={} after signal={}", cacheKey, signal);
                });
    }
}
