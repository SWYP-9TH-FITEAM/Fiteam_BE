package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.Domain.Character.Service.CharacterCardService.CompatibilityResult;
import com.backend.Fiteam.Domain.Group.Dto.CompatibilityResultDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupMemberMiniProfileResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupMemberProfileResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupMemberResponseDto;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Service.GroupMemberService;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import com.backend.Fiteam.Domain.User.Dto.UserGroupProfileDto;
import com.backend.Fiteam.Domain.User.Dto.UserProfileDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
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
@RequestMapping("/v1/member")
@RequiredArgsConstructor
@Tag(name = "5. GroupMemberController - 그룹에서 멤버로서")
public class GroupMemberController {

    /*
    1. 직무 유형 리스트 GET,POST (PM,디자이너 등)
    2. 해당그룹 프로필 작성 (경력/목표/URL/소개 작성)
    3. 현재 그룹에서 내가 작성한 프로필 GET
    4. 그룹 다른멤버 프로필 조회
    5. 그룹에 참여한 전체 멤버 리스트 GET

    6. 팀빌딩 페이지에 내 미니정보 가져오기->내 팀(1인일때도) 포함해서
    */

    private final GroupMemberService groupMemberService;

    // 1. 직무 유형 리스트 GET (PM,디자이너 등)
    @Operation(summary = "1. 직무 유형 리스트 GET (PM,디자이너 등)",
            description = "teamMakeType 기준으로 TeamType의 configJson을 파싱하여 직무(position) 리스트를 반환합니다.",
            responses = {@ApiResponse(content = @Content(examples = @ExampleObject(value = "[\"PM\", \"DS\", \"FE\", \"BE\"]")))})
    @GetMapping("/{groupId}/positions")
    public ResponseEntity<List<String>> getGroupPositions(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            groupMemberService.validateGroupMembership(userId, groupId);

            List<String> positions = groupMemberService.getPositionListForGroup(groupId);
            return ResponseEntity.ok(positions);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 2. 해당그룹 프로필 작성 (경력/목표/목적/URL/소개 작성)
    @Operation(summary = "2. 해당그룹 프로필 작성. (경력/목표/목적/URL/소개 작성)그룹멤버 프로필 수정", description = "초대 수락 후에 그룹 멤버가 자신의 프로필 정보를 수정합니다. (수정 안하는 필드는 null로 입력해주세요")
    @PatchMapping("{groupId}/set-profile")
    public ResponseEntity<?> updateGroupMemberProfile(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId, @RequestBody UserGroupProfileDto requestDto) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        groupMemberService.updateGroupMemberProfile(groupId, userId, requestDto);
        return ResponseEntity.ok().build();
    }

    // 3-1. 현재 그룹에서 내가 작성한 프로필 Mini GET
    @Operation(summary = "3. 현재 그룹에서 내가 작성한 프로필 Mini GET", description = "내 프로필 정보",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = GroupMemberMiniProfileResponseDto.class)))})
    @GetMapping("/{groupId}/profile/mini")
    public ResponseEntity<?> getMiniSelfMemberProfile(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        GroupMemberMiniProfileResponseDto profile = groupMemberService.getMemberMiniProfile(groupId,userId);
        return ResponseEntity.ok(profile);
    }

    // 3. 현재 그룹에서 내가 작성한 프로필 GET
    @Operation(summary = "3. 현재 그룹에서 내가 작성한 프로필 GET", description = "내 프로필 정보",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = GroupMemberProfileResponseDto.class)))})
    @GetMapping("/{groupId}/profile/my")
    public ResponseEntity<?> getSelfMemberProfile(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        GroupMemberProfileResponseDto profile = groupMemberService.getMemberProfile(groupId,userId);
        return ResponseEntity.ok(profile);
    }

    // 4. 그룹 다른멤버 프로필 조회
    @Operation(summary = "4. 그룹 다른멤버 프로필 조회", description = "같은 그룹의 멤버일 경우 해당 사용자의 프로필을 조회합니다. 5번 API에서 userId, memberId를 다 주기 때문에 memberId로 해주세요!",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = GroupMemberProfileResponseDto.class)))})
    @GetMapping("/profile/{memberId}")
    public ResponseEntity<?> getOtherMemberProfile(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer memberId) {
        Integer requesterId = Integer.parseInt(userDetails.getUsername());

        GroupMemberProfileResponseDto profile = groupMemberService.getMemberProfile(memberId);
        return ResponseEntity.ok(profile);
    }

    // 5. 유저가 그룹에 참여한 전체 멤버 리스트 GET
    @Operation(summary = "5. 유저가 그룹 전체 멤버 리스트 조회", description = "그룹에 속한 모든 멤버를 조회합니다.",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = GroupMemberResponseDto.class))))})
    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(
            @AuthenticationPrincipal UserDetails userDetails,  @PathVariable Integer groupId) {
        Integer requesterId = Integer.valueOf(userDetails.getUsername());

        // 요청자가 이 그룹에 속한 사용자 인지 확인
        boolean isMember = groupMemberService.isUserInGroup(groupId, requesterId);
        if (!isMember) {
            throw new IllegalArgumentException("해당 그룹에 접근할 권한이 없습니다.");
        }

        List<GroupMemberResponseDto> response = groupMemberService.getGroupMembers(requesterId, groupId, true);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "6. 유저 성향 궁합 보기", description = "로그인한 유저와 상대방 유저의 성향검사 결과를 기반으로 궁합 결과를 반환합니다.")
    @GetMapping("/fit-score/{otherUserId}")
    public ResponseEntity<CompatibilityResult> getCompatibilityResult(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer otherUserId) {
        Integer myUserId = Integer.parseInt(userDetails.getUsername());

        CompatibilityResult result = groupMemberService.getCompatibility(myUserId, otherUserId);
        return ResponseEntity.ok(result);
    }

}
