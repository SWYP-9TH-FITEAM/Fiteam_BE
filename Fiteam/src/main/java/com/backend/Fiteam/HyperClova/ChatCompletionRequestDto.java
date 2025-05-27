package com.backend.Fiteam.HyperClova;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "HCX-003 Chat Completions 요청 DTO")
public class ChatCompletionRequestDto {
    @Schema(description = "대화 메시지 리스트", required = true)
    private List<MessageDto> messages;

    @Schema(description = "top-p 샘플링 필터 (0.0~1.0)", example = "0.8")
    private Double topP;

    @Schema(description = "top-k 샘플링 필터 (0 사용 시 비활성)", example = "0")
    private Integer topK;

    @Schema(description = "최대 생성 토큰 수", example = "256")
    private Integer maxTokens;

    @Schema(description = "온도(temperature)", example = "0.5")
    private Double temperature;

    @JsonProperty("repetitionPenalty")
    @Schema(description = "반복 억제 페널티", example = "1.1")
    private Double repetitionPenalty;

    @JsonProperty("stop")
    @Schema(description = "생성 중단 토큰 리스트", example = "[]")
    private List<String> stop;

    @Schema(description = "AI 필터 포함 여부", example = "true")
    private Boolean includeAiFilters;

    @Schema(description = "난수 시드", example = "0")
    private Long seed;
}
