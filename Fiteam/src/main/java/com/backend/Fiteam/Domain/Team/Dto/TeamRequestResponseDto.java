package com.backend.Fiteam.Domain.Team.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class TeamRequestResponseDto {

    private Integer id;

    @Schema(description = "요청 보낸 유저 ID", example = "3")
    private Integer senderId;

    @Schema(description = "요청 보낸 유저 이름", example = "김코딩")
    private String senderName;

    @Schema(description = "그룹 ID", example = "1")
    private Integer groupId;

    @Schema(description = "요청 상태", example = "대기중")
    private String status;

    @Schema(description = "요청 시간", example = "2025-05-01 13:40:00")
    private Timestamp requestedAt;
}
