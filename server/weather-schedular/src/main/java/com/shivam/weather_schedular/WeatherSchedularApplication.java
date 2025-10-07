package com.shivam.weather_schedular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WeatherSchedularApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherSchedularApplication.class, args);
	}

}
