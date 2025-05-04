package com.backend.Fiteam.Domain.User.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserCardResponseDto {

    @Schema(description = "캐릭터 코드", example = "INTJ")
    private final String code;

    @Schema(description = "캐릭터 이름", example = "전략가")
    private final String name;

    @Schema(description = "키워드", example = "분석력, 전략, 계획")
    private final String keyword;

    @Schema(description = "요약 설명", example = "전략적이고 계획적인 성향을 가졌습니다.")
    private final String summary;

    @Schema(description = "팀에서의 강점", example = "팀 분위기 조성")
    private final String teamStrength;

    @Schema(description = "주의해야 할 점", example = "과도한 완벽주의")
    private final String caution;

    @Schema(description = "가장 잘 맞는 캐릭터 코드1", example = "ENFP")
    private final String bestMatchCode1;

    @Schema(description = "베스트 매칭 이유1", example = "서로의 부족한 부분을 보완할 수 있음")
    private final String bestMatchReason1;

    @Schema(description = "가장 잘 맞는 캐릭터 코드2", example = "INFJ")
    private final String bestMatchCode2;

    @Schema(description = "베스트 매칭 이유2", example = "공감과 계획의 조화로 시너지를 냄")
    private final String bestMatchReason2;

    @Schema(description = "가장 맞지 않는 캐릭터 코드1", example = "ISFP")
    private final String worstMatchCode1;

    @Schema(description = "워스트 매칭 이유1", example = "소통 스타일 차이")
    private final String worstMatchReason1;

    @Schema(description = "가장 맞지 않는 캐릭터 코드2", example = "ESFP")
    private final String worstMatchCode2;

    @Schema(description = "워스트 매칭 이유2", example = "즉흥성과 논리의 부조화")
    private final String worstMatchReason2;

    @Schema(description = "AI 분석 디테일", example = "ISTP 유형이면서 빠른 문제해결 능력을 지님")
    private final String details;

    @Schema(description = "EI 성향 점수", example = "7")
    private final Integer ei;

    @Schema(description = "PD 성향 점수", example = "3")
    private final Integer pd;

    @Schema(description = "CL 성향 점수", example = "8")
    private final Integer cl;

    @Schema(description = "VA 성향 점수", example = "5")
    private final Integer va;
}
