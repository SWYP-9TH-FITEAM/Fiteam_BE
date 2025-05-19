package com.backend.Fiteam.HyperClova;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Schema(description = "Test App Completions 요청 DTO (LK-D2 등)")
public class TestAppCompletionsRequestDto {
    @Schema(description = "생성할 텍스트", example = "")
    private String text;

    @Schema(description = "시작 위치(빈 문자열 가능)", example = "")
    private String start;

    @Schema(description = "재시작 위치(빈 문자열 가능)", example = "")
    private String restart;

    @Schema(description = "토큰 포함 여부", example = "true")
    private Boolean includeTokens;

    @Schema(description = "Top-P 필터 (0.0 ~ 1.0)", example = "0.8")
    private Double topP;

    @Schema(description = "Top-K 필터 (0이면 미사용)", example = "0")
    private Integer topK;

    @JsonProperty("maxTokens")
    @Schema(description = "최대 생성 토큰 수", example = "100")
    private Integer maxTokens;

    @Schema(description = "온도(temperature)", example = "0.5")
    private Double temperature;

    @JsonProperty("repeatPenalty")
    @Schema(description = "반복 억제 페널티", example = "1.1")
    private Double repeatPenalty;

    @Schema(description = "생성 중단 단어 리스트", example = "[]")
    private List<String> stopBefore;

    @Schema(description = "AI 필터 포함 여부", example = "true")
    private Boolean includeAiFilters;
}
