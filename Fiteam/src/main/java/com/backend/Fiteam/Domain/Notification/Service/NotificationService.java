package com.backend.Fiteam.Domain.Notification.Service;

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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserService userService;                 // 그룹 수락 호출
    private final TeamRequestService teamRequestService;   // 팀 수락 호출
    private final GroupMemberService groupMemberService;   // 그룹 멤버 조회/삭제


    /**
     * 새 알림 생성
     * @param recipientId  알림 수신자 User ID
     * @param senderId     알림 발신자 ID (유저/매니저 등)
     * @param senderType   발신자 타입 (e.g. "user", "manager")
     * @param type         알림 유형 (e.g. "Group invite", "Team request")
     * @param tableId      관련 테이블 PK (e.g. groupId or teamRequestId)
     * @param content      알림 메시지 내용
     * @return 생성된 Notification 엔티티
     */
    @Transactional
    public Notification createNotification(Integer recipientId, Integer senderId, String senderType,
            String type, Integer tableId, String content) {
        Notification notification = Notification.builder()
                .userId(recipientId)          // 수신자 ID
                .senderId(senderId)           // 발신자 ID
                .senderType(senderType)       // 발신자 타입
                .type(type)                   // 알림 유형
                .tableId(tableId)             // 연관된 테이블
                .content(content)             // 알림 내용
                .isRead(false)                // 기본 읽음
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        return notificationRepository.save(notification);
    }

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
