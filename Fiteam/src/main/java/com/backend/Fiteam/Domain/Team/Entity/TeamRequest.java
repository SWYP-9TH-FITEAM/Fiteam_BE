package com.backend.Fiteam.Domain.Team.Entity;

import com.backend.Fiteam.ConfigEnum.Custom.TeamRequestStatusConverter;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "TeamRequest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "team_id")
    private Integer teamId;

    @Column(name = "sender_id")
    private Integer senderId;

    @Column(name = "receiver_id")
    private Integer receiverId;

    private TeamRequestStatus status;

    @Column(name = "requested_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp requestedAt;
}
