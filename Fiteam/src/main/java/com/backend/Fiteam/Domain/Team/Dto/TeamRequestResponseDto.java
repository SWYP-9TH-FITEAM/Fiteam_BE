package com.backend.Fiteam.Domain.Team.Dto;

import com.backend.Fiteam.ConfigEnum.EnumLabelSerializer;
import com.backend.Fiteam.ConfigEnum.Custom.TeamRequestStatusDeserializer;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamRequestStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    @JsonSerialize(using = EnumLabelSerializer.class)
    @JsonDeserialize(using = TeamRequestStatusDeserializer.class)
    private TeamRequestStatus status;

    @Schema(description = "요청 시간", example = "2025-05-01 13:40:00")
    private Timestamp requestedAt;
}
