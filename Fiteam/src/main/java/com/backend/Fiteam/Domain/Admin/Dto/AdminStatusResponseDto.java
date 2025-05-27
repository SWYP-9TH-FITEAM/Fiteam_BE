package com.backend.Fiteam.Domain.Admin.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatusResponseDto {
    @Schema(description = "전체 회원 수", example = "124")
    private Long totalUsers;

    @Schema(description = "전체 매니저 수", example = "12")
    private Long totalManagers;

    @Schema(description = "오늘 하루 방문자 수", example = "45")
    private Long todayVisitors;  // 선택적 구현
}
