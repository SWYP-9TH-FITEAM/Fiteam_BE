package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import com.backend.Fiteam.Domain.Group.Service.ManagerService;
import com.backend.Fiteam.Domain.Group.Service.ManagerUserService;
import com.backend.Fiteam.Domain.Group.Service.TeamBuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@PreAuthorize("hasRole('Manager')")
@RestController
@RequestMapping("/v1/manager")
@RequiredArgsConstructor
@Tag(name = "8. ManagerUserController - 로그인한 Manager")
public class ManagerTeamBuildingController {

    private final ManagerService managerService;
    private final ProjectGroupRepository projectGroupRepository;
    private final Scheduler scheduler;
    private final GroupService groupService;
    private final TeamBuildingService teamBuildingService;

    /*
    1. 팀 빌딩 시작(수동). Quartz에 등록된 시작 잡을 즉시 실행
    2. 팀 빌딩 종료(수동). Quartz에 등록된 시작 잡을 즉시 실행

    duplicate: 랜덤 자동 팀빌딩 지원 -> 시작시간 설정 대규모 테스트 필요
    */

    //------------- duplicate --------------
    // 랜덤 자동 팀빌딩 지원 -> 시작시간 설정 대규모 테스트 필요
    @Operation(summary = "랜덤 자동 팀빌딩 지원- 이거는 테스트 용으로, FE에서 직접호출 아닙니다", description = "시작시간 설정 대규모 테스트 필요함")
    @PostMapping("/{groupId}/team-building/random")
    public ResponseEntity<?> randomTeamBuilding(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        managerService.authorizeManager(groupId, managerId);

        ProjectGroup projectGroup = groupService.getProjectGroup(groupId);
        teamBuildingService.RandomTeamBuilding(projectGroup);

        return ResponseEntity.ok().build();
    }
    //---------------------------------------

    // 1. 팀 빌딩 시작(수동). Quartz에 등록된 시작 잡을 즉시 실행
    @Operation(summary = "1. 팀 빌딩 시작(수동)", description = "Quartz에 등록된 시작 잡을 즉시 실행합니다.")
    @PostMapping("/{groupId}/team-building/start")
    public ResponseEntity<String> startBuildingManually(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId
    ) throws SchedulerException {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        managerService.authorizeManager(groupId, managerId);

        // Quartz에 예약된 잡이 남아 있으면 제거
        JobKey startKey = JobKey.jobKey("job_group_" + groupId + "_start", "teamBuilding");
        if (scheduler.checkExists(startKey)) {
            // 그룹, TeamType 정보 로드
            ProjectGroup group = projectGroupRepository.findById(groupId)
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다: " + groupId));
            TeamType tt = group.getTeamMakeType();

            if (Boolean.TRUE.equals(tt.getPositionBased())) {
                // 포지션 기반: Quartz 트리거 대신 직접 호출
                teamBuildingService.openPositionBasedRequests(group);
            } else {
                // 랜덤 빌딩: 직접 호출
                teamBuildingService.RandomTeamBuilding(group);
            }

            scheduler.deleteJob(startKey);
            return ResponseEntity.ok("팀 빌딩을 지금 시작합니다.");
        }else{
            return ResponseEntity.ok("이미 팀 빌딩이 시작되어있습니다.");
        }

    }

    // 2. 팀 빌딩 종료(수동)
    @Operation(summary = "2. 팀 빌딩 종료(수동)", description = "Quartz에 등록된 종료 잡을 즉시 실행합니다.")
    @PostMapping("/{groupId}/team-building/end")
    public ResponseEntity<String> endBuildingManually(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId
    ) throws SchedulerException {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        managerService.authorizeManager(groupId, managerId);

        JobKey endKey = JobKey.jobKey("job_group_" + groupId + "_end", "teamBuilding");
        if (scheduler.checkExists(endKey)) {
            ProjectGroup group = projectGroupRepository.findById(groupId)
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다: " + groupId));

            teamBuildingService.closeTeamBuilding(group);
            scheduler.deleteJob(endKey);
            return ResponseEntity.ok("팀 빌딩을 종료합니다.");
        }else {
            return ResponseEntity.ok("이미 팀 빌딩이 종료되었습니다.");
        }


    }
}
