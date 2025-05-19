package com.backend.Fiteam.HyperClova;

import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ClovaService {
    private final WebClient clovaWebClient;

    public ClovaService(WebClient clovaWebClient) {
        this.clovaWebClient = clovaWebClient;
    }

    public Mono<TestAppCompletionsResponseDto> completeTestApp(String model,
            TestAppCompletionsRequestDto req) {
        return clovaWebClient.post()
                .uri("/v1/completions/{model}", model)
                .header("X-NCP-CLOVASTUDIO-REQUEST-ID", UUID.randomUUID().toString())
                .bodyValue(req)
                .retrieve()
                .bodyToMono(TestAppCompletionsResponseDto.class);
    }

}
