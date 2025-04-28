package com.backend.Fiteam.Domain.Notification.Controller;

import com.backend.Fiteam.Domain.Notification.Service.NotificationService;
import com.backend.Fiteam.Domain.Notification.Dto.UserNotifyDto;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/noti")
@RequiredArgsConstructor
public class NotificationController {

    /*
    1. 내 알림 리스트 GET
    2. 알림 하나 열어보기
    3. 알림메시지 삭제하기
    4. 알림 통해 초대 수락(그룹참여, 팀 참여수락), 거절
    */

    private final NotificationService notificationService;

    // 1. 내 알림 리스트 GET
    @Operation(summary = "받은 알림 모아보기(최신순)", description = "JWT 인증된 사용자의 알림 목록을 조회합니다.")
    @GetMapping("/notifications")
    public ResponseEntity<List<UserNotifyDto>> getMyNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            List<UserNotifyDto> notifications = notificationService.getUserNotifications(userId);

            return ResponseEntity.ok(notifications);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
