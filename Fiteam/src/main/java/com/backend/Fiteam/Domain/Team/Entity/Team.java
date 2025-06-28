package com.backend.Fiteam.Domain.Team.Entity;

import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "Team")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "team_id")
    private Integer teamId;

    @Column(name = "master_user_id")
    private Integer masterUserId;

    @Column(length = 50)
    private String name;

    @Column(name = "max_members")
    private Integer maxMembers;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "team_status")
    private TeamStatus teamStatus;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;
}
