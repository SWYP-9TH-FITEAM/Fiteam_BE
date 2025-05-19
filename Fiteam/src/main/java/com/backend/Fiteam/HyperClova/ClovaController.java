package com.backend.Fiteam.HyperClova;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/clova")
public class ClovaController {
    private final ClovaService clovaService;

    public ClovaController(ClovaService clovaService) {
        this.clovaService = clovaService;
    }

    @Operation(
            summary     = "TestApp Completions",
            description = "테스트 API 키로 TestApp v1 Completions 호출"
    )
    @ApiResponse(responseCode = "200", description = "생성된 텍스트 반환")
    @PostMapping(
            path     = "/testapp/completions/{model}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<TestAppCompletionsResponseDto>> testappCompletions(
            @PathVariable String model,
            @RequestBody TestAppCompletionsRequestDto request
    ) {
        return clovaService
                .completeTestApp(model, request)
                .map(ResponseEntity::ok);
    }

}
