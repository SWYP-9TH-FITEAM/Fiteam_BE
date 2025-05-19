package com.backend.Fiteam.HyperClova;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClovaWebClientConfig {
    @Bean
    public WebClient clovaStreamer(ClovaProperties props) {
        return WebClient.builder()
                .baseUrl("https://clovastudio.stream.ntruss.com/testapp")
                .defaultHeader(HttpHeaders.AUTHORIZATION, props.getApiKey())
                .build();
    }
}
