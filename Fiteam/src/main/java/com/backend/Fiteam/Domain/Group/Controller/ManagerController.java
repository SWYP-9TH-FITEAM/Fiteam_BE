package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.Domain.Admin.Dto.SystemNoticeResponseDto;
import com.backend.Fiteam.Domain.Admin.Service.AdminService;
import com.backend.Fiteam.Domain.Group.Dto.GroupMemberResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeDetailDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeSummaryDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupListDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupStatusDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerProfileResponseDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupNotice;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Group.Service.GroupMemberService;
import com.backend.Fiteam.Domain.Group.Service.GroupNoticeService;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import com.backend.Fiteam.Domain.Group.Service.ManagerService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/manager")
@RequiredArgsConstructor
@Tag(name = "8. ManagerController - 로그인한 Manager")
public class ManagerController {

    private final ManagerService managerService;
    private final ProjectGroupRepository projectGroupRepository;
    private final GroupMemberService groupMemberService;
    private final GroupNoticeService groupNoticeService;
    private final AdminService adminService;
    private final Scheduler scheduler;
    /*
    1. 로그인한 매니저의 ID, 이름 반환
    2. 로그인한 매니저가 관리하는 그룹 정보를 반환합니다.
    3. 로그인한 매니저가 관리하는 그룹 정보를 전체 반환합니다.
    4. 매니저 관리 그룹 ID·이름 리스트 조회
    5. 매니저가 그룹 멤버 리스트 조회
    6.
     */

    private void authorizeManager(UserDetails userDetails) {
        boolean isManager = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_Manager"));
        if (!isManager) {
            throw new IllegalArgumentException("매니저 권한이 없습니다.");
        }
    }
    private void authorizeManager(Integer groupId, Integer managerId) {
        ProjectGroup group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found with id: " + groupId));
        if (!group.getManagerId().equals(managerId)) {
            throw new IllegalArgumentException("이 그룹을 관리할 권한이 없습니다.");
        }
    }

    // 1. 로그인한 매니저의 ID, 이름 반환
    @Operation(summary = "1. 로그인한 매니저의 ID, 이름 반환", description = "로그인한 매니저의 ID와 이름을 반환합니다.")
    @GetMapping("/name")
    public ResponseEntity<ManagerProfileResponseDto> getManagerBasicProfile(@AuthenticationPrincipal UserDetails userDetails) {
        authorizeManager(userDetails);
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        ManagerProfileResponseDto basic = managerService.getManagerBasicProfile(managerId);
        return ResponseEntity.ok(basic);
    }

