package com.backend.Fiteam.Domain.Notification.Service;

import com.backend.Fiteam.ConfigEnum.GlobalEnum;
import com.backend.Fiteam.Domain.Notification.Dto.UserNotifyDto;
import com.backend.Fiteam.Domain.Notification.Entity.Notification;
import com.backend.Fiteam.Domain.Notification.Repository.NotificationRepository;
import com.backend.Fiteam.Domain.Group.Service.GroupMemberService;
import com.backend.Fiteam.Domain.Team.Service.TeamRequestService;
import com.backend.Fiteam.Domain.User.Service.UserService;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;


    @Transactional
    public Notification createAndPushNotification(Integer recipientId, Integer senderId,
            GlobalEnum.SenderType senderType, GlobalEnum.NotificationEventType type, String content) {
        // 1) DB에 알림 저장
        Notification notice = Notification.builder()
                .userId(recipientId)
                .senderId(senderId)
                .senderType(senderType)
                .type(type)
                .content(content)
                .isRead(false)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        Notification saved = notificationRepository.save(notice);

        // 2) DTO 변환 (FE에 전달할 형태)
        UserNotifyDto dto = UserNotifyDto.builder()
                .id(saved.getId())
                .senderType(saved.getSenderType())
                .senderId(saved.getSenderId())
                .type(saved.getType())
                .content(saved.getContent())
                .isRead(saved.getIsRead())
                .createdAt(saved.getCreatedAt())
                .build();

        // 3) WebSocket 푸시 (유저 전용 큐)
        // 클라이언트는 '/user/queue/notifications'를 subscribe 해야 합니다.
        messagingTemplate.convertAndSendToUser(recipientId.toString(), "/queue/notifications", dto);

        return saved;
    }

    @Transactional
    public List<UserNotifyDto> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserId(userId).stream()
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
                .map(n -> UserNotifyDto.builder()
                        .id(n.getId())
                        .type(n.getType())
                        .senderType(n.getSenderType())
                        .senderId(n.getSenderId())
                        .content(n.getContent())
                        .isRead(n.getIsRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public UserNotifyDto markAsReadAndGet(Integer userId, Integer notiId) {
        Notification n = notificationRepository.findById(notiId)
                .orElseThrow(() -> new IllegalArgumentException("알림이 존재하지 않습니다."));
        if (!n.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        n.setIsRead(true);
        notificationRepository.save(n);
        return UserNotifyDto.builder()
                .id(n.getId())
                .type(n.getType())
                .senderType(n.getSenderType())
                .senderId(n.getSenderId())
                .content(n.getContent())
                .isRead(true)
                .createdAt(n.getCreatedAt())
                .build();
    }

    @Transactional
    public void deleteNotification(Integer userId, Integer notiId) {
        Notification n = notificationRepository.findById(notiId)
                .orElseThrow(() -> new IllegalArgumentException("알림이 존재하지 않습니다."));
        if (!n.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        notificationRepository.delete(n);
    }


}
