package com.backend.Fiteam.Domain.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupNoticeSummaryDto {
    @Schema(description = "공지 ID", example = "1")
    private Integer id;

    @Schema(description = "공지 대상 그룹 ID", example = "7")
    private Integer groupId;

    @Schema(description = "공지 제목", example = "이번 주 발표 안내")
    private String title;

    @Schema(description = "공지 작성 시간", example = "2025-05-12T18:00:00")
    private Timestamp createdAt;
}