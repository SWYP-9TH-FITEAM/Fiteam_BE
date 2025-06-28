package com.backend.Fiteam.Domain.Group.Dto;

import com.backend.Fiteam.ConfigEnum.Custom.TeamStatusDeserializer;
import com.backend.Fiteam.ConfigEnum.EnumLabelSerializer;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberResponseDto {

    @Schema(description = "유저 ID", example = "10")
    private Integer userId;

    @Schema(description = "유저 그룹MemberID", example = "20")
    private Integer memberId;

    @Schema(description = "유저 이름", example = "홍길동")
    private String userName;

    @Schema(description = "유저 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImageUrl;

    @Schema(description = "성향검사 카드 ID- 1~16", example = "3")
    private Integer cardId1;

    @Schema(description = "직군 (직무)", example = "PM, DS, FE, BE 등등")
    private String position;

    @Schema(description = "팀 참여 상태 (예: 모집중, 모집마감 등)", example = "모집중")
    @JsonSerialize(using = EnumLabelSerializer.class)
    @JsonDeserialize(using = TeamStatusDeserializer.class)
    private TeamStatus teamStatus;

    @Schema(description = "팀 ID", example = "12")
    private Integer teamId;

    @Schema(description = "로그인한 현재 유저가 Like 한 그룹 멤버인지", example = "18 or null")
    private Integer likeId;
}
