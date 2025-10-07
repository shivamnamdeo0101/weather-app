package com.shivam.weather_svc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")           // sab endpoints
                        .allowedOrigins("http://weather-svc:8080")  // FE URL
                        .allowedMethods("GET")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
