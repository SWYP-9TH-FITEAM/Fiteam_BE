package com.backend.Fiteam.Domain.Chat.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDto {

    @Schema(description = "채팅방 ID", example = "1")
    private Integer chatRoomId;

    @Schema(description = "보낸 사람 ID", example = "2")
    private Integer senderId;

    @Schema(description = "메시지 타입", example = "TEXT, TEAM_REQUEST, TEAM_RESPONSE")
    private String messageType;

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String content;
}
