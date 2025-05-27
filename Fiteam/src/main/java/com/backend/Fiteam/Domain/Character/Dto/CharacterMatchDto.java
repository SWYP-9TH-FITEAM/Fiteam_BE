package com.backend.Fiteam.Domain.Character.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterMatchDto {

    @Schema(description = "캐릭터 ID", example = "2")
    private Integer id;

    @Schema(description = "캐릭터 이름", example = "조율형 리더")
    private String name;

    @Schema(description = "캐릭터 이미지 URL", example = "https://kr.object.ncloudstorage.com/fiteam-character/2.jpg")
    private String imgUrl;

    @Schema(description = "캐릭터 코드", example = "INFP")
    private String code;

    @Schema(description = "매칭 이유", example = "가치 지향적인 관점을 공유하여 깊은 유대감을 형성")
    private String reason;
}
