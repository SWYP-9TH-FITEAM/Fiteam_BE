package com.backend.Fiteam.User.Controller;

import com.backend.Fiteam.User.Dto.UserCardDto;
import com.backend.Fiteam.User.Dto.UserGroupProfileDto;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /*
    1.테스트 결과 저장	POST	/v1/card/result
    2.테스트 결과 조회 (Mini 모달용)	GET	/v1/card/result/mini
    3.마이페이지에서 유저사진과 유저 이름 가져오기 GET	GET	/v1/user/mypage/profile
    4.내 프로필카드(GET) (캐릭터카드+AI분석)	GET	/v1/user/card
    5.내 테스트 결과(GET) (홈 화면)	GET	/v1/user/test-result
    6.내 알림 리스트 GET	GET	/v1/user/notifications
    7.알림 하나 열어보기	GET	/v1/user/notifications/{notificationId}
    //이거는 NotiController로 옮겨야하나.. - 8.알림 통해 초대 수락(그룹참여) 	PATCH	/v1/user/invitations/{groupMemberId}/accept , reject
    9.그룹 참여중/대기중 리스트 GET (User 입장)	GET	/v1/user/my-groups
    11.좋아요 표시 POST	POST	/v1/like
    12.좋아요 취소 DELETE	DELETE	/v1/like
    */

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

    // 보류
    // 그룹 참여할 때 운영자가 초대를 보내는게 아닌가?....
    @Operation(summary = "그룹 참여", description = "현재 사용자가 그룹 초대를 수락합니다.")
    @PatchMapping("/accept/{groupId}")
    public ResponseEntity<?> acceptInvitation(
            @PathVariable Integer groupId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            userService.acceptGroupInvitation(groupId, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "그룹멤버 프로필 수정", description = "초대 수락 후에 그룹 멤버가 자신의 프로필 정보를 수정합니다. (수정 안하는 필드는 null로 입력해주세요")
    @PatchMapping("/groupprofile/{groupMemberId}")
    public ResponseEntity<?> updateGroupMemberProfile(
            @PathVariable Integer groupMemberId, @RequestBody UserGroupProfileDto requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            userService.updateGroupMemberProfile(groupMemberId, userId, requestDto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