    // 2. 로그인한 매니저가 관리하는 진행중인 그룹 정보를 반환합니다.
    @Operation(summary = "2. 로그인한 매니저가 관리하는 진행중인 그룹 정보를 반환합니다.", description = "로그인한 매니저가 관리 중인(팀 빌딩 종료 전) 그룹 목록을 반환합니다.",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ManagerGroupResponseDto.class))))})
    @GetMapping("/groups/process")
    public ResponseEntity<List<ManagerGroupResponseDto>> getManagedGroups(@AuthenticationPrincipal UserDetails userDetails) {
        authorizeManager(userDetails);
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        List<ManagerGroupResponseDto> groups = managerService.getManagedGroups(managerId);
        return ResponseEntity.ok(groups);
    }

    // 3. 로그인한 매니저가 관리하는 그룹 정보를 전체 반환합니다.
    @Operation(summary = "3. 로그인한 매니저가 관리하는 그룹 정보를 전체 반환합니다.", description = "로그인한 매니저가 관리 중인 그룹의 이름, 참여자 수, 팀 빌딩 타입, 현재 진행 상태를 반환합니다.",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ManagerGroupStatusDto.class))))})
    @GetMapping("/groups/all")
    public ResponseEntity<List<ManagerGroupStatusDto>> getManagedGroupStatuses(@AuthenticationPrincipal UserDetails userDetails) {
        authorizeManager(userDetails);
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        List<ManagerGroupStatusDto> statuses = managerService.getManagedGroupStatuses(managerId);
        return ResponseEntity.ok(statuses);
    }

    // 4. 매니저 관리 그룹 ID·이름 리스트 조회
    @Operation(summary = "매니저 관리 그룹 ID·이름 리스트 조회", description = "로그인한 매니저가 관리하는 모든 그룹의 ID와 이름을 간단히 반환합니다.",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ManagerGroupListDto.class))))})
    @GetMapping("/groups/id-name")
    public ResponseEntity<List<ManagerGroupListDto>> getManagerGroupList(
            @AuthenticationPrincipal UserDetails userDetails) {
        authorizeManager(userDetails);
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        List<ManagerGroupListDto> list = managerService.getManagerGroupList(managerId);
        return ResponseEntity.ok(list);
    }

    // 5. 매니저가 그룹 멤버 리스트 조회
    @Operation(summary = "5. 매니저가 그룹 멤버 리스트 조회", description = "매니저 권한이 있는 사용자가 자신이 관리하는 그룹의 모든 멤버를 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = GroupMemberResponseDto.class))))})
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponseDto>> getGroupMembersByManager(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        // 1) 매니저 권한 및 그룹 관리 권한 검증
        authorizeManager(userDetails);
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        authorizeManager(groupId, managerId);

        List<GroupMemberResponseDto> response =
                groupMemberService.getGroupMembers(managerId, groupId, false);

        return ResponseEntity.ok(response);
    }

    // 6. 공지 생성
    @Operation(summary = "공지 생성", description = "로그인한 매니저가 자신이 관리하는 그룹에 새 공지를 작성합니다.")
    @PostMapping("/new-notice")
    public ResponseEntity<GroupNotice> createNotice(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody GroupNoticeRequestDto dto) {
        authorizeManager(userDetails);
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        authorizeManager(dto.getGroupId(), managerId);

        GroupNotice saved = groupNoticeService.createNotice(managerId, dto);
        return ResponseEntity.ok(saved);
    }

    // 7. 공지 수정
    @Operation(summary = "공지 수정", description = "로그인한 매니저가 자신이 작성한 공지를 수정합니다.")
    @PatchMapping("/notices/{noticeId}")
    public ResponseEntity<GroupNotice> updateNotice(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer noticeId, @RequestBody GroupNoticeRequestDto dto) {
        authorizeManager(userDetails);
        Integer managerId = Integer.valueOf(userDetails.getUsername());

        GroupNotice updated = groupNoticeService.updateNotice(managerId, noticeId, dto);
        return ResponseEntity.ok(updated);
    }

    // 8. 공지 삭제
    @Operation(summary = "공지 삭제", description = "로그인한 매니저가 자신이 작성한 공지를 삭제합니다.")
    @DeleteMapping("/notices/{noticeId}")
    public ResponseEntity<Void> deleteNotice(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer noticeId) {
        authorizeManager(userDetails);
        Integer managerId = Integer.valueOf(userDetails.getUsername());

        groupNoticeService.deleteNotice(managerId, noticeId);
        return ResponseEntity.ok().build();
    }

    // 9. 최근 공지목록 가져오기
    @Operation(summary = "작성한 공지 목록 조회", description = "로그인한 매니저가 자신이 작성한 모든 공지를 최신 순으로 반환합니다.")
    @GetMapping("/notices")
    public ResponseEntity<List<GroupNoticeSummaryDto>> getMyNotices(@AuthenticationPrincipal UserDetails userDetails) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        List<GroupNoticeSummaryDto> list = groupNoticeService.getNoticesByManager(managerId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "공지 상세 조회", description = "관리자가 작성한 특정 공지의 전체 내용을 반환합니다.")
    @GetMapping("/notices/{noticeId}")
    public ResponseEntity<GroupNoticeDetailDto> getNoticeById(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer noticeId) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        GroupNoticeDetailDto dto = groupNoticeService.getNoticeById(noticeId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "new-시스템 공지사항 조회", description = "등록된 모든 시스템 공지사항 목록을 반환합니다.")
    @GetMapping("/system/notices")
    public ResponseEntity<List<SystemNoticeResponseDto>> getSystemNotices() {
        List<SystemNoticeResponseDto> list = adminService.getAllSystemNotices();
        return ResponseEntity.ok(list);
    }


    @Operation(summary = "팀 빌딩 시작(수동)", description = "Quartz에 등록된 시작 잡을 즉시 실행합니다.")
    @PostMapping("/{groupId}/start")
    @PreAuthorize("hasAuthority('Manager')")
    public ResponseEntity<Void> startBuildingManually(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId
    ) throws SchedulerException {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        authorizeManager(groupId, managerId);

        JobKey startKey = JobKey.jobKey("job_group_" + groupId + "_start", "teamBuilding");
        if (!scheduler.checkExists(startKey)) {
            throw new IllegalStateException("팀 빌딩 시작이 불가능합니다. 이미 시작되었거나 시작 잡이 없습니다.");
        }

        // 1) 즉시 실행
        scheduler.triggerJob(startKey);
        // 2) 더 이상 재실행되지 않도록 잡 삭제
        scheduler.deleteJob(startKey);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "팀 빌딩 종료(수동)", description = "Quartz에 등록된 종료 잡을 즉시 실행합니다.")
    @PostMapping("/{groupId}/end")
    @PreAuthorize("hasAuthority('Manager')")
    public ResponseEntity<Void> endBuildingManually(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId
    ) throws SchedulerException {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        authorizeManager(groupId, managerId);

        JobKey endKey = JobKey.jobKey("job_group_" + groupId + "_end", "teamBuilding");

        if (!scheduler.checkExists(endKey)) {
            throw new IllegalStateException("팀 빌딩 종료가 불가능합니다. 이미 종료되었거나 종료 잡이 없습니다.");
        }

        scheduler.triggerJob(endKey);
        scheduler.deleteJob(endKey);
        return ResponseEntity.ok().build();
    }
}
