package com.backend.Fiteam.Domain.User.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLikeRequestDto {

    @Schema(description = "좋아요를 받을 유저 ID", example = "5")
    private Integer receiverId;

    @Schema(description = "해당 그룹 ID", example = "2")
    private Integer groupId;

    @Schema(description = "좋아요 수치", example = "1")
    private Integer number;

    @Schema(description = "메모", example = "이분이 FE중 1픽!")
    private String memo;
}
