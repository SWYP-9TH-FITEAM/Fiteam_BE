package com.backend.Fiteam.Domain.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CreateGroupRequestDto {

    @Schema(description = "매니저 ID", example = "1")
    private Integer managerId;

    @Schema(description = "그룹 이름", example = "스위프9기")
    private String name;

    @Schema(description = "그룹 설명", example = "함께 성장하는 스위프입니다.")
    private String description;

    @Schema(description = "최대 인원 수", example = "80")
    private Integer maxUserCount;

    @Schema(description = "팀 빌딩 타입 ID", example = "1")
    private Integer teamMakeType;

    @Schema(description = "연락 정책", example = "카카오톡 오픈채팅 이용, 전화번호 공유 등등")
    private String contactPolicy;
}