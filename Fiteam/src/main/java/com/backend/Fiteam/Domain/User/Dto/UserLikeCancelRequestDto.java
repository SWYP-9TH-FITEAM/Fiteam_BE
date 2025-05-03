package com.backend.Fiteam.Domain.User.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UserLikeCancelRequestDto {

    @Schema(description = "받는 사용자 ID", example = "5")
    private Integer receiverId;
}
