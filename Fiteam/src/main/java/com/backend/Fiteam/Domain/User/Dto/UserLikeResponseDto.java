package com.backend.Fiteam.Domain.User.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class UserLikeResponseDto {
    @Schema(description = "좋아요 ID", example = "10")
    private Integer likeId;

    @Schema(description = "좋아요를 받은 유저 ID", example = "5")
    private Integer receiverId;

    @Schema(description = "해당 그룹 ID", example = "2")
    private Integer groupId;

    @Schema(description = "좋아요 수치", example = "1")
    private Integer number;

    @Schema(description = "생성 시각", example = "2025-05-03T12:34:56")
    private Timestamp createdAt;
}
