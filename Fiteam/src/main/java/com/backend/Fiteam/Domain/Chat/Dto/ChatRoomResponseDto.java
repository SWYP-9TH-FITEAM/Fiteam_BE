package com.backend.Fiteam.Domain.Chat.Dto;

import com.backend.Fiteam.Domain.Chat.Entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class ChatRoomResponseDto {

    @Schema(description = "채팅방 ID", example = "1")
    private Integer chatRoomId;

    @Schema(description = "대화방이 속한 그룹 ID", example = "5")
    private Integer groupId;

    @Schema(description = "유저1 ID", example = "5")
    private Integer user1Id;

    @Schema(description = "유저1 이름", example = "홍길동")
    private String user1Name;

    @Schema(description = "유저1 프로필 이미지 URL", example = "https://fiteam.shop/1_img.jpg")
    private String user1ProfileImgUrl;

    @Schema(description = "직업 (예: PM, DS, FE, BE). Group의 직군 아님", example = "BE")
    private String user1Job;

    @Schema(description = "유저2 ID", example = "12")
    private Integer user2Id;

    @Schema(description = "유저2 이름", example = "김철수")
    private String user2Name;

    @Schema(description = "유저2 프로필 이미지 URL", example = "https://fiteam.shop/2_img.jpg")
    private String user2ProfileImgUrl;

    @Schema(description = "직업 (예: PM, DS, FE, BE). Group의 직군 아님", example = "FE")
    private String user2Job;

    @Schema(description = "채팅방 생성 시간", example = "2025-05-01T12:00:00")
    private Timestamp createdAt;


}
