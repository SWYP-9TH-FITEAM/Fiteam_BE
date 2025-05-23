package com.backend.Fiteam.Domain.Chat.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserChatRoomRequestDto {

    @Schema(description = "채팅 상대방 ID", example = "그룹에서 채팅 신청하기 버튼을 누를 때면, UserID 값을 알고 있을 것 같아서 이렇게 했습니다. email 입력으로 바꿀까요?")
    private Integer receiverId;

    @Schema(description = "대화방이 속한 그룹 ID", example = "5")
    private Integer groupId;
}
