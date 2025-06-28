package com.backend.Fiteam.Domain.Group.Dto;

import com.backend.Fiteam.ConfigEnum.Custom.TeamStatusDeserializer;
import com.backend.Fiteam.ConfigEnum.EnumLabelSerializer;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupMemberMiniProfileResponseDto {

    @Schema(description = "유저 ID", example = "7")
    private Integer userId;

    @Schema(description = "유저 이름", example = "홍길동")
    private String userName;

    @Schema(description = "프로필 이미지 URL", example = "https://fiteam.s3.ap-northeast-2.amazonaws.com/7_img.jpg")
    private String imageUrl;

    @Schema(description = "직군", example = "PM")
    private String position;

    @Schema(description = "팀 참여 상태 (예: 모집중, 모집마감 등)", example = "모집중")
    @JsonSerialize(using = EnumLabelSerializer.class)
    @JsonDeserialize(using = TeamStatusDeserializer.class)
    private TeamStatus teamStatus;

    @Schema(description = "팀 id (1인팀 포함해서)", example = "1")
    private Integer teamId;

    @Schema(description = "목표", example = "최고의 PM이 되는것")
    private String projectGoal;

    @Schema(description = "판단된 캐릭터카드 ID", example = "12")
    private Integer cardId;
}
