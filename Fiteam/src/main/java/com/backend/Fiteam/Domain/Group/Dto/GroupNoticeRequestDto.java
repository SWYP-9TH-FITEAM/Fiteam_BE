package com.backend.Fiteam.Domain.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "그룹 공지 생성 요청 DTO")
public class GroupNoticeRequestDto {

    @NotNull
    @Schema(description = "공지할 그룹 ID", example = "7", required = true)
    private Integer groupId;

    @NotBlank
    @Schema(description = "공지 제목", example = "이번 주 회의 안내", required = true)
    private String title;

    @NotBlank
    @Schema(description = "공지 내용", example = "금요일 오후 3시에 본관 101호에서 모임을 진행합니다.", required = true)
    private String context;
}
