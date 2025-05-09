package com.backend.Fiteam.Domain.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
@Schema(description = "매니저 프로필 및 관리 중인 그룹 목록 응답 DTO")
public class ManagerProfileResponseDto {
    @Schema(description = "매니저 ID", example = "1")
    private Integer managerId;

    @Schema(description = "매니저 이름", example = "홍길동")
    private String managerName;
}
