package com.backend.Fiteam.Domain.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "매니저 관리 그룹 상태 정보 응답 DTO")
public class ManagerGroupStatusDto {

    @Schema(description = "그룹 ID", example = "3")
    private Integer groupId;

    @Schema(description = "그룹 이름", example = "스위프9기")
    private String groupName;

    @Schema(description = "현재 그룹 참여중인 멤버 수", example = "12")
    private Integer memberCount;

    @Schema(description = "팀 빌딩 타입(직군별:true, 랜덤:false)", example = "true")
    private Boolean positionBased;

    @Schema(description = "팀 빌딩 상태(대기:PENDING, 진행:ONGOING, 종료:ENDED)", example = "ONGOING")
    private String status;
}
