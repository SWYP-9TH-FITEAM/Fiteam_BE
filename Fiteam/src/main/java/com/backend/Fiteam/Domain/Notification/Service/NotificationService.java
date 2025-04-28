package com.backend.Fiteam.Domain.Notification.Service;

import com.backend.Fiteam.Domain.Notification.Dto.UserNotifyDto;
import com.backend.Fiteam.Domain.Notification.Repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Service
@RequiredArgsConstructor
@RestControllerAdvice
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<UserNotifyDto> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserId(userId).stream()
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt())) // 최신순 정렬
                .map(notification -> UserNotifyDto.builder()
                        .senderType(notification.getSenderType())
                        .senderId(notification.getSenderId())
                        .content(notification.getContent())
                        .isRead(notification.getIsRead())
                        .createdAt(notification.getCreatedAt())
                        .build())
                .toList();
    }
}
