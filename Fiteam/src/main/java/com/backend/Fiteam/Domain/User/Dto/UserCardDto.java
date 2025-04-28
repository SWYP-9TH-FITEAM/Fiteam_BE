package com.backend.Fiteam.Domain.User.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserCardDto {

    @Schema(description = "캐릭터 코드", example = "INTJ")
    private final String code;

    @Schema(description = "캐릭터 이름", example = "전략가")
    private final String name;

    @Schema(description = "요약 설명", example = "전략적이고 계획적인 성향을 가졌습니다.")
    private final String summary;

    @Schema(description = "팀에서의 강점", example = "팀 분위기 조성")
    private final String teamStrength;

    @Schema(description = "주의해야 할 점", example = "과도한 완벽주의")
    private final String caution;

    @Schema(description = "가장 잘 맞는 캐릭터 코드", example = "ENFP")
    private final String bestMatchCode;

    @Schema(description = "베스트 매칭 이유", example = "서로의 부족한 부분을 보완할 수 있음")
    private final String bestMatchReason;

    @Schema(description = "가장 맞지 않는 캐릭터 코드", example = "ISFP")
    private final String worstMatchCode;

    @Schema(description = "워스트 매칭 이유", example = "소통 스타일 차이")
    private final String worstMatchReason;

    @Schema(description = "AI 분석 디테일", example = "ISTP 유형이면서 빠른 문제해결 능력을 지님")
    private final String details;

    @Schema(description = "EI 성향 점수", example = "7")
    private final Integer ei;

    @Schema(description = "PD 성향 점수", example = "3")
    private final Integer pd;

    @Schema(description = "CL 성향 점수", example = "8")
    private final Integer cl;

    @Schema(description = "IA 성향 점수", example = "5")
    private final Integer ia;
}

