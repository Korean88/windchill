package com.dtc.automation.windchill.client.download;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfiguration {

    @Value("${token.encoded}")
    private String token;

    @Bean
    public RestTemplate payloadRestTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        return new RestTemplateBuilder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + token)
                .requestFactory(() -> clientHttpRequestFactory)
                .build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
