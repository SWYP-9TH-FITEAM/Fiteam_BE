package com.backend.Fiteam.HyperClova;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1/clova")
public class ClovaController {

    private final ClovaService clovaService;
    public ClovaController(ClovaService clovaService) {
        this.clovaService = clovaService;
    }

    @Operation(
            summary     = "HCX-003 Chat Completions (스트리밍)",
            description = "테스트 API 키로 HCX-003 Chat Completions SSE 호출"
    )
    @ApiResponse(responseCode = "200", description = "스트리밍된 챗 조각 반환")
    @PostMapping(
            path     = "/chat/completions/{model}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<ChatStreamEvent> chatStream(
            @PathVariable String model,
            @RequestBody ChatCompletionRequestDto request
    ) {
        return clovaService.streamChat(model, request);
    }
}
