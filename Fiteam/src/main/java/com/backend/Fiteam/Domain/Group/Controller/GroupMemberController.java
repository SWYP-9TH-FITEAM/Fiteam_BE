package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.Domain.Group.Dto.GroupMemberProfileResponseDto;
import com.backend.Fiteam.Domain.Group.Service.GroupMemberService;
import com.backend.Fiteam.Domain.User.Dto.UserGroupProfileDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
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
public class GroupMemberController {

    /*
    1. 직무 유형 리스트 GET,POST (PM,디자이너 등)	GET	/v1/groupmember/positions
    2. 해당그룹 프로필 작성 (경력/목표/URL/소개 작성)	PATCH	/v1/groupmember/{groupMemberId}
    3. 그룹 다른멤버 프로필 조회	GET	/v1/member/{userId}/profile
    */

    private final GroupMemberService groupMemberService;

    // 1. 직무 유형 리스트 GET (PM,디자이너 등)
    @Operation(summary = "유저가 직무 유형 리스트 조회(PM, DS, FE, BE 등등)", description = "teamMakeType 기준으로 TeamType의 configJson을 파싱하여 직무(position) 리스트를 반환합니다.")
    @GetMapping("/{groupId}/positions")
    public ResponseEntity<?> getGroupPositions(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            groupMemberService.validateGroupMembership(userId, groupId); // 멤버 여부 확인

            List<String> positions = groupMemberService.getPositionListForGroup(groupId);
            return ResponseEntity.ok(positions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2. 해당그룹 프로필 작성 (경력/목표/URL/소개 작성)
    @Operation(summary = "그룹멤버 프로필 수정", description = "초대 수락 후에 그룹 멤버가 자신의 프로필 정보를 수정합니다. (수정 안하는 필드는 null로 입력해주세요")
    @PatchMapping("/groupprofile/{groupMemberId}")
    public ResponseEntity<?> updateGroupMemberProfile(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupMemberId, @RequestBody UserGroupProfileDto requestDto) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            groupMemberService.updateGroupMemberProfile(groupMemberId, userId, requestDto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    // 3. 그룹 다른멤버 프로필 조회
    @Operation(summary = "다른 멤버 프로필 조회", description = "같은 그룹의 멤버일 경우 해당 사용자의 프로필을 조회합니다.")
    @GetMapping("/member/{userId}/profile")
    public ResponseEntity<?> getOtherMemberProfile(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer userId) {
        try {
            Integer requesterId = Integer.parseInt(userDetails.getUsername());

            GroupMemberProfileResponseDto profile = groupMemberService.getMemberProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
