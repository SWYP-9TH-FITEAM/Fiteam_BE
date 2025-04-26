package com.backend.Fiteam.User.Controller;


import com.backend.Fiteam.Character.Entity.CharacterCard;
import com.backend.Fiteam.Character.Service.CharacterCardService;
import com.backend.Fiteam.Notification.Entity.Notification;
import com.backend.Fiteam.User.Dto.UserCardDto;
import com.backend.Fiteam.User.Dto.UserNotifyDto;
import com.backend.Fiteam.User.Dto.UserProfileDto;
import com.backend.Fiteam.User.Entity.User;
import com.backend.Fiteam.User.Repository.UserRepository;
import com.backend.Fiteam.User.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Mypage API
    // 1. get 마이페이지에서 유저사진과 유저 이름 가져오기
    @Operation(summary = "마이페이지 유저 프로필 조회", description = "JWT를 통해 사용자 이름과 프로필 이미지를 조회합니다.")
    @GetMapping("/mypagedata")
    public ResponseEntity<UserProfileDto> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            UserProfileDto profile = userService.getUserProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2. GET 내 프로필카드
    @Operation(summary = "유저 프로필카드 조회", description = "JWT를 통해 인증된 사용자의 프로필카드를 조회합니다.")
    @GetMapping("/card")
    public ResponseEntity<UserCardDto> getUserProfileCard(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            UserCardDto profileCard = userService.getUserProfileCard(userId);

            return ResponseEntity.ok(profileCard);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 에러는 그냥 상태코드만 반환
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 3. GET 내 알림 모아보기
    @Operation(summary = "받은 알림 모아보기(최신순)", description = "JWT 인증된 사용자의 알림 목록을 조회합니다.")
    @GetMapping("/notifications")
    public ResponseEntity<List<UserNotifyDto>> getMyNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            List<UserNotifyDto> notifications = userService.getUserNotifications(userId);

            return ResponseEntity.ok(notifications);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
