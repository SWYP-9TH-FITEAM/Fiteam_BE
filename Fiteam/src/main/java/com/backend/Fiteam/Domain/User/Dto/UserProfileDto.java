package com.backend.Fiteam.Domain.User.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileDto {
    @Schema(description = "유저 이름", example = "홍길동")
    private String userName;

    @Schema(description = "프로필 이미지 URL", example = "https://fiteam.shop~~/1_img.jpg")
    private String profileImgUrl;

    @Schema(description = "직무 유형 (예: PM, DS, FE, BE)", example = "BE")
    private String job;
}
