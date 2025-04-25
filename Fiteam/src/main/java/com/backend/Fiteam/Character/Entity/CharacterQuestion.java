package com.backend.Fiteam.Character.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CharacterQuestion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterQuestion {

    @Id
    private Integer id;

    @Column(length = 3)
    private String dimension;

    @Column(length = 300)
    private String question;

    @Column(name = "type_a", length = 1)
    private String typeA;

    @Column(name = "type_b", length = 1)
    private String typeB;
}
