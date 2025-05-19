package com.backend.Fiteam.HyperClova;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "clova")
public class ClovaProperties {
    // application.properties 의 clova.apiUrl 바인딩
    private String apiUrl;
    // application.properties 의 clova.apiKey 바인딩
    private String apiKey;

    public String getApiUrl() {
        return apiUrl;
    }
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    public String getApiKey() {
        return apiKey;
    }
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
