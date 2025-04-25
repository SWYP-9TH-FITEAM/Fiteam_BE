package com.backend.Fiteam.Team.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "TeamMember")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "team_id")
    private Integer teamId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(length = 30)
    private String position;

    @Column(name = "joined_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp joinedAt;
}
