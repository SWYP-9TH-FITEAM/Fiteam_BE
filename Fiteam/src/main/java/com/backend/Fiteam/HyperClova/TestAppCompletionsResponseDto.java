package com.backend.Fiteam.HyperClova;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestAppCompletionsResponseDto {
    @Schema(description="호출 상태 코드")
    private Status status;
    @Schema(description="실제 생성 결과")
    private Result result;

    @Getter @Setter
    public static class Status {
        @Schema(description="상태 코드", example="20000")
        private String code;
        @Schema(description="상태 메시지", example="OK")
        private String message;
    }

    @Getter @Setter
    public static class Result {
        @Schema(description="생성된 텍스트")
        private String text;
        private String start;
        private String restart;
        private Boolean includeTokens;
        private Double topP;
        private Integer topK;
        private Integer maxTokens;
        private Double temperature;
        @JsonProperty("repeatPenalty")
        private Double repeatPenalty;
        private java.util.List<String> stopBefore;
        private Boolean includeAiFilters;
    }
}
