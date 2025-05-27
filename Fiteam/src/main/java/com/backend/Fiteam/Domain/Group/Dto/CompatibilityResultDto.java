package com.backend.Fiteam.Domain.Group.Dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "두 유저 간 성향 궁합 결과 DTO")
public class CompatibilityResultDto {

    @Schema(description = "본인 캐릭터 코드", example = "EPIC")
    private String myCode;

    @Schema(description = "상대방 캐릭터 코드", example = "WISE")
    private String otherCode;

    @Schema(description = "궁합 점수 (0~100)", example = "87")
    private int score;

    @Schema(description = "궁합 설명", example = "서로 다른 성향이지만 보완이 잘 되는 궁합입니다.")
    private String description;
}

