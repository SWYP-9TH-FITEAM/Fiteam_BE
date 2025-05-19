package com.backend.Fiteam.Domain.Chat.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class ChatRoomListResponseDto {

    @Schema(description = "채팅방 ID", example = "1")
    private Integer chatRoomId;

    @Schema(description = "로그인한 본인 ID", example = "1")
    private Integer userId;

    @Schema(description = "상대방 사용자 ID", example = "2")
    private Integer otherUserId;

    @Column(name = "group_id", nullable = false)
    private Integer groupId;

    @Schema(description = "상대방 사용자 이름", example = "김고양이")
    private String otherUserName;

    @Schema(description = "상대방 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String otherUserProfileImgUrl;

    @Schema(description = "가장 최근 메시지 내용", example = "안녕하세요!")
    private String lastMessageContent;

    @Schema(description = "안 읽은 메시지 개수", example = "3")
    private Long unreadMessageCount;

    @Schema(description = "가장 최근 메시지 전송 시간", example = "2025-05-01T14:10:00")
    private Timestamp lastMessageTime;
}
