package com.backend.Fiteam.Domain.Team.Controller;


import com.backend.Fiteam.Domain.Team.Dto.TeamMemberDto;
import com.backend.Fiteam.Domain.Team.Dto.TeamRequestDto;
import com.backend.Fiteam.Domain.Team.Dto.TeamRequestResponseDto;
import com.backend.Fiteam.Domain.Team.Service.TeamRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/team")
@RequiredArgsConstructor
public class TeamController {

    /*
    1. ‘같이 팀할래?’ 요청 보내기
    2. 유저가 받은 요청 List로 받기
    3. 유저가 특정 유저에게 받은 요청 보기
    4. 팀 참가 요청 수락/거절

    5. 내 팀 구성 현황
    6. 전체 팀 구성 현황 보기(임시팀, 확정팀 둘다)
    */

    private final TeamRequestService teamRequestService;

    // 1. ‘같이 팀할래?’ 요청 보내기
    @PostMapping("/request")
    public ResponseEntity<?> handleTeamRequest(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody TeamRequestDto dto) {
        try {
            Integer senderId = Integer.parseInt(userDetails.getUsername());

            teamRequestService.sendTeamRequest(senderId, dto.getReceiverId(), dto.getGroupId());

            return ResponseEntity.ok("팀 요청이 처리되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    // 2. 유저가 받은 요청 List로 받기
    @Operation(summary = "내가 받은 팀 요청 조회", description = "로그인한 사용자가 받은 모든 팀 요청 목록을 조회합니다.")
    @GetMapping("/requests/received")
    public ResponseEntity<?> getReceivedRequests(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer receiverId = Integer.parseInt(userDetails.getUsername());
            List<TeamRequestResponseDto> requests = teamRequestService.getReceivedTeamRequests(receiverId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    // 3. 유저가 특정 유저에게 받은 요청 보기 -> 채팅방에서 요청 보내고 수락할때!
    @Operation(summary = "특정 유저에게 받은 팀 요청 조회", description = "내가 특정 사용자에게 받은 팀 요청이 있는지 확인합니다.")
    @GetMapping("/request/from/{userId}")
    public ResponseEntity<?> getRequestFromUser(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer userId) {
        try {
            Integer receiverId = Integer.parseInt(userDetails.getUsername());

            // 한 개만 조회
            Optional<TeamRequestResponseDto> dtoOpt = Optional.ofNullable(
                    teamRequestService.getRequestFromUser(userId, receiverId));

            return dtoOpt.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.noContent().build());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    // 4. 팀 참가 요청 수락
    @Operation(summary = "받은 팀 요청 수락", description = "특정 사용자가 보낸 팀 요청을 수락합니다.")
    @PostMapping("/request/accept/{senderId}")
    public ResponseEntity<?> acceptTeamRequest(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody TeamRequestResponseDto reqdto) {
        try {
            Integer receiverId = Integer.parseInt(userDetails.getUsername());
            teamRequestService.acceptTeamRequest(receiverId, reqdto);
            return ResponseEntity.ok("팀 요청이 수락되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    // 4. 팀 참가 요청 거절 - 요청을 삭제하는 방식으로 일단 했음.
    @Operation(summary = "받은 팀 요청 거절", description = "특정 사용자가 보낸 팀 요청을 거절합니다.")
    @PostMapping("/request/reject/{senderId}")
    public ResponseEntity<?> rejectTeamRequest(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody TeamRequestResponseDto reqdto) {
        try {
            Integer receiverId = Integer.parseInt(userDetails.getUsername());
            teamRequestService.rejectTeamRequest(receiverId, reqdto);
            return ResponseEntity.ok("팀 요청이 거절되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    // 5. 내 팀 구성 현황
    @Operation(summary = "내 팀 구성 현황 조회", description = "로그인한 사용자가 속한 팀의 멤버 리스트를 반환합니다.")
    @GetMapping("/myteam")
    public ResponseEntity<List<TeamMemberDto>> getMyTeam(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        List<TeamMemberDto> members = teamRequestService.getMyTeamMembers(userId);
        return ResponseEntity.ok(members);
    }

    //    6. 전체 팀 구성 현황 보기(임시팀, 확정팀 둘다)
    @Operation(
            summary = "전체 팀 구성 현황 조회",
            description = "로그인한 사용자의 그룹에 속한 모든 팀의 멤버 리스트(2차원 배열) 를 반환합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            description = "팀별 멤버 리스트",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    array = @ArraySchema(schema = @Schema(implementation = TeamMemberDto.class))
                            )
                    )
            }
    )
    @GetMapping("/teambuildingstatus")
    public ResponseEntity<List<List<TeamMemberDto>>> getGroupTeams(@AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        List<List<TeamMemberDto>> status = teamRequestService.getGroupTeamStatus(userId);
        return ResponseEntity.ok(status);
    }
}
