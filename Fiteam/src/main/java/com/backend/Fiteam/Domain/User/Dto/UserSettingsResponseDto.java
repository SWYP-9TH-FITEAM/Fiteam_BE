package com.backend.Fiteam.Domain.User.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class UserSettingsResponseDto {

    @Schema(description = "사용자 이름", example = "고양이")
    private String userName;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImgUrl;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "카카오톡 ID", example = "my_kakao_id")
    private String kakaoId;

    @Schema(description = "직업", example = "Frontend Developer")
    private String job;

    @Schema(description = "전공", example = "Computer Science")
    private String major;

    @Schema(description = "자기소개", example = "안녕하세요. 프론트엔드 개발자입니다.")
    private String introduction;

    @Schema(description = "웹사이트 URL (GitHub, Blog)", example = "https://github.com/username")
    private String url;

    @Schema(description = "CharacterCard1 ID", example = "1")
    private Integer cardId1;

    @Schema(description = "CharacterCard2 ID", example = "2")
    private Integer cardId2;

    @Schema(description = "캐릭터 카드 AI 분석결과", example = "ISTP 유형이면서 ~~한 특징이 있는 사람입니다.")
    private String details;

    @Schema(description = "EI 성향 점수", example = "7")
    private Integer numEI;

    @Schema(description = "PD 성향 점수", example = "3")
    private Integer numPD;

    @Schema(description = "VA 성향 점수", example = "5")
    private Integer numVA;

    @Schema(description = "CL 성향 점수", example = "8")
    private Integer numCL;

    @Schema(description = "계정 생성일시", example = "2025-05-01T12:00:00")
    private Timestamp createdAt;
}
