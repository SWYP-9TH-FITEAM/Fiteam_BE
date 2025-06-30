package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.Domain.Admin.Dto.SystemNoticeResponseDto;
import com.backend.Fiteam.Domain.Admin.Service.AdminService;
import com.backend.Fiteam.Domain.Group.Dto.GroupDetailResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeDetailDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeSummaryDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupNotice;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Service.GroupNoticeService;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import com.backend.Fiteam.Domain.Group.Service.ManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

@PreAuthorize("hasRole('User') or hasRole('Manager')")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "8. GroupSharedController - 그룹에서 User, Manager 공통사용 API")
public class GroupSharedController {

    private final GroupService groupService;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupNoticeService groupNoticeService;
    private final AdminService adminService;
    private final ManagerService managerService;

    /*
    1. 그룹 정보 가져오기(유저, 매니저 둘다 접근하게)
    2. 그룹의 공지리스트 보기
    3. 그룹공지 상세보기
    */


    // 1. 그룹 정보 가져오기(유저, 매니저 둘다 접근하게)
    @Operation(summary = "1. 그룹 상세 정보 조회", description = "groupId에 해당하는 그룹과 해당 팀 빌딩 타입 정보를 하나의 DTO로 반환합니다.")
    @GetMapping("/group/{groupId}/data")
    public ResponseEntity<GroupDetailResponseDto> getGroupDetail(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        Integer userId = Integer.valueOf(userDetails.getUsername());

        boolean isManager = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_Manager"));

        if (isManager) {
            // 매니저라면, 이 그룹을 관리하는 매니저인지 확인
            managerService.authorizeManager(groupId, userId);
        } else {
            // 일반 유저라면, 가입되어 있고 수락된 상태인지 확인
            boolean joined = groupMemberRepository
                    .findByGroupIdAndUserIdAndIsAcceptedTrue(groupId, userId).isPresent();
            if (!joined) {
                throw new IllegalArgumentException("해당 그룹 유저가 아닙니다.");
            }
        }

        GroupDetailResponseDto detail = groupService.getGroupDetail(groupId);
        return ResponseEntity.ok(detail);
    }

    // 2. 그룹의 공지목록 보기.
    @Operation(summary = "2. 그룹 공지 목록 조회 (멤버용)", description = "로그인한 멤버가 자신이 속한 그룹의 공지를 최신 순으로 조회합니다.")
    @GetMapping("/notice/{groupId}/list")
    public ResponseEntity<List<GroupNoticeSummaryDto>> getGroupNotices(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        Integer userId = Integer.valueOf(userDetails.getUsername());
        List<GroupNoticeSummaryDto> list = groupNoticeService.getNoticesForMember(userId, groupId);
        return ResponseEntity.ok(list);
    }

    // 3. 그룹공지 상세보기
    @Operation(summary = "3. 공지 상세 조회", description = "관리자가 작성한 특정 공지의 전체 내용을 반환합니다.")
    @GetMapping("/notice/{noticeId}/detail")
    public ResponseEntity<GroupNoticeDetailDto> getNoticeById(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer noticeId) {
        GroupNoticeDetailDto dto = groupNoticeService.getNoticeById(noticeId);
        return ResponseEntity.ok(dto);
    }


    @Operation(summary = "new-시스템 공지사항 조회", description = "등록된 모든 시스템 공지사항 목록을 반환합니다.")
    @GetMapping("/system/notices")
    public ResponseEntity<List<SystemNoticeResponseDto>> getSystemNotices() {
        List<SystemNoticeResponseDto> list = adminService.getAllSystemNotices();
        return ResponseEntity.ok(list);
    }
}
