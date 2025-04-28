package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.Domain.Group.Service.GroupMemberService;
import com.backend.Fiteam.Domain.User.Dto.UserGroupProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    3. 그룹 다른멤버 프로필 조회	GET	/v1/groupmember/{groupMemberId}/profile
    4. 다른 유저 상세 프로필 보기	GET	/v1/team/user-profile/{userId}
    */

    private final GroupMemberService groupMemberService;

    // 2. 해당그룹 프로필 작성 (경력/목표/URL/소개 작성)
    @Operation(summary = "그룹멤버 프로필 수정", description = "초대 수락 후에 그룹 멤버가 자신의 프로필 정보를 수정합니다. (수정 안하는 필드는 null로 입력해주세요")
    @PatchMapping("/groupprofile/{groupMemberId}")
    public ResponseEntity<?> updateGroupMemberProfile(
            @PathVariable Integer groupMemberId, @RequestBody UserGroupProfileDto requestDto, @AuthenticationPrincipal UserDetails userDetails) {
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
}
