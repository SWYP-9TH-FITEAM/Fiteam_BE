package com.backend.Fiteam.Domain.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "그룹 요약 정보 DTO")
public class ManagerGroupResponseDto {
    @Schema(description = "그룹 ID", example = "1")
    private Integer id;

    @Schema(description = "그룹 이름", example = "스위프9기")
    private String name;

    @Schema(description = "그룹 설명", example = "함께 성장하는 스위프입니다.")
    private String description;

    @Schema(description = "그룹 생성일시", example = "2025-04-26T10:30:00")
    private Timestamp createdAt;

    @Schema(description = "팀 빌딩 종료일시", example = "2025-05-15T15:00:00")
    private LocalDateTime endDatetime;
}
