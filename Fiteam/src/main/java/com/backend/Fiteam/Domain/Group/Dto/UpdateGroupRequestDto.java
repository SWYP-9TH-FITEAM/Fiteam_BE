package com.backend.Fiteam.Domain.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UpdateGroupRequestDto {

    @Schema(description = "그룹 이름 (수정할 경우 입력)", example = "스위프10기")
    private String name;

    @Schema(description = "그룹 설명 (수정할 경우 입력)", example = "더 멋진 스위프가 되었습니다.")
    private String description;

    @Schema(description = "최대 인원 수 (수정할 경우 입력)", example = "100")
    private Integer maxUserCount;

    @Schema(description = "연락 정책 (수정할 경우 입력)", example = "이메일로만 연락")
    private String contactPolicy;
}
