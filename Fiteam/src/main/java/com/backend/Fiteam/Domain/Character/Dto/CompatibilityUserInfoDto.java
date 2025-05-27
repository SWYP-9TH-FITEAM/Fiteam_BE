package com.backend.Fiteam.Domain.Character.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "궁합 분석에 필요한 사용자 성향 정보")
public class CompatibilityUserInfoDto {

    @Schema(description = "성향 카드 ID", example = "1")
    private Integer cardId;

    @Schema(description = "EI 성향 점수", example = "7")
    private Integer numEI;

    @Schema(description = "PD 성향 점수", example = "3")
    private Integer numPD;

    @Schema(description = "VA 성향 점수", example = "5")
    private Integer numVA;

    @Schema(description = "CL 성향 점수", example = "8")
    private Integer numCL;
}

