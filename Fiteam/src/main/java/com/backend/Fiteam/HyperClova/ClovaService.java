package com.backend.Fiteam.HyperClova;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class ClovaService {
    private final WebClient clovaWebClient;
    private final ObjectMapper objectMapper;

    public ClovaService(WebClient clovaWebClient, ObjectMapper objectMapper) {
        this.clovaWebClient  = clovaWebClient;
        this.objectMapper    = objectMapper;
    }

    public Flux<ChatStreamEvent> streamChat(String model, ChatCompletionRequestDto req) {
        return clovaWebClient.post()
                .uri("/v3/chat-completions/{model}", model)
                .header("X-NCP-CLOVASTUDIO-REQUEST-ID", UUID.randomUUID().toString())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .bodyValue(req)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .flatMap(sse -> {
                    String json = sse.data();
                    if (json == null || "[DONE]".equals(json.trim())) {
                        return Flux.empty();
                    }
                    try {
                        // ChatStreamEvent 로 매핑
                        return Flux.just(objectMapper.readValue(json, ChatStreamEvent.class));
                    } catch (Exception e) {
                        return Flux.error(e);
                    }
                });
    }
}
