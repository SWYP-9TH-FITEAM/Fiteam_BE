package com.backend.Fiteam.Domain.Chat.Dto;

import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage.SenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.Timestamp;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerChatRoomListResponseDto {
    @Schema(description = "Manager-User 채팅방 ID", example = "1")
    private Integer id;

    @Schema(description = "로그인한 본인 ID", example = "2")
    private Integer user_or_manager_Id;

    @Schema(description = "채팅방에 있는 본인 말고, 상대가 User인지 Manager인지 구분", example = "USER, MANAGER")
    private SenderType otherType;  // USER 또는 MANAGER

    @Schema(description = "상대방 ID", example = "2")
    private Integer otherId;

    @Schema(description = "그룹 ID", example = "1")
    private Integer groupId;

    @Schema(description = "상대방 이름", example = "김고양이")
    private String otherName;

    @Schema(description = "상대방 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String otherProfileImgUrl;

    @Schema(description = "가장 최근 메시지 내용", example = "안녕하세요!")
    private String lastMessageContent;

    @Schema(description = "안 읽은 메시지 개수", example = "3")
    private Long unreadMessageCount;

    @Schema(description = "가장 최근 메시지 전송 시간", example = "2025-05-01T14:10:00")
    private Timestamp lastMessageTime;

    @Schema(description = "생성 일시", example = "2025-05-20T13:45:00")
    private Timestamp createdAt;
}
