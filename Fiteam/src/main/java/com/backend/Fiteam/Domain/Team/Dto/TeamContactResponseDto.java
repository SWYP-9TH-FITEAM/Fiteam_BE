package com.backend.Fiteam.Domain.Team.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamContactResponseDto {

    @Schema(description = "유저 ID", example = "5")
    private Integer userId;

    @Schema(description = "유저 이름", example = "홍길동")
    private String userName;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "카카오톡 ID", example = "hong_kakao")
    private String kakaoId;
}
