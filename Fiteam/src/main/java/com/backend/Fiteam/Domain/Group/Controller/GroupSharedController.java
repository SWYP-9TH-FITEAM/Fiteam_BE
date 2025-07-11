package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.Domain.Admin.Dto.SystemNoticeResponseDto;
import com.backend.Fiteam.Domain.Admin.Service.AdminService;
import com.backend.Fiteam.Domain.Group.Dto.GroupDetailResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupMemberResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeDetailDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeSummaryDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupNotice;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Service.GroupMemberService;
import com.backend.Fiteam.Domain.Group.Service.GroupNoticeService;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import com.backend.Fiteam.Domain.Group.Service.ManagerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_MANAGER')")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "8. GroupSharedController - 그룹에서 User, Manager 공통사용 API")
public class GroupSharedController {

    private final GroupService groupService;
    private final GroupNoticeService groupNoticeService;
    private final AdminService adminService;

    /*
    1. 그룹 정보 가져오기(유저, 매니저 둘다 접근하게)
    2. 직무 유형 리스트 GET,POST (PM,디자이너 등)
    3. 그룹의 공지리스트 보기
    4. 그룹공지 상세보기
    5. 유저가 그룹 전체 멤버 리스트 조회
    */


    // 1. 그룹 정보 가져오기(유저, 매니저 둘다 접근하게)
    @Operation(summary = "1. 그룹 상세 정보 조회", description = "groupId에 해당하는 그룹과 해당 팀 빌딩 타입 정보를 하나의 DTO로 반환합니다.")
    @GetMapping("/group/{groupId}/data")
    public ResponseEntity<GroupDetailResponseDto> getGroupDetail(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        Integer userId = Integer.valueOf(userDetails.getUsername());

        groupService.authorizeManagerOrMember(groupId, userId);

        GroupDetailResponseDto detail = groupService.getGroupDetail(groupId);
        return ResponseEntity.ok(detail);
    }

    // 2. 직무 유형 리스트 GET (PM,디자이너 등)
    @Operation(summary = "2. 직무 유형 리스트 GET (PM,디자이너 등)",
            description = "teamMakeType 기준으로 TeamType의 configJson을 파싱하여 직무(position) 리스트를 반환합니다.",
            responses = {@ApiResponse(content = @Content(examples = @ExampleObject(value = "[\"PM\", \"DS\", \"FE\", \"BE\"]")))})
    @GetMapping("/group/{groupId}/positions")
    public ResponseEntity<List<String>> getGroupPositions(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            groupService.validateGroupMembership(userId, groupId);

            List<String> positions = groupService.getPositionListForGroup(groupId);
            return ResponseEntity.ok(positions);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 2. 그룹의 공지목록 보기.
    @Operation(summary = "2. 그룹 공지 목록 조회 (멤버용)", description = "로그인한 멤버가 자신이 속한 그룹의 공지를 최신 순으로 조회합니다.")
    @GetMapping("/group/{groupId}/notice/list")
    public ResponseEntity<List<GroupNoticeSummaryDto>> getGroupNotices(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        Integer userId = Integer.valueOf(userDetails.getUsername());
        List<GroupNoticeSummaryDto> list = groupNoticeService.getNoticesForMember(userId, groupId);
        return ResponseEntity.ok(list);
    }

    // 3. 그룹공지 상세보기
    @Operation(summary = "3. 공지 상세 조회", description = "관리자가 작성한 특정 공지의 전체 내용을 반환합니다.")
    @GetMapping("/group/notice/{noticeId}/detail")
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

    // 5. 유저 또는 매니저가 그룹에 참여한 전체 멤버 리스트 GET
    @Operation(summary = "5. 유저가 그룹 전체 멤버 리스트 조회", description = "그룹에 속한 모든 멤버를 조회합니다.",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = GroupMemberResponseDto.class))))})
    @GetMapping("/group/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponseDto>> getGroupMembers(
            @AuthenticationPrincipal UserDetails userDetails,  @PathVariable Integer groupId) {
        Integer userId = Integer.valueOf(userDetails.getUsername());
        boolean isManager = groupService.authorizeManagerOrMember(groupId, userId);
        List<GroupMemberResponseDto> response = groupService.getGroupMembers(userId, groupId, !isManager);

        return ResponseEntity.ok(response);
    }

}
