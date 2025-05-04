package com.backend.Fiteam.Domain.User.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UserSettingsRequestDto {

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "카카오톡 ID", example = "my_kakao_id")
    private String kakaoId;

    @Schema(description = "직업", example = "Frontend Developer")
    private String job;

    @Schema(description = "전공", example = "Computer Science")
    private String major;

    @Schema(description = "자기소개", example = "안녕하세요. 프론트엔드 개발자입니다.")
    private String introduction;

    @Schema(description = "웹사이트 URL (GitHub, Blog)", example = "https://github.com/username")
    private String url;
}

