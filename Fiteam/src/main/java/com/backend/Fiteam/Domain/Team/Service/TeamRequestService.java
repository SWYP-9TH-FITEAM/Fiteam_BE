package com.backend.Fiteam.Domain.Team.Service;

import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamRequestStatus;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;
import com.backend.Fiteam.Domain.Chat.Service.ChatService;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Team.Dto.TeamMemberDto;
import com.backend.Fiteam.Domain.Team.Dto.TeamRequestResponseDto;
import com.backend.Fiteam.Domain.Team.Dto.TeamStatusDto;
import com.backend.Fiteam.Domain.Team.Entity.Team;
import com.backend.Fiteam.Domain.Team.Entity.TeamRequest;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Team.Repository.TeamRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamRequestRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamRequestService {

    private final ProjectGroupRepository  projectGroupRepository;
    private final TeamRequestRepository   teamRequestRepository;
    private final TeamTypeRepository      teamTypeRepository;
    private final TeamRepository          teamRepository;
    private final GroupMemberRepository   groupMemberRepository;
    private final UserRepository          userRepository;
    private final ChatService             chatService;


    /*
    sendTeamRequest 함수
    1. 시간복잡도 : O(1)
        - sender/receiver 간 요청 존재 조회 및 그룹 멤버 여부 확인이 모두 상수 횟수의 쿼리로 처리됨
        - teamRequest 저장 및 메시지 전송도 상수 작업

    2. DB Read : 총 7회
        - teamRequestRepository.existsBySenderIdAndReceiverIdAndGroupId(sender→receiver) → 1회
        - teamRequestRepository.existsBySenderIdAndReceiverIdAndGroupId(receiver→sender) → 1회
        - groupMemberRepository.existsByGroupIdAndUserIdAndIsAcceptedTrue(groupId, senderId) → 1회
        - groupMemberRepository.existsByGroupIdAndUserIdAndIsAcceptedTrue(groupId, receiverId) → 1회
        - groupMemberRepository.findByUserIdAndGroupId(senderId, groupId) → 1회
        - groupMemberRepository.findByUserIdAndGroupId(receiverId, groupId) → 1회
        - teamRepository.findById(receiverTeamId) → 1회
    3. DB Write : 총 2회
        - teamRequestRepository.save(request) → 1회
        - chatService.sendTeamRequestMessage(...) (메시지 저장) → 1회
    4. 병렬처리 필요성 여부 : ⛔ 필요 없음
        - 트랜잭션 내에서 일관성 있게 처리되어야 하는 쓰기 중심 로직
        - 비동기화 시 오히려 예외 처리·롤백 복잡도 증가
    5. 개선점
        - **중복 조회 통합**
            • `existsBy…` + `findBy…` 로 두 번 조회하는 회원 정보를
              `findByUserIdAndGroupId` 한 번으로 조회 후 검증하여 DB 호출 수 절감
        - **조건 분기 정리**
            • 팀 상태 검증(`PENDING`, `CLOSED`, `FIXED`) 로ジック을 헬퍼 메서드로 분리하여 가독성 향상
        - **트랜잭션 경계 최소화**
            • 순수 검증 로직을 별도 서비스로 분리해, 실제 저장은 가장 마지막에만 수행
        - **Bulk 설정**
            • 많은 요청이 동시 발생할 때 대비해 인덱스 최적화, DB 커넥션 풀 모니터링 필요
    */
    @Transactional
    public void sendTeamRequest(Integer senderId, Integer receiverId, Integer groupId) {
        // 1) 이미 보낸 요청 여부
        if (teamRequestRepository.existsBySenderIdAndReceiverIdAndGroupId(senderId, receiverId, groupId)) {
            throw new IllegalArgumentException("이미 요청을 보냈습니다.");
        }

        // 2) 반대로 받은 요청이 있는 경우 → 추후 수락 처리로 연결
        if (teamRequestRepository.existsBySenderIdAndReceiverIdAndGroupId(receiverId, senderId, groupId)) {
            acceptTeamRequest(receiverId, senderId, groupId);
            return;
        }

        // 3) sender와 receiver가 모두 그룹의 수락된 멤버인지 단일 조회로 확인
        GroupMember senderMember = groupMemberRepository
                .findByUserIdAndGroupIdAndIsAcceptedTrue(senderId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("요청 보낸 사용자가 그룹에 속해 있지 않습니다."));
        GroupMember receiverMember = groupMemberRepository
                .findByUserIdAndGroupIdAndIsAcceptedTrue(receiverId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("요청 받는 사용자가 그룹에 속해 있지 않습니다."));

        Integer senderTeamId   = senderMember.getTeamId();
        Integer receiverTeamId = receiverMember.getTeamId();

        // 4-1) 이미 같은 팀에 속해 있는지 체크
        if (senderTeamId != null && senderTeamId.equals(receiverTeamId)) {
            throw new IllegalArgumentException("이미 같은 팀에 속해 있습니다.");
        }

        Team receiverTeam = teamRepository.findById(receiverTeamId)
                .orElseThrow(() -> new IllegalArgumentException("팀 정보를 찾을 수 없습니다."));

        // 4-2) "대기중" 이라는건 팀 빌딩 시작 전
        if (TeamRequestStatus.PENDING.equals(receiverTeam.getTeamStatus())) {
            throw new IllegalStateException("아직 팀 모집 기간이 아닙니다. 현재 상태: " + receiverTeam.getTeamStatus());
        }
        // 4-3) "모집마감" 이란건 팀 확정상태
        if (TeamStatus.CLOSED.equals(receiverTeam.getTeamStatus())) {
            throw new IllegalStateException("이미 확정팀의 멤버입니다.: " + receiverTeam.getTeamStatus());
        }if(TeamStatus.FIXED.equals(receiverMember.getTeamStatus())){
            throw new IllegalStateException("이미 확정팀의 멤버입니다.: " + receiverTeam.getTeamStatus());
        }

        // 5) 요청 저장
        TeamRequest request = TeamRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .groupId(groupId)
                .teamId(senderTeamId)
                .status(TeamRequestStatus.PENDING)
                .requestedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        teamRequestRepository.save(request);

        // 6) 메시지로 저장도 함.
        chatService.sendTeamRequestMessage(senderId, receiverId, groupId);
    }


    @Transactional
    public List<TeamRequestResponseDto> getReceivedTeamRequests(Integer receiverId, Integer groupId) {
        List<TeamRequest> requests = teamRequestRepository.findAllByReceiverIdAndGroupId(receiverId, groupId);

        return requests.stream()
                .map(req -> {
                    User sender = userRepository.findById(req.getSenderId())
                            .orElseThrow(() -> new IllegalArgumentException("보낸 유저 정보가 없습니다."));
                    return TeamRequestResponseDto.builder()
                            .id(req.getId())
                            .senderId(sender.getId())
                            .senderName(sender.getUserName())
                            .groupId(req.getGroupId())
                            .status(req.getStatus())
                            .requestedAt(req.getRequestedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamRequestResponseDto getRequestFromUser(Integer senderId, Integer receiverId) {
        TeamRequest req = teamRequestRepository
                .findBySenderIdAndReceiverId(senderId, receiverId)
                .orElseThrow(() -> new IllegalArgumentException("해당 요청이 없습니다."));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("보낸 유저 정보가 없습니다."));
        return TeamRequestResponseDto.builder()
                .id(req.getId())
                .senderId(sender.getId())
                .senderName(sender.getUserName())
                .groupId(req.getGroupId())
                .status(req.getStatus())
                .requestedAt(req.getRequestedAt())
                .build();
    }

    @Transactional
    public void acceptTeamRequest(Integer receiverId, Integer senderId, Integer groupId) {
        // 1) 요청 엔티티 확인
        TeamRequest request = teamRequestRepository
                .findBySenderIdAndReceiverIdAndGroupId(senderId, receiverId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요청입니다."));

        // 2) 이미 처리된 요청인지 체크
        if (!TeamRequestStatus.PENDING.equals(request.getStatus())) {
            throw new IllegalArgumentException("이미 처리된 요청입니다.");
        }

        // 3) sender, receiver 는 그룹에 속해 있고 teamId 가 세팅된 상태
        GroupMember senderMember = groupMemberRepository
                .findByUserIdAndGroupId(senderId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("요청 보낸 사용자가 그룹에 속해 있지 않습니다."));
        GroupMember receiverMember = groupMemberRepository
                .findByUserIdAndGroupId(receiverId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("요청 받는 사용자가 그룹에 속해 있지 않습니다."));

        // 4) 수락자는 반드시 자신의 팀장이어야 함
        Team receiverTeam = teamRepository.findById(receiverMember.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("수락 사용자의 팀 정보를 찾을 수 없습니다."));
        if (!receiverTeam.getMasterUserId().equals(receiverId)) {
            teamRequestRepository.delete(request);
            throw new IllegalArgumentException("팀 참가 요청은 팀장만 수락할 수 있습니다.");
        }

        // 5) 채팅 메시지 추가
        chatService.sendTeamResponseMessage(senderId, receiverId, groupId, "수락");

        // 6) 항상 두 팀(1인 팀 포함)을 병합
        mergeTeams(senderMember.getTeamId(), receiverMember.getTeamId());

        // 7) 요청 상태 업데이트 -> 사실상 삭제..
        request.setStatus(TeamRequestStatus.APPROVED);
        teamRequestRepository.delete(request);
    }

    // '수락 API 에서 팀을 합치는 로직
    @Transactional
    protected void mergeTeams(Integer primaryTeamId, Integer secondaryTeamId) {
        if (primaryTeamId.equals(secondaryTeamId)) {
            throw new IllegalArgumentException("이미 같은 팀입니다.");
        }

        // 기준 팀/병합 대상 팀 조회
        Team primary   = teamRepository.findById(primaryTeamId)
                .orElseThrow(() -> new IllegalArgumentException("기준 팀이 없습니다."));
        Team secondary = teamRepository.findById(secondaryTeamId)
                .orElseThrow(() -> new IllegalArgumentException("병합 대상 팀이 없습니다."));

        // 최대 인원 검사
        TeamType teamType = projectGroupRepository.findById(primary.getGroupId())
                        .orElseThrow(() -> new IllegalArgumentException("그룹이 없습니다."))
                        .getTeamMakeType();

        long size1 = groupMemberRepository.countByTeamId(primaryTeamId);
        long size2 = groupMemberRepository.countByTeamId(secondaryTeamId);
        if (size1 + size2 > teamType.getMaxMembers()) {
            throw new IllegalArgumentException("병합 후 팀 최대 인원을 초과합니다.");
        }

        // secondary 팀원 모두를 primary 팀으로 이동
        List<GroupMember> toMove = groupMemberRepository.findAllByTeamId(secondaryTeamId);
        toMove.forEach(gm -> gm.setTeamId(primaryTeamId));
        groupMemberRepository.saveAll(toMove);

        //  추가: TeamRequest 삭제
        List<TeamRequest> relatedRequests = teamRequestRepository.findByTeamId(secondaryTeamId);
        teamRequestRepository.deleteAll(relatedRequests);

        // secondary 팀 삭제
        teamRepository.delete(secondary);
    }

    @Transactional
    public void rejectTeamRequest(Integer receiverId, Integer senderId, Integer groupId) {
        // 1) 요청 엔티티 조회
        TeamRequest request = teamRequestRepository
                .findBySenderIdAndReceiverIdAndGroupId(senderId, receiverId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요청입니다."));

        // 2) 이미 처리된 요청인지 검사
        if (!TeamRequestStatus.PENDING.equals(request.getStatus())) {
            throw new IllegalArgumentException("이미 처리된 요청입니다.");
        }

        // 3) 채팅 메시지 추가
        chatService.sendTeamResponseMessage(senderId, receiverId, groupId, "거절");
        // 4) 요청 삭제
        teamRequestRepository.delete(request);
    }

    @Transactional(readOnly = true)
    public TeamStatusDto getMyTeamMembers(Integer userId, Integer groupId) {
        // 1) 내 GroupMember 조회 (팀에 속해 있어야 함)
        GroupMember me = groupMemberRepository
                .findByUserIdAndGroupIdAndIsAcceptedTrueAndTeamIdNotNull(userId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹에 속한 팀이 없습니다."));

        Integer teamId = me.getTeamId();

        // 2) 같은 팀의 모든 멤버만 조회
        //    (repository에 이미 정의된 메서드 사용)
        List<GroupMember> teamMembers = groupMemberRepository.findAllByGroupIdAndTeamId(groupId, teamId);

        // 3) 팀 엔티티와 리더 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀 정보를 찾을 수 없습니다."));
        Integer leaderId = team.getMasterUserId();

        // 4) TeamMemberDto 변환 (getGroupTeamStatus 참고)
        List<TeamMemberDto> memberDtos = teamMembers.stream()
                .map(gm -> {
                    User u = userRepository.findById(gm.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
                    return TeamMemberDto.builder()
                            .userId(u.getId())
                            .userName(u.getUserName())
                            .profileImgUrl(u.getProfileImgUrl())
                            .position(gm.getPosition())
                            .isMaster(u.getId().equals(leaderId))
                            .build();
                })
                .collect(Collectors.toList());

        // 5) 응답 DTO 빌드
        return TeamStatusDto.builder()
                .teamId(teamId)
                .masterUserId(leaderId)
                .teamStatus(team.getTeamStatus())
                .members(memberDtos)
                .build();
    }

    @Transactional
    public List<TeamStatusDto> getGroupTeamStatus(Integer groupId) {
        // 1) (권한 체크 로직)

        // 2) 그룹의 전체 팀 배정된 멤버만 조회
        List<GroupMember> all = groupMemberRepository.findAllByGroupIdAndIsAcceptedTrue(groupId).stream()
                .filter(gm -> gm.getTeamId() != null)
                .collect(Collectors.toList());

        // 3) teamId 별로 묶기
        Map<Integer, List<GroupMember>> byTeam = all.stream()
                .collect(Collectors.groupingBy(GroupMember::getTeamId));

        // 4) 팀별로 TeamStatusDto 변환
        return byTeam.entrySet().stream()
                .map(entry -> {
                    Integer teamId = entry.getKey();
                    List<GroupMember> gmList = entry.getValue();

                    // 팀 조회 (findByTeamId 사용 시에도 동일하게 교체 가능)
                    Team team = teamRepository.findById(teamId)
                            .orElseThrow(() -> new IllegalArgumentException("팀 정보를 찾을 수 없습니다."));
                    Integer leaderId = team.getMasterUserId();

                    List<TeamMemberDto> members = gmList.stream()
                            .map(gm -> {
                                User u = userRepository.findById(gm.getUserId())
                                        .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
                                return TeamMemberDto.builder()
                                        .userId(u.getId())
                                        .userName(u.getUserName())
                                        .profileImgUrl(u.getProfileImgUrl())
                                        .position(gm.getPosition())
                                        .isMaster(u.getId().equals(leaderId))
                                        .build();
                            })
                            .collect(Collectors.toList());

                    return TeamStatusDto.builder()
                            .teamId(teamId)
                            .masterUserId(leaderId)
                            .teamStatus(team.getTeamStatus())
                            .members(members)
                            .build();
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public List<TeamMemberDto> getTeamOfSender(Integer senderId, Integer groupId) {
        // 1) sender가 그룹에 속해 있어야 함
        GroupMember senderMember = groupMemberRepository.findByUserIdAndGroupId(senderId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 그룹에 속해 있지 않습니다."));

        if (senderMember.getTeamId() == null) {
            throw new IllegalArgumentException("해당 사용자가 팀에 속해 있지 않습니다.");
        }

        Integer teamId = senderMember.getTeamId();

        // 2) 해당 팀 정보 조회 (팀장 ID, 상태 등)
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀 정보를 찾을 수 없습니다."));
        Integer masterId = team.getMasterUserId();

        // 3) 해당 팀의 멤버 전체 조회
        List<GroupMember> members = groupMemberRepository.findAllByTeamId(teamId);

        return members.stream()
                .map(gm -> {
                    User u = userRepository.findById(gm.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
                    return TeamMemberDto.builder()
                            .userId(u.getId())
                            .isMaster(u.getId().equals(masterId))
                            .userName(u.getUserName())
                            .profileImgUrl(u.getProfileImgUrl())
                            .position(gm.getPosition())
                            .build();
                })
                .collect(Collectors.toList());
    }


}
