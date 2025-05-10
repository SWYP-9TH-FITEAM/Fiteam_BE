package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.Domain.Group.Dto.GroupMemberResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupListDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupStatusDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerProfileResponseDto;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Group.Service.GroupMemberService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    /*
    1. 로그인한 매니저의 ID, 이름 반환
    2. 로그인한 매니저가 관리하는 그룹 정보를 반환합니다.
    3. 로그인한 매니저가 관리하는 그룹 정보를 전체 반환합니다.
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
        try {
            authorizeManager(userDetails);
            Integer managerId = Integer.valueOf(userDetails.getUsername());
            ManagerProfileResponseDto basic = managerService.getManagerBasicProfile(managerId);
            return ResponseEntity.ok(basic);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2. 로그인한 매니저가 관리하는 진행중인 그룹 정보를 반환합니다.
    @Operation(summary = "2. 로그인한 매니저가 관리하는 진행중인 그룹 정보를 반환합니다.", description = "로그인한 매니저가 관리 중인(팀 빌딩 종료 전) 그룹 목록을 반환합니다.",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ManagerGroupResponseDto.class))))})
    @GetMapping("/groups/process")
    public ResponseEntity<List<ManagerGroupResponseDto>> getManagedGroups(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            authorizeManager(userDetails);
            Integer managerId = Integer.valueOf(userDetails.getUsername());
            List<ManagerGroupResponseDto> groups = managerService.getManagedGroups(managerId);
            return ResponseEntity.ok(groups);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 3. 로그인한 매니저가 관리하는 그룹 정보를 전체 반환합니다.
    @Operation(summary = "3. 로그인한 매니저가 관리하는 그룹 정보를 전체 반환합니다.", description = "로그인한 매니저가 관리 중인 그룹의 이름, 참여자 수, 팀 빌딩 타입, 현재 진행 상태를 반환합니다.",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ManagerGroupStatusDto.class))))})
    @GetMapping("/groups/all")
    public ResponseEntity<List<ManagerGroupStatusDto>> getManagedGroupStatuses(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            authorizeManager(userDetails);
            Integer managerId = Integer.valueOf(userDetails.getUsername());
            List<ManagerGroupStatusDto> statuses = managerService.getManagedGroupStatuses(managerId);
            return ResponseEntity.ok(statuses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4. 매니저 관리 그룹 ID·이름 리스트 조회
    @Operation(summary = "매니저 관리 그룹 ID·이름 리스트 조회", description = "로그인한 매니저가 관리하는 모든 그룹의 ID와 이름을 간단히 반환합니다.",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ManagerGroupListDto.class))))})
    @GetMapping("/groups/id-name")
    public ResponseEntity<List<ManagerGroupListDto>> getManagerGroupList(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            authorizeManager(userDetails);
            Integer managerId = Integer.valueOf(userDetails.getUsername());
            List<ManagerGroupListDto> list = managerService.getManagerGroupList(managerId);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 5. 매니저가 그룹 멤버 리스트 조회
    @Operation(summary = "5. 매니저가 그룹 멤버 리스트 조회", description = "매니저 권한이 있는 사용자가 자신이 관리하는 그룹의 모든 멤버를 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = GroupMemberResponseDto.class))))})
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponseDto>> getGroupMembersByManager(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        try {
            // 1) 매니저 권한 및 그룹 관리 권한 검증
            authorizeManager(userDetails);
            Integer managerId = Integer.valueOf(userDetails.getUsername());
            authorizeManager(groupId, managerId);

            List<GroupMemberResponseDto> response =
                    groupMemberService.getGroupMembers(managerId, groupId, false);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
