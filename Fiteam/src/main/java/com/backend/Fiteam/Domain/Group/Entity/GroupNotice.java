package com.backend.Fiteam.Domain.Group.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "GroupNotice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "공지 ID", example = "1")
    private Integer id;

    @Column(name = "manager_id", nullable = false)
    @Schema(description = "공지 작성자(매니저) ID", example = "1")
    private Integer managerId;

    @Column(name = "group_id", nullable = false)
    @Schema(description = "공지 대상 그룹 ID", example = "7")
    private Integer groupId;

    @Column(length = 100, nullable = false)
    @Schema(description = "공지 제목", example = "이번 주 발표 안내")
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    @Schema(description = "공지 내용", example = "금요일 오후 3시에 본관 101호에서 모임을 진행합니다.")
    private String context;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @Schema(description = "공지 작성 시간", example = "2025-05-12T18:00:00")
    private Timestamp createdAt;
}
