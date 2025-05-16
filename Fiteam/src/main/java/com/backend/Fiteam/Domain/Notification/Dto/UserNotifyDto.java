package com.backend.Fiteam.Domain.Notification.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
public class UserNotifyDto {

    @Schema(description = "알림 ID", example = "1")
    private Integer id;

    @Schema(description = "발신자 타입 (user, admin 등)", example = "user")
    private String senderType;

    @Schema(description = "발신자 ID (유저/관리자 등)", example = "2")
    private Integer senderId;

    @Schema(description = "알림 유형", example = "group_invite")
    private String type;

    @Schema(description = "알림 내용", example = "새로운 팀 초대가 도착했습니다.")
    private String content;

    @Schema(description = "알림 읽음 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "알림 생성 시간", example = "2025-05-05T14:30:00")
    private Timestamp createdAt;
}
