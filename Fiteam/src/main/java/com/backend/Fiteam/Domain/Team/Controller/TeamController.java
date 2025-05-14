package com.backend.Fiteam.Domain.Team.Controller;


import com.backend.Fiteam.Domain.Team.Dto.TeamContactResponseDto;
import com.backend.Fiteam.Domain.Team.Dto.TeamMemberDto;
import com.backend.Fiteam.Domain.Team.Dto.TeamRequestResponseDto;
import com.backend.Fiteam.Domain.Team.Service.TeamRequestService;
import com.backend.Fiteam.Domain.Team.Service.TeamService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/v1/team")
@RequiredArgsConstructor
@Tag(name = "6. TeamController - 그룹에서 팀Building")
public class TeamController {

    /*
    1. ‘같이 팀할래?’ 요청 보내기
    2. 유저가 받은 요청 List로 받기
    3. 유저가 특정 유저에게 받은 요청 보기
    4. 팀 참가 요청 수락/거절
    5. 내 팀 구성 현황
    5-1. 나한테 팀참가 요청 보낸 사람의 팀 구성 보기. -> 특정 유저가 소속한 팀 정보 보기(같은 그룹에서)
    6. 전체 팀 구성 현황 보기(임시팀, 확정팀 둘다)
    7. 팀장 변경하기(현재 팀장이 같은 팀소속의 다른 멤버를 지정해서 넘겨줌)
    8. 팀(임시 포함) 나오기 기능
    9. 팀 확정이후 연락처 공유하기 기능
    10. 팀 확정신청
    11. ‘같이 팀할래?’ 이거를 채팅방에서 하도록 연결

    12.
    */

    private final TeamRequestService teamRequestService;
    private final TeamService teamService;

    // 1. ‘같이 팀할래?’ 요청 보내기
    @Operation(summary = "1. ‘같이 팀할래?’ 요청 보내기", description = "만약 상대방이 보낸 요청이 있으면 즉시 수락처리됨.")
    @PostMapping("/request/{groupId}/{receiverId}")
    public ResponseEntity<String> handleTeamRequest(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId, @PathVariable Integer receiverId) {
        Integer senderId = Integer.parseInt(userDetails.getUsername());

        teamRequestService.sendTeamRequest(senderId, receiverId, groupId);

        return ResponseEntity.ok("팀 요청을 보냈습니다.");
    }

