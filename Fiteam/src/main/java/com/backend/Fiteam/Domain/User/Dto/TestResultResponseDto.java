package com.backend.Fiteam.Domain.User.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestResultResponseDto {

    @Schema(description = "캐릭터 코드 (ex: INTJ)", example = "INTJ")
    private String code;

    @Schema(description = "캐릭터 이름 (ex: 전략가)", example = "전략가")
    private String name;

    @Schema(description = "EI 성향 점수", example = "7")
    private Integer numEI;

    @Schema(description = "PD 성향 점수", example = "3")
    private Integer numPD;

    @Schema(description = "VA 성향 점수", example = "5")
    private Integer numVA;

    @Schema(description = "CL 성향 점수", example = "8")
    private Integer numCL;
}
