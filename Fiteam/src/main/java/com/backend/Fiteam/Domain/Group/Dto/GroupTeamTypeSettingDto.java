package com.backend.Fiteam.Domain.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GroupTeamTypeSettingDto {
    @Schema(description = "팀타입 이름", example = "랜덤매칭, 직군별구성")
    private String name;

    @Schema(description = "팀타입 설명", example = "직무에 따라 팀을 구성하는 방식")
    private String description;

    @Schema(description = "팀 시작 날짜", example = "2025-05-01T00:00:00")
    private LocalDateTime startDatetime;

    @Schema(description = "팀 종료 날짜", example = "2025-06-01T00:00:00")
    private LocalDateTime endDatetime;

    @Schema(description = "최소 팀원 수", example = "3")
    private Integer minMembers;

    @Schema(description = "최대 팀원 수", example = "6")
    private Integer maxMembers;

    @Schema(description = "직무 기반 매칭 여부", example = "랜덤매칭=false, 직군별:true")
    private Boolean positionBased;

    @Schema(description = "직군별 인원 구성", example = "{\"PM\":1, \"DS\":1, \"FE\":2, \"BE\":2}")
    private Map<String, Integer> configJson;
}
