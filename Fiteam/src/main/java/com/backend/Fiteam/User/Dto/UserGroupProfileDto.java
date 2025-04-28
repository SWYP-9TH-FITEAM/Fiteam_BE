package com.backend.Fiteam.User.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserGroupProfileDto {

    @Schema(description = "포지션", example = "백엔드 개발자")
    private String position;

    @Schema(description = "경력(년 수), 1년 미만은 0으로 표시", example = "2")
    private Integer workHistory;

    @Schema(description = "프로젝트 목표", example = "포트폴리오 웹사이트 개발")
    private String projectGoal;

    @Schema(description = "개인 URL", example = "https://portfolio.example.com")
    private String url;

    @Schema(description = "자기소개", example = "열정 넘치는 백엔드 개발자입니다.")
    private String introduction;
}
