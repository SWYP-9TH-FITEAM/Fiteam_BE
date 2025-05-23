package com.backend.Fiteam.Domain.Chat.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ChatMessage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "chat_room_id", nullable = false)
    private Integer chatRoomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", length = 10, nullable = false)
    @Schema(description = "메시지 전송자 타입", example = "USER or MANAGER")
    private SenderType senderType;   // USER or MANAGER

    @Column(name = "sender_id", nullable = false)
    private Integer senderId;

    @Column(name = "message_type", length = 20)
    private String messageType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private Timestamp sentAt;

    public enum SenderType {
        USER,
        MANAGER
    }
}


