package com.backend.Fiteam.Domain.Group.Entity;

import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "GroupMember")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "is_accepted")
    private Boolean isAccepted = false;

    @Column(name = "invited_at")
    private Timestamp invitedAt;

    @Column(name = "team_status")
    private TeamStatus teamStatus;

    @Column(name = "team_id")
    private Integer teamId;

    @Column
    private Boolean ban = false;

    @Column(length = 30)
    private String position;

    @Column(name = "work_history")
    private Integer workHistory;

    @Column(name = "project_goal", length = 200)
    private String projectGoal;

    @Column(name = "project_purpose", length = 50)
    private String projectPurpose;

    @Column(length = 200)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String introduction;
}
