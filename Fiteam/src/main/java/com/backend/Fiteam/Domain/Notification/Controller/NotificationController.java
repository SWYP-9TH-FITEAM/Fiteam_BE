package com.backend.Fiteam.Domain.Notification.Controller;

import com.backend.Fiteam.Domain.Notification.Dto.UserNotifyDto;
import com.backend.Fiteam.Domain.Notification.Service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/noti")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 1. 내 알림 리스트 GET
    @Operation(summary = "받은 알림 모아보기(최신순)", description = "JWT 인증된 사용자의 알림 목록을 조회합니다.")
    @GetMapping("/notifications")
    public ResponseEntity<List<UserNotifyDto>> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = Integer.valueOf(userDetails.getUsername());
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    // 2. 알림 하나 열어보기
    @Operation(summary = "알림 상세 조회 및 읽음 처리", description = "알림 ID로 특정 알림을 읽음 처리하고 반환합니다.")
    @GetMapping("/notifications/{notificationId}")
    public ResponseEntity<UserNotifyDto> readNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer notificationId) {
        Integer userId = Integer.valueOf(userDetails.getUsername());
        UserNotifyDto dto = notificationService.markAsReadAndGet(userId, notificationId);
        return ResponseEntity.ok(dto);
    }

    // 3. 알림 메시지 삭제하기
    @Operation(summary = "알림 삭제", description = "알림 ID로 특정 알림을 삭제합니다.")
    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer notificationId) {
        Integer userId = Integer.valueOf(userDetails.getUsername());
        notificationService.deleteNotification(userId, notificationId);
        return ResponseEntity.noContent().build();
    }


}
