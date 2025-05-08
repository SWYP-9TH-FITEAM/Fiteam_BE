package com.backend.Fiteam.Domain.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupMemberProfileResponseDto {

    @Schema(description = "직군", example = "PM, DS, FE, BE 등등")
    private String position;

    @Schema(description = "경력(개월 수)", example = "12")
    private Integer workHistory;

    @Schema(description = "목표", example = "최고의 PM이 되는것")
    private String projectGoal;

    @Schema(description = "목적", example = "수익화")
    private String projectPurpose;

    @Schema(description = "포트폴리오 URL", example = "https://portfolio.com/abc")
    private String url;

    @Schema(description = "자기소개", example = "안녕하세요. 프론트엔드 개발자입니다.")
    private String introduction;

    // User Entity 기반 필드 추가
    @Schema(description = "판단된 캐릭터카드 ID", example = "12")
    private Integer cardId;

    @Schema(description = "EI 성향 점수", example = "7")
    private Integer numEI;

    @Schema(description = "PD 성향 점수", example = "3")
    private Integer numPD;

    @Schema(description = "IA 성향 점수", example = "5")
    private Integer numVA;

    @Schema(description = "CL 성향 점수", example = "8")
    private Integer numCL;
}