    // 2. 유저가 받은 요청 List로 받기
    @Operation(summary = "2. 유저가 받은 요청 List로 받기", description = "로그인한 사용자가 받은 모든 팀 요청 목록을 조회합니다.",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = TeamRequestResponseDto.class))))})
    @GetMapping("/requests/received")
    public ResponseEntity<?> getReceivedRequests(@AuthenticationPrincipal UserDetails userDetails) {
        Integer receiverId = Integer.parseInt(userDetails.getUsername());
        List<TeamRequestResponseDto> requests = teamRequestService.getReceivedTeamRequests(receiverId);
        return ResponseEntity.ok(requests);
    }

    // 3. 유저가 특정 유저에게 받은 요청 보기 -> 채팅방에서 요청 보내고 수락할때!
    @Operation(summary = "3. 특정 유저에게 받은 팀 요청 조회", description = "내가 특정 사용자에게 받은 팀 요청이 있는지 확인합니다.",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = TeamRequestResponseDto.class)))})
    @GetMapping("/request/from/{userId}")
    public ResponseEntity<TeamRequestResponseDto> getRequestFromUser(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer userId) {
        Integer receiverId = Integer.parseInt(userDetails.getUsername());

        // 한 개만 조회
        Optional<TeamRequestResponseDto> dtoOpt = Optional.ofNullable(
                teamRequestService.getRequestFromUser(userId, receiverId));

        return dtoOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    // 4. 팀 참가 요청 수락
    @Operation(summary = "4. 팀 참가 요청 수락", description = "특정 사용자가 보낸 팀 요청을 수락합니다. (로그인한 수락자 기준)")
    @PostMapping("/request/accept/{groupId}/{senderId}")
    public ResponseEntity<String> acceptTeamRequest(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId, @PathVariable Integer senderId) {
        Integer receiverId = Integer.parseInt(userDetails.getUsername());
        teamRequestService.acceptTeamRequest(receiverId, senderId, groupId);
        return ResponseEntity.ok("팀 요청이 수락되었습니다.");
    }

    // 4. 팀 참가 요청 거절 - 요청을 삭제하는 방식으로 일단 했음.
    @Operation(summary = "4. 팀 참가 요청 거절", description = "특정 사용자가 보낸 팀 요청을 거절합니다. (요청 삭제 방식)")
    @DeleteMapping("/request/reject/{groupId}/{senderId}")
    public ResponseEntity<String> rejectTeamRequest(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId, @PathVariable Integer senderId) {
        Integer receiverId = Integer.parseInt(userDetails.getUsername());
        teamRequestService.rejectTeamRequest(receiverId, senderId, groupId);
        return ResponseEntity.ok("팀 요청이 거절되었습니다.");
    }

    // 5. 내 팀 구성 현황
    @Operation(summary = "5. 내 팀 구성 현황", description = "로그인한 사용자가 속한 팀의 멤버 리스트를 반환합니다.",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = TeamMemberDto.class))))})
    @GetMapping("/myteam")
    public ResponseEntity<List<TeamMemberDto>> getMyTeam(@AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        List<TeamMemberDto> members = teamRequestService.getMyTeamMembers(userId);
        return ResponseEntity.ok(members);
    }

    // 5-1. 나한테 팀참가 요청 보낸 사람의 팀 구성 보기. -> 특정 유저가 소속한 팀 정보 보기(같은 그룹에서)
    @Operation(summary = "5-1. 나한테 팀참가 요청 보낸 사람의 팀 구성 보기.", description = "특정 사용자가 소속된 팀의 멤버 리스트를 반환합니다.",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = TeamMemberDto.class))))})
    @GetMapping("/{senderId}/{groupId}")
    public ResponseEntity<List<TeamMemberDto>> getSenderTeam(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer senderId, @PathVariable Integer groupId) {
        List<TeamMemberDto> members = teamRequestService.getTeamOfSender(senderId, groupId);
        return ResponseEntity.ok(members);
    }

    // 6. 전체 팀 구성 현황 보기(임시팀, 확정팀 둘다)
    @Operation(summary = "6. 전체 팀 구성 현황 조회", description = "로그인한 사용자의 그룹에 속한 모든 팀의 멤버 리스트(2차원 배열) 를 반환합니다.",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = TeamMemberDto.class))))})
    @GetMapping("/teambuildingstatus/{groupId}")
    public ResponseEntity<List<List<TeamMemberDto>>> getGroupTeams(@AuthenticationPrincipal UserDetails userDetails,@PathVariable Integer groupId) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        boolean isManager = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_Manager"));

        List<List<TeamMemberDto>> status = teamRequestService.getGroupTeamStatus(userId, groupId,isManager);
        return ResponseEntity.ok(status);
    }

    // 7. 팀장 변경하기(현재 팀장이 같은 팀소속의 다른 멤버를 지정해서 넘겨줌)
    @Operation(summary = "7. 팀장 변경", description = "현재 팀장만 자신의 팀 내 다른 멤버를 새로운 팀장으로 지정할 수 있습니다.",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = String.class)))})
    @PatchMapping("/{teamId}/leader/{new_master_id}")
    public ResponseEntity<String> changeTeamLeader(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer teamId, @PathVariable Integer new_master_id) {
        // 로그인한 사용자의 userId를 현재 팀장으로 간주
        Integer userId = Integer.valueOf(userDetails.getUsername());

        // 실제 변경 로직 호출
        teamService.changeTeamMaster(teamId, userId, new_master_id);

        return ResponseEntity.ok("팀장이 성공적으로 변경되었습니다.");
    }

    // 8. 팀(임시 포함) 나오기-> 1인 팀으로 되는거야
    @Operation(summary = "8. 팀 탈퇴", description = "팀 소속 멤버가 팀을 탈퇴합니다. (팀장은 먼저 리더를 변경해야 합니다.)",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = String.class)))})
    @DeleteMapping("/{teamId}/leave")
    public ResponseEntity<String> leaveTeam(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer teamId) {
        Integer userId = Integer.valueOf(userDetails.getUsername());
        teamService.leaveTeam(teamId, userId);
        return ResponseEntity.ok("팀을 성공적으로 탈퇴했습니다.");
    }


    @Operation(summary = "9. 팀 확정 신청", description = "로그인한 팀장이 해당 팀을 확정 상태로 변경합니다.",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = String.class)))})
    @PatchMapping("/{teamId}/confirm")
    public ResponseEntity<?> confirmTeam(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer teamId) {
        try {
            Integer masterId = Integer.valueOf(userDetails.getUsername());
            teamService.confirmTeam(teamId, masterId);
            return ResponseEntity.ok("팀이 성공적으로 확정되었습니다.");
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 10. 팀 확정 이후 연락처 공유
    @Operation(summary = "10. 팀 확정 이후 연락처 공유", description = "팀 확정 이후, 로그인한 팀장만 해당 팀의 멤버 연락처(전화번호, 카카오톡 ID)를 조회할 수 있습니다.",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = TeamContactResponseDto.class))))})
    @GetMapping("/{teamId}/contacts")
    public ResponseEntity<?> getTeamContacts(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer teamId) {
        Integer currentUserId = Integer.parseInt(userDetails.getUsername());
        List<TeamContactResponseDto> contacts = teamService.getTeamContacts(teamId, currentUserId);
        return ResponseEntity.ok(contacts);
    }

}
