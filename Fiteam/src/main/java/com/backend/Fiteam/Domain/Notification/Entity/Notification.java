package com.backend.Fiteam.Domain.Notification.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "Notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "알림 ID", example = "1")
    private Integer id;

    @Column(name = "sender_id")
    @Schema(description = "발신자 ID (유저/관리자 등)", example = "2")
    private Integer senderId;

    @Column(name = "sender_type", length = 50)
    @Schema(description = "발신자 타입 (user, admin 등)", example = "user")
    private String senderType;

    @Column(name = "user_id")
    @Schema(description = "알림 수신자(현재 사용자) ID", example = "5")
    private Integer userId;

    @Schema(description = "알림 유형", example = "Group invite, team match ... ")
    @Column(name = "type")
    private String type;

    @Column(name = "content", length = 300)
    @Schema(description = "알림 내용", example = "새로운 팀 초대가 도착했습니다.")
    private String content;

    @Column(name = "is_read")
    @Schema(description = "알림 읽음 여부", example = "false")
    private Boolean isRead = false;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Schema(description = "알림 생성 시간", example = "2024-05-01T12:00:00")
    private Timestamp createdAt;
}
