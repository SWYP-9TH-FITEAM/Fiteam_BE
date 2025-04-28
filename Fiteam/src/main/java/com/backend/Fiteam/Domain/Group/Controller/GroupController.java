package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.Domain.Group.Dto.CreateGroupRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupInvitedResponseDto;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /*
    1. 매니저가 그룹 생성
    2. 매니저가 이메일로 유저 초대(1~N명)
    3. 그룹의 팀타입 지정 POST(랜덤 매칭인지, 직군별 매칭인지 등등)
    4. 그룹에 참여한 전체 멤버 리스트 GET
    */

    // 미완성. Manager가 그룹 생성할 때 TeamType도 생성해야 해서.
    @Operation(summary = "프로젝트 그룹 생성", description = "Manager가 프로젝트 그룹을 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestBody CreateGroupRequestDto requestDto) {
        try{
            groupService.createGroup(requestDto);
            return ResponseEntity.ok().build();
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @Operation(summary = "그룹에 이메일로 유저 초대 1~N명 가능", description = "Manager가 여러 사용자를 프로젝트 그룹에 초대합니다.")
    @PostMapping("/{groupId}/invite")
    public ResponseEntity<GroupInvitedResponseDto> inviteUsersToGroup(
            @PathVariable Integer groupId, @RequestBody List<String> emails) {
        try {
            GroupInvitedResponseDto responseDto = groupService.inviteUsersToGroup(groupId, emails);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }



}

