package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.Domain.Group.Dto.CreateGroupRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupInviteRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupInvitedResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupMemberResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupTeamTypeSettingDto;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/v1/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /*
    1. 매니저가 그룹 생성
    2. 매니저가 그룹의 팀 구성 방식을 설정함.
    3. 매니저가 이메일로 유저 초대(1~N명)
    4. 그룹에 참여한 전체 멤버 리스트 GET
    */

    // UserDetails 같은 경우 매니져가 로그인한 상태이면 managerId 로 생각하면 된다.

    //1. 매니저가 그룹 생성
    @PostMapping("/create")
    public ResponseEntity<?> createGroup(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody CreateGroupRequestDto requestDto) {
        try {
            Integer managerId = Integer.valueOf(userDetails.getUsername());  // Manager 테이블의 id

            groupService.createGroup(managerId, requestDto);  // Manager id 넘기기
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2. 매니저가 그룹의 팀 구성 방식을 설정함.
    @Operation(summary = "팀 빌딩 타입 설정", description = "특정 그룹에 팀 빌딩 방식을 설정합니다.")
    @PostMapping("/set-teamtype/{groupId}")
    public ResponseEntity<?> setTeamType(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId, @RequestBody GroupTeamTypeSettingDto requestDto) {
        try {
            Integer managerId = Integer.valueOf(userDetails.getUsername());

            // 그룹 조회
            ProjectGroup projectGroup = groupService.getProjectGroup(groupId);

            // 매니저 ID 검증
            if (!projectGroup.getManagerId().equals(managerId)) {
                return ResponseEntity.status(HttpServletResponse.SC_FORBIDDEN)
                        .body("로그인한 매니져가 관리하는 그룹이 아닙니다.");
            }

            // 팀타입 설정 진행
            groupService.setTeamType(groupId, requestDto);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 그룹입니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 3. 매니저가 이메일로 유저 초대(1~N명)
    @Operation(summary = "그룹에 이메일로 유저 초대 (1~N명 가능)", description = "Manager가 여러 사용자를 프로젝트 그룹에 초대합니다.")
    @PostMapping("/invite")
    public ResponseEntity<?> inviteUsersToGroup(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody GroupInviteRequestDto requestDto) {
        try {
            Integer managerId = Integer.valueOf(userDetails.getUsername());

            ProjectGroup group = groupService.getProjectGroup(requestDto.getGroupId());
            if (!group.getManagerId().equals(managerId)) {
                return ResponseEntity.status(HttpServletResponse.SC_FORBIDDEN)
                        .body("이 그룹을 관리할 권한이 없습니다.");
            }

            GroupInvitedResponseDto responseDto = groupService.inviteUsersToGroup(requestDto.getGroupId(), requestDto.getEmails());
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4. 그룹에 참여한 전체 멤버 리스트 GET
    @Operation(summary = "그룹 전체 멤버 리스트 조회", description = "그룹에 속한 모든 멤버를 조회합니다.")
    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(
            @AuthenticationPrincipal UserDetails userDetails,  @PathVariable Integer groupId) {
        try {
            Integer requesterId = Integer.valueOf(userDetails.getUsername());

            // 1. 그룹 존재 여부 확인
            ProjectGroup group = groupService.getProjectGroup(groupId);

            // 2. 요청자가 이 그룹에 속한 사용자 or 매니저인지 확인
            boolean isManager = group.getManagerId().equals(requesterId);
            boolean isMember = groupService.isUserInGroup(groupId, requesterId);

            if (!isManager && !isMember) {
                throw new IllegalArgumentException("해당 그룹에 접근할 권한이 없습니다.");
            }

            List<GroupMemberResponseDto> response = groupService.getGroupMembers(groupId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}

