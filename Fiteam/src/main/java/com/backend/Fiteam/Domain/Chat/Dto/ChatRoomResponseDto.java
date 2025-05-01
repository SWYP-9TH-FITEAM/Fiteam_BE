package com.backend.Fiteam.Domain.Chat.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class ChatRoomResponseDto {

    @Schema(description = "채팅방 ID", example = "1")
    private Integer chatRoomId;

    @Schema(description = "유저1 ID", example = "5")
    private Integer user1Id;

    @Schema(description = "유저2 ID", example = "12")
    private Integer user2Id;

    @Schema(description = "채팅방 생성 시간", example = "2025-05-01T12:00:00")
    private Timestamp createdAt;
}
