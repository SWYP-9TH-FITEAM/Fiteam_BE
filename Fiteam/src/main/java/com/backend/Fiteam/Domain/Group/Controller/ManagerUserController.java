package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.Domain.Group.Dto.GroupInviteRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupInvitedResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupMemberResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupTeamTypeSettingDto;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Group.Service.GroupMemberService;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import com.backend.Fiteam.Domain.Group.Service.ManagerService;
import com.backend.Fiteam.Domain.Group.Service.ManagerUserService;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@PreAuthorize("hasAuthority('ROLE_MANAGER')")
@RestController
@RequestMapping("/v1/manager/user")
@RequiredArgsConstructor
@Tag(name = "8. ManagerUserController - 로그인한 Manager")
public class ManagerUserController {

    private final ManagerService managerService;
    private final ManagerUserService managerUserService;


    /*
    1. 매니저가 그룹 멤버 리스트 조회
    2. 매니저가 이메일로 그룹에 유저 초대(1~N명)
    3. 매니저가 요청한 그룹 참여 취소하기(잘못 입력하거나 할때)
    4. 매니저가 그룹 멤버 Ban 하기
    5. 매니저가 그룹의 팀 구성 방식을 설정함.
    */

    /*
    // 1. 매니저가 그룹 멤버 리스트 조회
    @Operation(summary = "1. 매니저가 그룹 멤버 리스트 조회",
            description = "매니저 권한이 있는 사용자가 자신이 관리하는 그룹의 모든 멤버를 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(array = @ArraySchema(
            schema = @Schema(implementation = GroupMemberResponseDto.class))))})
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponseDto>> getGroupMembersByManager(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId) {
        // 1) 매니저 권한 및 그룹 관리 권한 검증
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        managerService.authorizeManager(groupId, managerId);

        List<GroupMemberResponseDto> response = groupService.getGroupMembers(managerId, groupId, false);
        return ResponseEntity.ok(response);
    }
    */

    // 2. 매니저가 이메일로 유저 초대(1~N명)
    @Operation(summary = "2. 매니저가 이메일로 그룹에 유저 초대(1~N명)", description = "Manager가 여러 사용자를 프로젝트 그룹에 초대합니다.")
    @PostMapping("/invite")
    public ResponseEntity<GroupInvitedResponseDto> inviteUsersToGroup(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody GroupInviteRequestDto requestDto) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        managerService.authorizeManager(requestDto.getGroupId(), managerId);

        GroupInvitedResponseDto responseDto = managerUserService.inviteUsersToGroup(requestDto.getGroupId(), requestDto.getEmails(), managerId);
        return ResponseEntity.ok(responseDto);
    }

    // 3. 매니저가 요청한 그룹 참여 취소하기(잘못 입력하거나 할때)
    @Operation(summary = "3. 매니저가 사용자에게 보낸 그룹 초대 취소", description = "매니저 권한이 있는 사용자가 아직 수락되지 않은 초대를 사용자 이메일로 취소합니다.",
            parameters = {@Parameter(name = "groupId", in = ParameterIn.PATH, description = "초대를 취소할 그룹 ID", required = true, example = "3"),
                    @Parameter(name = "email", in = ParameterIn.QUERY, description = "취소할 사용자 이메일", required = true, example = "user@example.com")})
    @DeleteMapping("/{groupId}/cancel")
    public ResponseEntity<String> cancelGroupInvitationByEmail(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId, @RequestParam String email) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        managerService.authorizeManager(groupId, managerId);

        // 이메일 기준 취소 로직
        managerUserService.cancelGroupInvitationByEmail(groupId, email);
        return ResponseEntity.ok().body("사용자 초대를 취소했습니다.");
    }

    // 4. 매니저가 그룹 멤버 Ban 하기
    @Operation(summary = "4. 매니저가 그룹에 참여한 멤버 Ban 하기", description = "매니저 권한이 있는 사용자가 그룹 멤버를 Ban 처리합니다.")
    @PatchMapping("/{groupId}/ban/{groupMemberId}")
    public ResponseEntity<String> banGroupMember(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId, @PathVariable Integer groupMemberId) {

        Integer managerId = Integer.valueOf(userDetails.getUsername());
        managerService.authorizeManager(groupId, managerId);

        // 2) 차단 로직 실행
        managerUserService.banGroupMember(groupId, groupMemberId);
        return ResponseEntity.ok().body("차단했습니다.");
    }




}
