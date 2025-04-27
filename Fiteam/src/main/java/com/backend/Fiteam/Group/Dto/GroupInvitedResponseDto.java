package com.backend.Fiteam.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class GroupInvitedResponseDto {

    @Schema(description = "초대 성공한 사용자 수", example = "30")
    private Integer successCount;

    @Schema(description = "이미 그룹에 참가 중인 사용자 이메일 목록", example = "[\"already1@example.com\", \"already2@example.com\"]")
    private List<String> alreadyInGroupEmails;

    @Schema(description = "존재하지 않는 사용자 이메일 목록", example = "[\"notfound1@example.com\", \"notfound2@example.com\"]")
    private List<String> notFoundEmails;
}
