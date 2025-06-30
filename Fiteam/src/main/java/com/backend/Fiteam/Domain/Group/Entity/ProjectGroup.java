package com.backend.Fiteam.Domain.Group.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "그룹을 생성한 매니저 ID", example = "1")
    @Column(name = "manager_id")
    private Integer managerId;

    @Schema(description = "그룹 이름", example = "스위프9기")
    @Column(length = 50)
    private String name;

    @Schema(description = "그룹 설명", example = "함께 성장하는 스위프입니다.")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Schema(description = "최대 인원 수", example = "80")
    @Column(name = "max_user_count")
    private Integer maxUserCount;

    @Schema(description = "팀 빌딩 방식 타입")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_make_type", insertable = false, updatable = false)
    private TeamType teamMakeType;

    @Schema(description = "연락 정책", example = "카카오톡 오픈채팅 이용, 전화번호 공유 등등")
    @Column(name = "contact_policy", length = 255)
    private String contactPolicy;

    @Schema(description = "그룹 생성일시", example = "2025-04-26T10:30:00")
    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;
}
