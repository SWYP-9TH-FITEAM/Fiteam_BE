package com.backend.Fiteam.Notification.Entity;

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
    private Integer id;

    @Column(name = "sender_id")
    private Integer senderId;

    @Column(name = "sender_type", length = 50)
    private String senderType;

    @Column(name = "user_id")
    private Integer userId;

    @Column(length = 300)
    private String content;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;
}
