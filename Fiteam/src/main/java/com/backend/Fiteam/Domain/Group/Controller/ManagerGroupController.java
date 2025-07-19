package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.Domain.Group.Dto.CreateGroupRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupInviteRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupInvitedResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeSummaryDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupTeamTypeSettingDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupListDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupStatusDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerProfileResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.UpdateGroupRequestDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupNotice;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Service.GroupNoticeService;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import com.backend.Fiteam.Domain.Group.Service.ManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@PreAuthorize("hasAuthority('ROLE_MANAGER')")
@RestController
@RequestMapping("/v1/manager")
@RequiredArgsConstructor
@Tag(name = "7. ManagerGroupController- 매니저가 그룹 관리할때(Manager만 사용하는)")
public class ManagerGroupController {

    private final GroupNoticeService groupNoticeService;
    private final ManagerService managerService;
    private final GroupService groupService;
    /*
    1. 로그인한 매니저의 ID, 이름 반환
    2. 매니저 관리 그룹 ID·이름 리스트 조회
    3. 로그인한 매니저가 관리하는 그룹 정보를 전체 반환합니다.
    4. 로그인한 매니저가 관리하는 진행중인 그룹 정보를 반환합니다.
    5. 매니저가 그룹 생성
    5-2. 매니저가 그룹의 팀 구성 방식을 설정함.
    6. 그룹 정보 수정하기
    7. 매니져가 그룹 자체를 삭제하기…

    duplicate : 랜덤 자동 팀빌딩 지원. teamtype-positionbased = false 일 때
    */

