package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.Domain.Group.Dto.CreateGroupRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupInviteRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupInvitedResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupTeamTypeSettingDto;
import com.backend.Fiteam.Domain.Group.Dto.UpdateGroupRequestDto;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/v1/group")
@RequiredArgsConstructor
public class ManagerController {

    private final GroupService groupService;

    /*
    1. 매니저가 그룹 생성
    2. 매니저가 그룹의 팀 구성 방식을 설정함.
    3. 매니저가 이메일로 그룹에 유저 초대(1~N명)
    4. 그룹 정보 수정하기
    5. 랜덤 자동 팀빌딩 지원. teamtype-positionbased = false 일 때
    7. 매니저가 그룹 멤버 Ban 하기
    8. 매니저가 요청한 그룹 참여 취소하기(잘못 입력하거나 할때)
    9. 매니져가 그룹 자체를 삭제하기…

    10. 관리중인 그룹 정보 가져오기-> 그룹별로 팀빌딩 메인 페이지
    11. 팀 상세보기
    */

    // UserDetails 같은 경우 매니져가 로그인한 상태이면 managerId 로 생각하면 된다.

    private void authorizeManager(UserDetails userDetails) {
        boolean isManager = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("Manager"));
        if (!isManager) {
            throw new IllegalArgumentException("매니저 권한이 없습니다.");
        }
    }
    private void authorizeManager(Integer groupId, Integer managerId) {
        ProjectGroup group = groupService.getProjectGroup(groupId);
        if (!group.getManagerId().equals(managerId)) {
            throw new IllegalArgumentException("이 그룹을 관리할 권한이 없습니다.");
        }
    }

    //1. 매니저가 그룹 생성
    @Operation(summary = "1. 매니저가 그룹 생성", description = "매니저 권한이 있는 사용자가 새로운 프로젝트 그룹을 생성합니다. " +
            "같은 매니저가 이미 생성한 그룹 중 동일한 이름이 있을 경우 400 에러를 반환합니다.")
    @PostMapping("/create")
    public ResponseEntity<String> createGroup(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody CreateGroupRequestDto requestDto) {
        try {
            // 1) 매니저 여부 확인
            authorizeManager(userDetails);
            Integer managerId = Integer.valueOf(userDetails.getUsername());  // Manager 테이블의 id

            groupService.createGroup(managerId, requestDto);  // Manager id 넘기기
            return ResponseEntity.ok().body("그룹을 생성했습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2. 매니저가 그룹의 팀 구성 방식을 설정함.
    @Operation(summary = "2. 매니저가 그룹의 팀 구성 방식을 설정함.", description = "특정 그룹에 팀 빌딩 방식을 설정합니다.")
    @PostMapping("/set-teamtype/{groupId}")
    public ResponseEntity<String> setTeamType(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId, @RequestBody GroupTeamTypeSettingDto requestDto) {
        try {
            // 1) 매니저 권한 확인
            authorizeManager(userDetails);
            Integer managerId = Integer.valueOf(userDetails.getUsername());
            authorizeManager(groupId, managerId);

            // 팀타입 설정 진행
            groupService.setTeamType(groupId, requestDto);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 그룹입니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 3. 매니저가 이메일로 유저 초대(1~N명)
    @Operation(summary = "3. 매니저가 이메일로 그룹에 유저 초대(1~N명)", description = "Manager가 여러 사용자를 프로젝트 그룹에 초대합니다.")
    @PostMapping("/invite")
    public ResponseEntity<?> inviteUsersToGroup(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody GroupInviteRequestDto requestDto) {
        try {
            authorizeManager(userDetails);
            Integer managerId = Integer.valueOf(userDetails.getUsername());
            authorizeManager(requestDto.getGroupId(), managerId);

            GroupInvitedResponseDto responseDto = groupService.inviteUsersToGroup(requestDto.getGroupId(), requestDto.getEmails());
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4. 그룹 정보 수정하기
    @Operation(summary = "4. 그룹 정보 수정", description = "매니저가 그룹 정보를 수정합니다. (수정하지 않는 필드는 null로 전달)")
    @PatchMapping("/{groupId}/update")
    public ResponseEntity<String> updateGroupInfo(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId, @RequestBody UpdateGroupRequestDto requestDto) {
        try {
            authorizeManager(userDetails);
            Integer managerId = Integer.valueOf(userDetails.getUsername());
            authorizeManager(groupId, managerId);

            ProjectGroup projectGroup = groupService.getProjectGroup(groupId);
            groupService.updateGroup(projectGroup, requestDto);

            return ResponseEntity.ok().body("그룹 정보가 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 5. 랜덤 자동 팀빌딩 지원 -> 시작시간 설정 대규모 테스트 필요
    @Operation(summary = "5. 랜덤 자동 팀빌딩 지원(미완성)", description = "시작시간 설정 대규모 테스트 필요함")
    @PostMapping("/{groupId}/random-team-building")
    public ResponseEntity<?> randomTeamBuilding(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        try {
            authorizeManager(userDetails);
            Integer managerId = Integer.valueOf(userDetails.getUsername());
            authorizeManager(groupId, managerId);

            ProjectGroup projectGroup = groupService.getProjectGroup(groupId);
            groupService.RandomTeamBuilding(projectGroup);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 6. 매니저가 그룹 멤버 Ban 하기
    @Operation(summary = "6. 매니저가 그룹 멤버 Ban 하기", description = "매니저 권한이 있는 사용자가 그룹 멤버를 Ban 처리합니다.")
    @PatchMapping("/{groupId}/member/{groupMemberId}/ban")
    public ResponseEntity<String> banGroupMember(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId, @PathVariable Integer groupMemberId) {
        try {
            authorizeManager(userDetails);
            Integer managerId = Integer.valueOf(userDetails.getUsername());
            authorizeManager(groupId, managerId);

            // 2) 차단 로직 실행
            groupService.banGroupMember(groupId, groupMemberId);
            return ResponseEntity.ok().body("차단했습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 7. 매니저가 요청한 그룹 참여 취소하기(잘못 입력하거나 할때)
    @Operation(summary = "7. 매니저가 요청한 그룹 참여 취소하기(잘못 입력하거나 할때)", description = "매니저 권한이 있는 사용자가 아직 수락되지 않은 초대를 사용자 이메일로 취소합니다.")
    @DeleteMapping("/{groupId}/member/cancel")
    public ResponseEntity<String> cancelGroupInvitationByEmail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId, @RequestParam String email) {
        try {
            authorizeManager(userDetails);
            Integer managerId = Integer.valueOf(userDetails.getUsername());
            authorizeManager(groupId, managerId);

            // 이메일 기준 취소 로직
            groupService.cancelGroupInvitationByEmail(groupId, email);
            return ResponseEntity.ok().body("사용자 초대를 취소했습니다.");

        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 8. 매니져가 그룹 자체를 삭제하기…
    @Operation(summary = "그룹 삭제- 사용시 주의", description = "매니저 권한이 있는 사용자가 해당 그룹을 완전 삭제합니다. (멤버·팀·설정 모두 삭제)")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId) {
        try {
            authorizeManager(userDetails);
            Integer managerId = Integer.valueOf(userDetails.getUsername());
            authorizeManager(groupId, managerId);

            // 삭제 로직 실행
            groupService.deleteGroup(groupId);
            return ResponseEntity.ok().build();

        } catch (NoSuchElementException e) {
            // 그룹이 없을 때
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IllegalArgumentException e) {
            // 권한 없을 때 403
            if ("이 그룹을 관리할 권한이 없습니다.".equals(e.getMessage())) {
                return ResponseEntity
                        .status(HttpServletResponse.SC_FORBIDDEN)
                        .body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

