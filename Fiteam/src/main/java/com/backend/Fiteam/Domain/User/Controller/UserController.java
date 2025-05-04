package com.backend.Fiteam.Domain.User.Controller;

import com.backend.Fiteam.Domain.User.Dto.SaveTestAnswerRequestDto;
import com.backend.Fiteam.Domain.User.Dto.TestResultResponseDto;
import com.backend.Fiteam.Domain.User.Dto.UserCardResponseDto;
import com.backend.Fiteam.Domain.User.Dto.UserGroupProfileDto;
import com.backend.Fiteam.Domain.User.Dto.UserGroupStatusDto;
import com.backend.Fiteam.Domain.User.Dto.UserLikeRequestDto;
import com.backend.Fiteam.Domain.User.Dto.UserLikeResponseDto;
import com.backend.Fiteam.Domain.User.Dto.UserProfileDto;
import com.backend.Fiteam.Domain.User.Dto.UserSettingsRequestDto;
import com.backend.Fiteam.Domain.User.Dto.UserSettingsResponseDto;
import com.backend.Fiteam.Domain.User.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.NoSuchElementException;
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
    5.그룹 초대 수락하기
    6.그룹 참여중/대기중 리스트 GET (User 입장)
    7.마이페이지 설정값 변경하기(프로필 이미지나 등등)
    */

    // 1.테스트 결과 저장	POST
    @Operation(summary = "사용자의 성향검사 답변을 결과형태로 저장. 질문의 typeA:I, typeB:E 일 때 응답이 4라면-> I:1, E:4",
            description = "[{E:4, I:1}, {P:2, D:3}...] 이렇게 사용자 응답결과를 RequestBody로 주세요")
    @PostMapping("/savecard")
    public ResponseEntity<?> saveTestResult(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody SaveTestAnswerRequestDto requestDto) {
        try{
            Integer userId = Integer.parseInt(userDetails.getUsername());

            // 질문에 대한 답변을 받아서 연산후 성향 점수를 저장함.
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
    @Operation(summary = "유저 프로필카드 조회", description = "JWT를 통해 인증된 사용자 본인의 프로필카드를 조회합니다.")
    @GetMapping("/card")
    public ResponseEntity<UserCardResponseDto> getUserProfileCard(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            UserCardResponseDto profileCard = userService.getUserProfileCard(userId);

            return ResponseEntity.ok(profileCard);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4. get 마이페이지에서 유저사진과 유저 이름 가져오기
    @Operation(summary = "마이페이지 유저 프로필 조회", description = "JWT를 통해 사용자 이름과 프로필 이미지를 조회합니다.")
    @GetMapping("/name/img")
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

    // 5. 초대받은 그룹에 참여하기
    @Operation(summary = "유저가 받은 그룹 초대에서 그룹 참여", description = "현재 사용자가 그룹 초대를 수락합니다.")
    @PatchMapping("/accept/{groupId}")
    public ResponseEntity<?> acceptInvitation(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
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

    // 6. 참여중인 그룹 목록 조회
    @Operation(summary = "참여중인 그룹 조회", description = "JWT 인증된 사용자가 현재 참여중인 그룹 리스트를 반환합니다.")
    @GetMapping("/groups/accepted")
    public ResponseEntity<List<UserGroupStatusDto>> getAcceptedGroups(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            List<UserGroupStatusDto> list = userService.getUserGroupsByStatus(userId, true);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 6. 초대받은(유저가 수락하기 전) 그룹 목록 조회
    @Operation(summary = "대기중인 그룹 조회", description = "JWT 인증된 사용자가 초대 대기중인 그룹 리스트를 반환합니다.")
    @GetMapping("/groups/pending")
    public ResponseEntity<List<UserGroupStatusDto>> getPendingGroups(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            List<UserGroupStatusDto> list = userService.getUserGroupsByStatus(userId, false);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 7.마이페이지 설정값 변경하기(프로필 이미지나 등등)
    @Operation(summary = "마이페이지 설정 변경", description = "전화번호, 카카오톡 ID, 직업, 전공, 소개, URL을 수정합니다. null은 변경하지 않습니다.")
    @PatchMapping("/settings")
    public ResponseEntity<?> updateUserSettings(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody UserSettingsRequestDto dto) {
        try {
            Integer userId = Integer.valueOf(userDetails.getUsername());
            userService.updateUserSettings(userId, dto);
            return ResponseEntity.ok().build();

        } catch (NoSuchElementException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 8. 마이페이지 설정값 가져오기
    @Operation(summary = "마이페이지 정보 조회", description = "로그인한 사용자의 전화번호, 카카오ID, 직업, 전공, 소개, URL을 반환합니다.")
    @GetMapping("/settings")
    public ResponseEntity<UserSettingsResponseDto> getUserSettings(@AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = Integer.valueOf(userDetails.getUsername());
        UserSettingsResponseDto dto = userService.getUserSettings(userId);
        return ResponseEntity.ok(dto);
    }
}
