package com.backend.Fiteam.Domain.Chat.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage.SenderType;

/**
 * 매니저-유저 채팅방 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor @Builder
public class CreateManagerChatRoomRequestDto {
    @Schema(description = "매니저 ID", example = "100")
    private Integer managerId;

    @Schema(description = "유저 ID", example = "200")
    private Integer userId;

    @Schema(description = "그룹 ID", example = "1")
    private Integer groupId;
}