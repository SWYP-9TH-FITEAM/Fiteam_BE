package com.backend.Fiteam.Domain.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupMemberProfileResponseDto {

    @Schema(description = "경력(개월 수)", example = "12")
    private Integer workHistory;

    @Schema(description = "목표", example = "프론트엔드 실력 성장")
    private String projectGoal;

    @Schema(description = "포트폴리오 URL", example = "https://portfolio.com/abc")
    private String url;

    @Schema(description = "자기소개", example = "안녕하세요. 프론트엔드 개발자입니다.")
    private String introduction;

    // User Entity 기반 필드 추가
    @Schema(description = "AI 분석 설명", example = "ISTP 유형이면서 ~~한 특징이 있는 사람입니다.")
    private String details;

    @Schema(description = "EI 성향 점수", example = "7")
    private Integer numEI;

    @Schema(description = "PD 성향 점수", example = "3")
    private Integer numPD;

    @Schema(description = "IA 성향 점수", example = "5")
    private Integer numVA;

    @Schema(description = "CL 성향 점수", example = "8")
    private Integer numCL;
}
