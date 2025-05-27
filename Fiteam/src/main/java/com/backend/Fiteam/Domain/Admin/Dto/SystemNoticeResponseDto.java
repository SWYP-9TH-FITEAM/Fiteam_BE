// src/main/java/com/backend/Fiteam/Domain/Admin/Dto/SystemNoticeResponseDto.java
package com.backend.Fiteam.Domain.Admin.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemNoticeResponseDto {

    @Schema(description = "공지 ID", example = "10")
    private Integer id;

    @Schema(description = "작성한 Admin 이름", example = "1")
    private String adminName;

    @Schema(description = "공지 제목", example = "서비스 점검 안내")
    private String title;

    @Schema(description = "공지 내용", example = "5월 30일 02:00 ~ 04:00 서비스 점검이 진행됩니다.")
    private String content;

    @Schema(description = "작성 시각", example = "2025-05-27T11:30:00")
    private LocalDateTime createdAt;
}
