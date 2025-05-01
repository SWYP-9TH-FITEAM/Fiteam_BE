package com.backend.Fiteam.Domain.User.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UserLikeRequestDto {

    @Schema(description = "받는 사용자 ID", example = "5")
    private Integer receiverId;

    @Schema(description = "그룹 ID", example = "2")
    private Integer groupId;

    @Schema(description = "메모", example = "협업이 즐거웠습니다.")
    private String memo;

    @Schema(description = "번호 (예: 1~3 중 고유값)", example = "2")
    private Integer number;
}
