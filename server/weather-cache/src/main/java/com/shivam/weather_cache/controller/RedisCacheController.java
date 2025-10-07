package com.shivam.weather_cache.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.weather_cache.dto.CustomResponse;
import com.shivam.weather_cache.service.RedisCacheManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather-cache/redis/keys")
public class RedisCacheController {

    private final RedisCacheManagerService cacheService;

    /** 🔍 Get all city keys */
    @GetMapping("/")
    public ResponseEntity<CustomResponse<Set<String>>> getAllKeys() {
        Set<String> keys = cacheService.getAllCityKeys();
        return ResponseEntity.ok(
                CustomResponse.<Set<String>>builder()
                        .success(true)
                        .message(keys.isEmpty() ? "No keys found" : "Keys retrieved successfully")
                        .data(keys)
                        .build()
        );
    }

    /** 🗑️ Delete both data and meta by key */
    @DeleteMapping("/")
    public ResponseEntity<CustomResponse<String>> deleteKey(@RequestParam String key) {
        boolean deleted = cacheService.deleteCityCache(key);
        return ResponseEntity.ok(
                CustomResponse.<String>builder()
                        .success(deleted)
                        .message(deleted
                                ? "Deleted cache for " + key
                                : "Failed to delete cache for " + key)
                        .build()
        );
    }

    /** 📋 Get metadata (hits, lastAccess, etc.) */
    @GetMapping("/meta")
    public ResponseEntity<CustomResponse<Map<Object, Object>>> getMeta(@RequestParam String key) {
        Map<Object, Object> meta = cacheService.getCityMetadata(key);
        return ResponseEntity.ok(
                CustomResponse.<Map<Object, Object>>builder()
                        .success(true)
                        .message(meta == null || meta.isEmpty() ? "No metadata found" : "Metadata retrieved")
                        .data(meta)
                        .build()
        );
    }

    /** 🌦️ Get cached city data */
    @GetMapping("/data")
    public ResponseEntity<CustomResponse<Object>> getData(@RequestParam String key) {
        Object rawData = cacheService.getCityData(key);

        if (rawData == null) {
            return ResponseEntity.ok(CustomResponse.builder()
                    .success(false)
                    .message("No data found for key: " + key)
                    .data(null)
                    .build());
        }

        ObjectMapper mapper = new ObjectMapper();
        Object formattedData;

        try {
            // Convert rawData into a Map if possible
            Map<String, Object> dataMap = mapper.convertValue(rawData, Map.class);

            // If inner "data" exists and is a Map or List, extract it
            if (dataMap.containsKey("data")) {
                formattedData = dataMap.get("data"); // could be List or Map
            } else {
                formattedData = dataMap; // just the whole object
            }

        } catch (IllegalArgumentException e) {
            // rawData is not a Map, probably already a List
            formattedData = rawData;
        }

        CustomResponse<Object> response = CustomResponse.builder()
                .success(true)
                .message("Data fetched successfully")
                .data(formattedData)
                .build();

        return ResponseEntity.ok(response);
    }



    /** ♻️ Refresh metadata timestamp */
    @PutMapping("/refresh")
    public ResponseEntity<CustomResponse<String>> refresh(
            @RequestParam String key,
            @RequestBody Map<String, Object> payload // Accept any JSON
    ) {

        boolean updated = cacheService.refreshCityDataAndMeta(key,payload);

        return ResponseEntity.ok(
                CustomResponse.<String>builder()
                        .success(updated)
                        .message(updated
                                ? "Refresh done for " + key
                                : "Failed to update refresh for " + key)
                        .data(updated ? "Updated successfully" : null)
                        .build()
        );
    }

}
