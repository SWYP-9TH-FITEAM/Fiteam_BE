package com.backend.Fiteam.Group.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "ProjectGroup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "manager_id")
    private Integer managerId;

    @Column(length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(name = "max_user_count")
    private Integer maxUserCount;

    @Column(name = "team_make_type")
    private Integer teamMakeType;

    @Column(name = "contact_policy", length = 255)
    private String contactPolicy;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;
}
