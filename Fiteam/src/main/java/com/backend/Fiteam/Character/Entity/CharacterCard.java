package com.backend.Fiteam.Character.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CharacterCard")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Schema(example = "INTJ")
    @Column(nullable = false, unique = true, length = 4)
    private String code;

    @Schema(description = "캐릭터 이름", example = "전략가")
    @Column(length = 30)
    private String name;

    @Schema(description = "요약 설명", example = "전략적이고 계획적인 성향을 가졌습니다.")
    @Column(length = 200)
    private String summary;

    @Schema(description = "팀에서의 강점", example = "~~분위기 메이커")
    @Column(name = "team_strength", columnDefinition = "TEXT")
    private String teamStrength;

    @Schema(description = "주의해야 할 점", example = "과도한 완벽주의")
    @Column(columnDefinition = "TEXT")
    private String caution;

    @Schema(description = "가장 잘 맞는 캐릭터 코드", example = "ENFP")
    @Column(name = "best_match_code", length = 4)
    private String bestMatchCode;

    @Schema(description = "베스트 매칭 이유", example = "ISTP의 부족함인 적극성을 보완하는 성격")
    @Column(name = "best_match_reason", length = 300)
    private String bestMatchReason;

    @Schema(description = "가장 맞지 않는 캐릭터 코드", example = "ISFP")
    @Column(name = "worst_match_code", length = 4)
    private String worstMatchCode;

    @Schema(description = "워스트 매칭 이유", example = "소통 방식 차이")
    @Column(name = "worst_match_reason", length = 300)
    private String worstMatchReason;
}
