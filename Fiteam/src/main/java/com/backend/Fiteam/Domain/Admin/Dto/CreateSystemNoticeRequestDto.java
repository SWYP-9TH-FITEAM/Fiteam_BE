// src/main/java/com/backend/Fiteam/Domain/Admin/Dto/CreateSystemNoticeRequestDto.java
package com.backend.Fiteam.Domain.Admin.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSystemNoticeRequestDto {

    @Schema(description = "Admin ID", example = "1")
    private Integer adminId;

    @Schema(description = "공지 제목", example = "서비스 점검 안내")
    private String title;

    @Schema(description = "공지 내용", example = "5월 30일 02:00 ~ 04:00 서비스 점검이 진행됩니다.")
    private String content;
}
