package com.shivam.weather_cache.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "rest_template_connect_timeout=5000",
        "rest_template_read_timeout=10000"
})
class AppConfigTest {

    @Autowired
    private AppConfig appConfig;

    @Test
    void testRestTemplateBeanTimeouts() throws Exception {
        RestTemplate restTemplate = appConfig.restTemplate();
        assertThat(restTemplate).isNotNull();
        assertThat(restTemplate.getInterceptors()).isNotEmpty();

        // Unwrap the internal factory if interceptors exist
        var requestFactory = restTemplate.getRequestFactory();
        SimpleClientHttpRequestFactory factory;
        if (requestFactory instanceof InterceptingClientHttpRequestFactory interceptingFactory) {
            factory = (SimpleClientHttpRequestFactory) interceptingFactory.getDelegate();
        } else {
            factory = (SimpleClientHttpRequestFactory) requestFactory;
        }

        // Use reflection to read private fields
        Field connectTimeoutField = SimpleClientHttpRequestFactory.class.getDeclaredField("connectTimeout");
        connectTimeoutField.setAccessible(true);
        int connectTimeout = (int) connectTimeoutField.get(factory);

        Field readTimeoutField = SimpleClientHttpRequestFactory.class.getDeclaredField("readTimeout");
        readTimeoutField.setAccessible(true);
        int readTimeout = (int) readTimeoutField.get(factory);

        assertThat(connectTimeout).isEqualTo(5000);
        assertThat(readTimeout).isEqualTo(10000);
    }

}
