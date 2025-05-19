package com.backend.Fiteam.HyperClova;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Playground Completions 요청 DTO")
public class PlaygroundCompletionsRequestDto {
    @Schema(description = "Playground 모델 이름", example = "LK-001")
    private String model;

    @Schema(description = "생성할 텍스트의 프롬프트", example = "안녕하세요, 오늘 일정 알려줘.")
    private String prompt;

    @JsonProperty("max_tokens")
    @Schema(description = "최대 생성 토큰 수", example = "50")
    private Integer maxTokens;

    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public String getPrompt() {
        return prompt;
    }
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    public Integer getMaxTokens() {
        return maxTokens;
    }
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
}
