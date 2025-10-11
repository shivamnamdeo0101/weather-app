package com.shivam.weather_cache.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class AppConfig {

    @Value("${rest_template_connect_timeout}")
    private int connectTimeout;
    @Value("${rest_template_read_timeout}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate() {
        //Factory with timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout); // 5 sec connection timeout
        factory.setReadTimeout(readTimeout);    // 5 sec read timeout

        RestTemplate restTemplate = new RestTemplate(factory);

        //logging interceptor for debugging requests
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add((request, body, execution) -> {
//            log.info("Calling URL: " + request.getURI());
            return execution.execute(request, body);
        });
        restTemplate.setInterceptors(interceptors);

        return restTemplate;
    }
}