    // 1. 로그인한 매니저의 ID, 이름 반환
    @Operation(summary = "1. 로그인한 매니저의 ID, 이름 반환", description = "로그인한 매니저의 ID와 이름을 반환합니다.")
    @GetMapping("/profile")
    public ResponseEntity<ManagerProfileResponseDto> getManagerBasicProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        ManagerProfileResponseDto basic = managerService.getManagerBasicProfile(managerId);
        return ResponseEntity.ok(basic);
    }

    // 2. 매니저 관리 그룹 ID·이름 리스트 조회
    @Operation(summary = "2. 매니저 관리 그룹 ID·이름 리스트 조회", description = "로그인한 매니저가 관리하는 모든 그룹의 ID와 이름을 간단히 반환합니다.",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ManagerGroupListDto.class))))})
    @GetMapping("/groups/id-name")
    public ResponseEntity<List<ManagerGroupListDto>> getManagerGroupList(@AuthenticationPrincipal UserDetails userDetails) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        List<ManagerGroupListDto> list = managerService.getManagerGroupList(managerId);
        return ResponseEntity.ok(list);
    }

    // 3. 로그인한 매니저가 관리하는 그룹 정보를 전체 반환합니다.
    @Operation(summary = "3. 로그인한 매니저가 관리하는 그룹 정보를 전체 반환합니다.", description = "로그인한 매니저가 관리 중인 그룹의 이름, 참여자 수, 팀 빌딩 타입, 현재 진행 상태를 반환합니다.",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ManagerGroupStatusDto.class))))})
    @GetMapping("/groups/all")
    public ResponseEntity<List<ManagerGroupStatusDto>> getManagedGroupStatuses(@AuthenticationPrincipal UserDetails userDetails) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        List<ManagerGroupStatusDto> statuses = managerService.getManagedGroupStatuses(managerId);
        return ResponseEntity.ok(statuses);
    }

    // 4. 로그인한 매니저가 관리하는 진행중인 그룹 정보를 반환합니다.
    @Operation(summary = "4. 로그인한 매니저가 관리하는 진행중인 그룹 정보를 반환합니다.", description = "로그인한 매니저가 관리 중인(팀 빌딩 종료 전) 그룹 목록을 반환합니다.",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ManagerGroupResponseDto.class))))})
    @GetMapping("/groups/process")
    public ResponseEntity<List<ManagerGroupResponseDto>> getManagedGroups(@AuthenticationPrincipal UserDetails userDetails) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        List<ManagerGroupResponseDto> groups = managerService.getManagedGroups(managerId);
        return ResponseEntity.ok(groups);
    }

    // 5. 매니저가 새로 그룹 생성
    @Operation(summary = "5. 매니저가 새로 그룹 생성", description = "매니저 권한이 있는 사용자가 새로운 프로젝트 그룹을 생성합니다. " +
            "같은 매니저가 이미 생성한 그룹 중 동일한 이름이 있을 경우 400 에러를 반환합니다.")
    @PostMapping("/group/create")
    public ResponseEntity<Integer> createGroup(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateGroupRequestDto requestDto) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        Integer newGroupId = managerService.createGroup(managerId, requestDto);
        return ResponseEntity.ok().body(newGroupId);
    }

    // 5. 매니저가 그룹의 팀 구성 방식을 설정함.
    @Operation(summary = "5-2. 매니저가 그룹의 팀 구성 방식을 설정함.", description = "특정 그룹에 팀 빌딩 방식을 설정합니다.")
    @PostMapping("/group/{groupId}/set-teamtype")
    public ResponseEntity<String> setTeamType(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId, @RequestBody GroupTeamTypeSettingDto requestDto) throws SchedulerException {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        managerService.authorizeManager(groupId, managerId);

        groupService.setTeamType(groupId, requestDto);
        return ResponseEntity.ok().body("팀 구성 방식을 설정했습니다.");
    }

    // 6. 그룹 정보 수정하기
    @Operation(summary = "4. 그룹 정보 수정", description = "매니저가 그룹 정보를 수정합니다. (수정하지 않는 필드는 null로 전달)")
    @PatchMapping("/group/{groupId}/update")
    public ResponseEntity<String> updateGroupInfo(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId, @RequestBody UpdateGroupRequestDto requestDto) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        managerService.authorizeManager(groupId, managerId);

        managerService.updateGroup(groupId, requestDto);

        return ResponseEntity.ok().body("그룹 정보가 수정되었습니다.");
    }

    // 7. 매니져가 그룹 자체를 삭제하기…
    @Operation(summary = "7. 그룹 삭제- 사용시 주의", description = "매니저 권한이 있는 사용자가 해당 그룹을 완전 삭제합니다. (멤버·팀·설정 모두 삭제)")
    @DeleteMapping("group/{groupId}")
    public ResponseEntity<?> deleteGroup(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        try {
            Integer managerId = Integer.valueOf(userDetails.getUsername());
            managerService.authorizeManager(groupId, managerId);

            // 삭제 로직 실행
            managerService.deleteGroup(groupId);
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

    // 6. 공지 생성
    @Operation(summary = "공지 생성", description = "로그인한 매니저가 자신이 관리하는 그룹에 새 공지를 작성합니다.")
    @PostMapping("/new-notice")
    public ResponseEntity<GroupNotice> createNotice(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody GroupNoticeRequestDto dto) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        managerService.authorizeManager(dto.getGroupId(), managerId);

        GroupNotice saved = groupNoticeService.createNotice(managerId, dto);
        return ResponseEntity.ok(saved);
    }

    // 7. 공지 수정
    @Operation(summary = "공지 수정", description = "로그인한 매니저가 자신이 작성한 공지를 수정합니다.")
    @PatchMapping("/notices/{noticeId}")
    public ResponseEntity<GroupNotice> updateNotice(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer noticeId, @RequestBody GroupNoticeRequestDto dto) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());

        managerService.authorizeManager(dto.getGroupId(), managerId);
        GroupNotice updated = groupNoticeService.updateNotice(managerId, noticeId, dto);
        return ResponseEntity.ok(updated);
    }

    // 8. 공지 삭제
    @Operation(summary = "공지 삭제", description = "로그인한 매니저가 자신이 작성한 공지를 삭제합니다.")
    @DeleteMapping("/notices/{noticeId}")
    public ResponseEntity<String> deleteNotice(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer noticeId) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());

        groupNoticeService.deleteNotice(managerId, noticeId);
        return ResponseEntity.ok("공지를 삭제했습니다.");
    }

    // 9. 최근 공지목록 가져오기
    @Operation(summary = "작성한 공지 목록 조회", description = "로그인한 매니저가 자신이 작성한 모든 공지를 최신 순으로 반환합니다.")
    @GetMapping("/notices")
    public ResponseEntity<List<GroupNoticeSummaryDto>> getMyNotices(@AuthenticationPrincipal UserDetails userDetails) {
        Integer managerId = Integer.valueOf(userDetails.getUsername());
        List<GroupNoticeSummaryDto> list = groupNoticeService.getNoticesByManager(managerId);
        return ResponseEntity.ok(list);
    }

}

