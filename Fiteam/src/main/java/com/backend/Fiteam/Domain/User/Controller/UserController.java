package com.backend.Fiteam.Domain.User.Controller;

import com.backend.Fiteam.Domain.User.Dto.SaveTestAnswerRequestDto;
import com.backend.Fiteam.Domain.User.Dto.TestResultResponseDto;
import com.backend.Fiteam.Domain.User.Dto.UserCardResponseDto;
import com.backend.Fiteam.Domain.User.Dto.UserGroupProfileDto;
import com.backend.Fiteam.Domain.User.Dto.UserLikeCancelRequestDto;
import com.backend.Fiteam.Domain.User.Dto.UserLikeRequestDto;
import com.backend.Fiteam.Domain.User.Dto.UserProfileDto;
import com.backend.Fiteam.Domain.User.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /*
    1.테스트 결과 저장	POST
    2.테스트 결과 조회 (Mini 모달용)
    3.내 프로필카드(GET) (캐릭터카드+AI분석)
    4.마이페이지에서 유저사진과 유저 이름 가져오기
    5.그룹 참여중/대기중 리스트 GET (User 입장)

    6.좋아요 표시
    7.좋아요 취소
    */

    // 1.테스트 결과 저장	POST
    @Operation(summary = "사용자의 성향검사 답변을 결과형태로 저장", description = "[{E:4, I:1}, {P:2, D:3}...] 이렇게 사용자 응답결과를 RequestBody로 주세요")
    @PostMapping("/savecard")
    public ResponseEntity<String> saveTestResult(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody SaveTestAnswerRequestDto requestDto) {
        try{
            Integer userId = Integer.parseInt(userDetails.getUsername());

            userService.saveCharacterTestResult(userId, requestDto);
            return ResponseEntity.ok("테스트 결과가 성공적으로 저장되었습니다.");
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2.테스트 결과 조회 (Mini 모달용)
    @Operation(summary = "성향검사 결과 조회 (Mini 모달용)", description = "유저의 성향검사 결과를 조회합니다.")
    @GetMapping("/test-result")
    public ResponseEntity<TestResultResponseDto> getTestResult(@AuthenticationPrincipal UserDetails userDetails){
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());

            TestResultResponseDto response = userService.getTestResult(userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 3. 내 프로필카드(GET) (캐릭터카드+AI분석)
    @Operation(summary = "유저 프로필카드 조회", description = "JWT를 통해 인증된 사용자의 프로필카드를 조회합니다.")
    @GetMapping("/card")
    public ResponseEntity<UserCardResponseDto> getUserProfileCard(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            UserCardResponseDto profileCard = userService.getUserProfileCard(userId);

            return ResponseEntity.ok(profileCard);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 에러는 그냥 상태코드만 반환
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4. get 마이페이지에서 유저사진과 유저 이름 가져오기
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


    // 보류
    // 그룹 참여할 때 운영자가 초대를 보내는게 아닌가?....
    // 운영자 초대->유저 수락->운영자 수락..
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

    // 6. 유저 좋아요 표시
    @Operation(summary = "다른 유저에게 좋아요 남기기", description = "같은 그룹에 속한 다른 유저에게 좋아요와 메모를 남깁니다.")
    @PostMapping("/like")
    public ResponseEntity<?> sendLike(@RequestBody UserLikeRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer senderId = Integer.parseInt(userDetails.getUsername());
            userService.sendLike(senderId, dto);
            return ResponseEntity.ok("Like success");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    // 7. 유저 좋아요 취소
    @Operation(summary = "좋아요 취소", description = "특정 유저에게 남긴 모든 좋아요를 취소합니다.")
    @DeleteMapping("/unlike/{receiverId}")
    public ResponseEntity<?> cancelAllLikes(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer receiverId) {
        try {
            Integer senderId = Integer.parseInt(userDetails.getUsername());
            userService.cancelAllLikesToUser(senderId, receiverId);
            return ResponseEntity.ok("좋아요가 모두 취소되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }


}
