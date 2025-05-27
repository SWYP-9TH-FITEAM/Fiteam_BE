package com.backend.Fiteam.Domain.Character.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterCardDto {

    @Schema(description = "캐릭터 ID", example = "1")
    private Integer id;

    @Schema(description = "캐릭터 이름", example = "반짝이는 아이디어 조율 요정")
    private String name;

    @Schema(description = "캐릭터 이미지 URL", example = "https://kr.object.ncloudstorage.com/fiteam-character/1.jpg")
    private String imgUrl;

    @Schema(description = "캐릭터 코드", example = "EPIC")
    private String code;

    @Schema(description = "성향 요약", example = "외향적이고 창의적이며 계획을 중시하고 분위기를 잘 맞춤")
    private String summary;

    @Schema(description = "팀에서의 강점", example = "아이디어 공유, 팀원 간 중재, 분위기메이커")
    private String teamStrength;

    @Schema(description = "보완해야 할 점", example = "우유부단, 결정 장애, 방향 설정 고민")
    private String caution;

    @Schema(description = "가장 잘 맞는 캐릭터 1")
    private CharacterMatchDto bestMatch1;

    @Schema(description = "가장 잘 맞는 캐릭터 2")
    private CharacterMatchDto bestMatch2;

    @Schema(description = "가장 맞지 않는 캐릭터 1")
    private CharacterMatchDto worstMatch1;

    @Schema(description = "가장 맞지 않는 캐릭터 2")
    private CharacterMatchDto worstMatch2;
}
