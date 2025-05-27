// src/main/java/com/backend/Fiteam/Domain/Admin/Entity/SystemNotice.java
package com.backend.Fiteam.Domain.Admin.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_notice")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 공지 작성한 Admin의 ID */
    @Column(name = "admin_id", nullable = false)
    private Integer adminId;

    /** 공지 제목 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 공지 내용 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 작성 시각 (자동 입력) */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
