package com.backend.Fiteam.Domain.Character.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SaveTestResultResponseDto {
    @Schema(description = "판단된 캐릭터카드 ID", example = "12")
    private Integer cardId;

    @Schema(description = "EI 성향 점수", example = "7")
    private Integer numEI;

    @Schema(description = "PD 성향 점수", example = "5")
    private Integer numPD;

    @Schema(description = "VA 성향 점수", example = "4")
    private Integer numVA;

    @Schema(description = "CL 성향 점수", example = "6")
    private Integer numCL;
}

