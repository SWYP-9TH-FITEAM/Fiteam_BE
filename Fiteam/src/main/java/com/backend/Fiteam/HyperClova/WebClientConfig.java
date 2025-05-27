package com.backend.Fiteam.HyperClova;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    private final ClovaProperties props;
    public WebClientConfig(ClovaProperties props) {
        this.props = props;
    }

    @Bean
    public WebClient clovaWebClient() {
        return WebClient.builder()
                .baseUrl(props.getApiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, props.getApiKey())
                .build();
    }
}
