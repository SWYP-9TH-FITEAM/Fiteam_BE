package com.backend.Fiteam.Domain.Chat.Dto;

import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage.SenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerChatMessageDto {
    @Schema(description = "채팅방 ID", example = "1")
    private Integer roomId;

    @Schema(description = "전송자 타입 (USER or MANAGER)", example = "MANAGER")
    private SenderType senderType;

    @Schema(description = "전송자 ID", example = "100")
    private Integer senderId;

    @Schema(description = "메시지 타입", example = "TEXT")
    private String messageType;

    @Schema(description = "메시지 내용", example = "안녕하세요 매니저입니다.")
    private String content;
}