package com.shivam.weather_svc.service;

import com.shivam.weather_svc.dto.ForecastItemDTO;
import com.shivam.weather_svc.dto.ForecastResponseDTO;
import com.shivam.weather_svc.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private final WeatherPredictionService predictionService;
    private final WebClient webClient = WebClient.create(); // for reactive calls



    public WeatherService(WeatherPredictionService predictionService) {
        this.predictionService = predictionService;
    }

    public Mono<List<ForecastItemDTO>> getThreeHourForecastReactive(String cityName) {
        String url = apiUrl + "?q=" + cityName + "&cnt=10&units=metric&appid=" + apiKey;

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ForecastResponseDTO>() {})
                .map(response -> {
                    if (response == null || response.getList() == null) {
                        return Collections.<ForecastItemDTO>emptyList();
                    }
                    response.getList()
                            .forEach(item -> item.setPredictions(predictionService.generatePredictions(item)));
                    return response.getList();
                })
                .onErrorMap(e -> {
                    log.error("Reactive weather API call failed for city {}: {}", cityName, e.getMessage());
                    return new ExternalApiException("Weather API failed", null);
                });
    }

}
