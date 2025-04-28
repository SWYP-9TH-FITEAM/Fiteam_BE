package com.backend.Fiteam.Domain.Team.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TeamType")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50)
    private String name;

    @Column(length = 100)
    private String description;

    @Column(name = "start_datetime")
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime")
    private LocalDateTime endDatetime;

    @Column(name = "min_members")
    private Integer minMembers;

    @Column(name = "max_members")
    private Integer maxMembers;

    @Column(name = "position_based")
    private Boolean positionBased = false;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;
}
