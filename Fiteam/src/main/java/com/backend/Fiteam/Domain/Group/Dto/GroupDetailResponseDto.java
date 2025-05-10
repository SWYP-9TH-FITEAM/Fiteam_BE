package com.backend.Fiteam.Domain.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "그룹 상세 정보 응답 DTO (ProjectGroup + TeamType)")
public class GroupDetailResponseDto {

    @Schema(description = "그룹 ID", example = "3")
    private Integer id;

    @Schema(description = "그룹 이름", example = "스위프9기")
    private String name;

    @Schema(description = "그룹 설명", example = "함께 성장하는 스위프입니다.")
    private String description;

    @Schema(description = "최대 인원 수", example = "80")
    private Integer maxUserCount;

    @Schema(description = "연락 정책", example = "카카오톡 오픈채팅 이용")
    private String contactPolicy;

    @Schema(description = "그룹 생성일시", example = "2025-04-26T10:30:00")
    private Timestamp createdAt;

    // ---- TeamType 필드들 ----

    @Schema(description = "팀 빌딩 타입 ID", example = "2")
    private Integer teamTypeId;

    @Schema(description = "팀 빌딩 타입 이름", example = "랜덤 자동 배정")
    private String teamTypeName;

    @Schema(description = "팀 빌딩 타입 설명", example = "규칙에 따라 랜덤으로 팀을 구성합니다.")
    private String teamTypeDescription;

    @Schema(description = "팀 빌딩 시작일시", example = "2025-05-15T15:00:00")
    private LocalDateTime startDatetime;

    @Schema(description = "팀 빌딩 종료일시", example = "2025-05-20T18:00:00")
    private LocalDateTime endDatetime;

    @Schema(description = "최소 팀 인원", example = "3")
    private Integer minMembers;

    @Schema(description = "최대 팀 인원", example = "6")
    private Integer maxMembers;

    @Schema(description = "직군별 배정 여부(직군별:true, 랜덤:false)", example = "false")
    private Boolean positionBased;

    @Schema(description = "추가 설정 JSON", example = "{\"FE\":2,\"BE\":2}")
    private String configJson;
}
