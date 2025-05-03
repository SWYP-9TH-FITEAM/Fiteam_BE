package com.backend.Fiteam.Domain.Character.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CharacterQuestion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "성향검사 질문 엔티티")
public class CharacterQuestion {

    @Id
    @Schema(description = "질문 고유 ID", example = "1")
    private Integer id;

    @Column(length = 4)
    @Schema(description = "질문이 속한 차원 (E-I, P-D, V-A, C-L)")
    private String dimension;

    @Column(length = 300)
    @Schema(description = "성향 검사 질문 내용", example = "당신은 사람들 앞에서 말하는 것을 좋아합니까?")
    private String question;

    @Column(name = "type_a", length = 1)
    @Schema(description = "응답 1에 가까운 타입", example = "I")
    private String typeA;

    @Column(name = "type_b", length = 1)
    @Schema(description = "응답 5에 가까운 타입", example = "E")
    private String typeB;
}
