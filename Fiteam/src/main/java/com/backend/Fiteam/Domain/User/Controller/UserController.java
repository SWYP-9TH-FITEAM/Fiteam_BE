package com.backend.Fiteam.Domain.User.Controller;

import com.backend.Fiteam.Domain.User.Dto.UserCardDto;
import com.backend.Fiteam.Domain.User.Dto.UserGroupProfileDto;
import com.backend.Fiteam.Domain.User.Dto.UserProfileDto;
import com.backend.Fiteam.Domain.User.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
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
    1.테스트 결과 저장	POST
    2.테스트 결과 조회 (Mini 모달용)
    3.마이페이지에서 유저사진과 유저 이름 가져오기
    4.내 프로필카드(GET) (캐릭터카드+AI분석)
    5.내 테스트 결과(GET) (홈 화면)
    6.그룹 참여중/대기중 리스트 GET (User 입장)
    7.좋아요 표시
    8.좋아요 취소
    */


    // 3. get 마이페이지에서 유저사진과 유저 이름 가져오기
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

    // 4. GET 내 프로필카드
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
