package com.backend.Fiteam.Domain.User.Controller;


import com.backend.Fiteam.Domain.User.Dto.TestResultResponseDto;
import com.backend.Fiteam.Domain.User.Dto.UserCardHistoryDto;
import com.backend.Fiteam.Domain.User.Dto.UserCardResponseDto;

import com.backend.Fiteam.Domain.User.Dto.UserGroupStatusDto;

import com.backend.Fiteam.Domain.User.Dto.UserProfileDto;
import com.backend.Fiteam.Domain.User.Dto.UserSettingsRequestDto;
import com.backend.Fiteam.Domain.User.Dto.UserSettingsResponseDto;
import com.backend.Fiteam.Domain.User.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Tag(name = "3. UserController - 로그인한 User")
public class UserController {

    private final UserService userService;

    /*
    1.테스트 결과 저장	POST
    2.성향검사 결과 조회 (Mini 모달용)
    3.나의 테스트 결과(캐릭터카드, 점수)+AI분석 보기
    4.get 마이페이지에서 유저사진과 유저 이름 가져오기
    5.초대받은 그룹에 참여하기
    6.참여중인 그룹 목록 조회
    7.초대받은(유저가 수락하기 전) 그룹 목록 조회
    8.마이페이지 내정보 변경하기(프로필 이미지나 등등)
    9.마이페이지 내정보 조회
    */

    private void authorizeUser(UserDetails userDetails) {
        boolean isUser = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_User"));
        if (!isUser) {
            throw new IllegalArgumentException("유저가 아닙니다.");
        }
    }

    // 1.테스트 결과 저장	POST
    @Operation(
            summary = "1. 테스트 결과 저장 (성향검사 응답 → 결과)",
            description = "사용자의 성향검사 답변을 결과형태로 저장합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(
                    value = "[{\"E\": 4, \"I\": 1}, {\"P\": 2, \"D\": 3}, " + "{\"V\": 3, \"A\": 2}, {\"C\": 4, \"L\": 1}]"))))
    @PostMapping("/savecard")
    public ResponseEntity<String> saveTestResult(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody List<Map<String, Integer>> answers) {

        authorizeUser(userDetails);
        Integer userId = Integer.parseInt(userDetails.getUsername());
        userService.saveCharacterTestResult(userId, answers);
        return ResponseEntity.ok("테스트 결과가 성공적으로 저장되었습니다.");
    }

    // 2.성향검사 결과 조회 (Mini 모달용)
    @Operation(summary = "2. 성향검사 결과 조회 (Mini 모달용)", description = "유저의 성향검사 결과를 조회합니다.")
    @GetMapping("/mini-result")
    public ResponseEntity<TestResultResponseDto> getTestResult(@AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        TestResultResponseDto response = userService.getTestResult(userId);
        return ResponseEntity.ok(response);
    }

    // 3.나의 테스트 결과(캐릭터카드, 점수)+AI분석 보기
    @Operation(summary = "3. 나의 테스트 결과(캐릭터카드, 점수)+AI분석 보기", description = "JWT를 통해 인증된 사용자 본인의 프로필카드를 조회합니다.")
    @GetMapping("/card")
    public ResponseEntity<UserCardResponseDto> getUserProfileCard(@AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        UserCardResponseDto profileCard = userService.getUserProfileCard(userId);
        return ResponseEntity.ok(profileCard);
    }

    // 3-1.나의 테스트 결과-id 가져오기
    @Operation(summary = "3-1.나의 테스트 결과-id 가져오기", description = "사용자 검사결과 가져오기-최대 2개까지")
    @GetMapping("/card-ids")
    public ResponseEntity<List<UserCardHistoryDto>> getUserProfileCardID(@AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        List<UserCardHistoryDto> cardHistory = userService.getUserCardHistory(userId);
        return ResponseEntity.ok(cardHistory);
    }

    // 4. get 마이페이지에서 유저사진과 유저 이름 가져오기
    @Operation(summary = "4. 마이페이지에서 유저사진과 유저 이름 가져오기", description = "JWT를 통해 사용자 이름과 프로필 이미지 그리고 직업을 조회합니다.")
    @GetMapping("/name-img-job")
    public ResponseEntity<UserProfileDto> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        UserProfileDto profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    // 5. 초대받은 그룹에 참여하기
    @Operation(summary = "5. 초대받은 그룹에 참여하기", description = "그룹 ID 정보가 '알림' 에 담겨서 넘어올 수 있음. 팀빌딩 페이지 에서도 확인 가능")
    @PatchMapping("/accept/{groupId}")
    public ResponseEntity<String> acceptInvitation(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        userService.acceptGroupInvitation(groupId, userId);
        return ResponseEntity.ok("그룹에 참여했습니다.");
    }

    // 6. 참여중인 그룹 목록 조회
    @Operation(summary = "6. 참여중인 그룹 목록 조회", description = "JWT 인증된 사용자가 현재 참여중인 그룹 리스트를 반환합니다.")
    @GetMapping("/groups/accepted")
    public ResponseEntity<List<UserGroupStatusDto>> getAcceptedGroups(@AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        List<UserGroupStatusDto> list = userService.getUserGroupsByStatus(userId, true);
        return ResponseEntity.ok(list);
    }

    // 7. 초대받은(유저가 수락하기 전) 그룹 목록 조회
    @Operation(summary = "7. 초대받은(유저가 수락하기 전) 그룹 목록 조회", description = "6번 API와 구조 동일")
    @GetMapping("/groups/pending")
    public ResponseEntity<List<UserGroupStatusDto>> getPendingGroups(@AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        List<UserGroupStatusDto> list = userService.getUserGroupsByStatus(userId, false);
        return ResponseEntity.ok(list);
    }

    // 8.마이페이지 내정보 변경하기(프로필 이미지나 등등)
    @Operation(
            summary = "8. 마이페이지 내정보 변경하기 (프로필 이미지 등)",
            description = "null로 넘어온 값은 변경되지 않습니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = UserSettingsRequestDto.class)))
    )
    @PatchMapping("/settings")
    public ResponseEntity<Void> updateUserSettings(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserSettingsRequestDto dto) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        userService.updateUserSettings(userId, dto);
        return ResponseEntity.ok().build();
    }

    // 9. 마이페이지 내정보 조회
    @Operation(summary = "9. 마이페이지 내정보 조회", description = "로그인한 사용자의 전화번호, 카카오 ID, 직업, 전공, 소개, URL을 반환합니다.")
    @GetMapping("/settings")
    public ResponseEntity<UserSettingsResponseDto> getUserSettings(
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        UserSettingsResponseDto dto = userService.getUserSettings(userId);
        return ResponseEntity.ok(dto);
    }

    // 10. 본인 ID 리턴하는
    @GetMapping("/myId")
    public Integer getMyId(@AuthenticationPrincipal UserDetails userDetails) {
        return Integer.valueOf(userDetails.getUsername());
    }
}
