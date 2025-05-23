package com.backend.Fiteam.Domain.Chat.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.Timestamp;
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
public class ManagerChatRoomResponseDto {
    @Schema(description = "채팅방 ID", example = "1")
    private Integer id;

    @Schema(description = "매니저 ID", example = "100")
    private Integer managerId;

    @Schema(description = "유저 ID", example = "200")
    private Integer userId;

    @Schema(description = "그룹 ID", example = "1")
    private Integer groupId;

    @Schema(description = "생성 일시", example = "2025-05-20T13:45:00")
    private Timestamp createdAt;

    @Schema(description = "유저 이름", example = "홍길동")
    private String userName;

    @Schema(description = "유저 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String userProfileImgUrl;

    @Schema(description = "상대방 직업", example = "유저의 경우 직군 | 매니저는 Manager")
    private String userJob;
}