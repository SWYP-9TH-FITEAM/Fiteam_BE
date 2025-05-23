package com.backend.Fiteam.Domain.Chat.Dto;

import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage.SenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class ChatMessageResponseDto {

    @Schema(description = "메시지 ID", example = "101")
    private Integer id;

    @Schema(description = "채팅방 ID", example = "1")
    private Integer chatRoomId;

    @Enumerated(EnumType.STRING)
    @Schema(description = "메시지 전송자 타입", example = "USER or MANAGER")
    private SenderType senderType;   // USER or MANAGER

    @Schema(description = "보낸 사람 ID", example = "2")
    private Integer senderId;

    @Schema(description = "메시지 타입", example = "TEXT")
    private String messageType;

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String content;

    @Schema(description = "읽음 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "보낸 시간", example = "2025-05-01T14:15:00")
    private Timestamp sentAt;
}
