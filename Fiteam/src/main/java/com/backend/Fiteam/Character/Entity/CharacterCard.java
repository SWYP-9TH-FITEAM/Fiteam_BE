package com.backend.Fiteam.Character.Entity;

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

    @Column(nullable = false, unique = true, length = 4)
    private String code;

    @Column(length = 30)
    private String name;

    @Column(length = 200)
    private String summary;

    @Column(name = "team_strength", columnDefinition = "TEXT")
    private String teamStrength;

    @Column(columnDefinition = "TEXT")
    private String caution;

    @Column(name = "best_match_code", length = 4)
    private String bestMatchCode;

    @Column(name = "best_match_reason", length = 300)
    private String bestMatchReason;

    @Column(name = "worst_match_code", length = 4)
    private String worstMatchCode;

    @Column(name = "worst_match_reason", length = 300)
    private String worstMatchReason;
}
