package com.backend.Fiteam.Domain.User.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
public class SaveTestAnswerRequestDto {

    @Schema(description = "질문별 답변 리스트\", example = \"[{\\\"E\\\":4,\\\"I\\\":1}, {\\\"P\\\":3,\"D\":2}]")
    private List<Map<String, Integer>> answers;
}
