// com/backend/Fiteam/Domain/Group/Dto/ManagerGroupListDto.java
package com.backend.Fiteam.Domain.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "매니저 관리 그룹 간단 리스트 DTO")
public class ManagerGroupListDto {

    @Schema(description = "그룹 ID", example = "1")
    private Integer id;

    @Schema(description = "그룹 이름", example = "스위프9기")
    private String name;

    @Schema(description = "팀 빌딩 상태(대기:PENDING, 진행:ONGOING, 종료:ENDED)", example = "ONGOING")
    private String status;
}
