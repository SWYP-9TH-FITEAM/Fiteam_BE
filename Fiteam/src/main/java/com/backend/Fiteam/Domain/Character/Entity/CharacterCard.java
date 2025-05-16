package com.backend.Fiteam.Domain.Character.Entity;

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

    @Schema(example = "https://kr.object.ncloudstorage.com/fiteam-character/1.jpg")
    @Column(nullable = false, unique = true, length = 100)
    private String imgUrl;

    @Schema(example = "EPIC")
    @Column(nullable = false, unique = true, length = 4)
    private String code;

    @Schema(description = "캐릭터 이름", example = "반짝이는 아이디어 조율 요정")
    @Column(length = 30)
    private String name;

    @Schema(description = "키워드", example = "아이디어, 조율력, 팀 분위기 업")
    @Column(length = 50)
    private String keyword;

    @Schema(description = "성향 요약", example = "외향적이고 창의적이며 계획을 중시하고 분위기를 잘 맞춤")
    @Column(length = 200)
    private String summary;

    @Schema(description = "팀에서의 강점", example = "아이디어 공유, 팀원 간 중재, 분위기메이커")
    @Column(name = "team_strength", columnDefinition = "TEXT")
    private String teamStrength;

    @Schema(description = "보완해야 할 점", example = "우유부단, 결정 장애, 방향 설정 고민")
    @Column(columnDefinition = "TEXT")
    private String caution;

    @Schema(description = "가장 잘 맞는 캐릭터 코드1", example = "ENFP")
    @Column(name = "best_match_code1", length = 4)
    private String bestMatchCode1;

    @Schema(description = "베스트 매칭 이유1", example = "감정적인 접근을 통해 INTJ의 균형을 잡아주는 성격")
    @Column(name = "best_match_reason1", columnDefinition = "TEXT")
    private String bestMatchReason1;

    @Schema(description = "가장 잘 맞는 캐릭터 코드2", example = "INFJ")
    @Column(name = "best_match_code2", length = 4)
    private String bestMatchCode2;

    @Schema(description = "베스트 매칭 이유2", example = "가치 지향적인 관점을 공유하여 깊은 유대감을 형성")
    @Column(name = "best_match_reason2", columnDefinition = "TEXT")
    private String bestMatchReason2;

    @Schema(description = "가장 맞지 않는 캐릭터 코드1", example = "ESFP")
    @Column(name = "worst_match_code1", length = 4)
    private String worstMatchCode1;

    @Schema(description = "워스트 매칭 이유1", example = "즉흥적인 행동과 계획적인 성향의 충돌")
    @Column(name = "worst_match_reason1", columnDefinition = "TEXT")
    private String worstMatchReason1;

    @Schema(description = "가장 맞지 않는 캐릭터 코드2", example = "ISFP")
    @Column(name = "worst_match_code2", length = 4)
    private String worstMatchCode2;

    @Schema(description = "워스트 매칭 이유2", example = "갈등 회피 경향으로 인해 소통이 어려울 수 있음")
    @Column(name = "worst_match_reason2", columnDefinition = "TEXT")
    private String worstMatchReason2;
}
