package com.shivam.weather_cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WeatherCacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherCacheApplication.class, args);
	}

}
