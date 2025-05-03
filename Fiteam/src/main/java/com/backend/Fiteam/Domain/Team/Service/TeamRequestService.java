package com.backend.Fiteam.Domain.Team.Service;

import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Team.Dto.TeamMemberDto;
import com.backend.Fiteam.Domain.Team.Dto.TeamRequestResponseDto;
import com.backend.Fiteam.Domain.Team.Entity.Team;
import com.backend.Fiteam.Domain.Team.Entity.TeamRequest;
import com.backend.Fiteam.Domain.Team.Entity.TeamType;
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

@Service
@RequiredArgsConstructor
public class TeamRequestService {

    private final ProjectGroupRepository  projectGroupRepository;
    private final TeamRequestRepository   teamRequestRepository;
    private final TeamTypeRepository      teamTypeRepository;
    private final TeamRepository          teamRepository;
    private final GroupMemberRepository   groupMemberRepository;
    private final UserRepository          userRepository;

    public void sendTeamRequest(Integer senderId, Integer receiverId, Integer groupId) {
        // 1) 이미 보낸 요청 여부
        if (teamRequestRepository.existsBySenderIdAndReceiverIdAndGroupId(senderId, receiverId, groupId)) {
            throw new IllegalArgumentException("이미 요청을 보냈습니다.");
        }

        // 2) 반대로 받은 요청이 있는 경우 → 추후 수락 처리로 연결
        if (teamRequestRepository.existsBySenderIdAndReceiverIdAndGroupId(receiverId, senderId, groupId)) {
            return; // 이미 받은 요청이 있으면 여기서 처리(수락) 로직으로 연결될 수 있습니다.
        }

        // 3) 둘 다 그룹에 정식 멤버여야 함
        boolean bothInGroup = groupMemberRepository
                .existsByGroupIdAndUserIdAndIsAcceptedTrue(groupId, senderId)
                && groupMemberRepository
                .existsByGroupIdAndUserIdAndIsAcceptedTrue(groupId, receiverId);
        if (!bothInGroup) {
            throw new IllegalArgumentException("같은 그룹 멤버가 아닙니다.");
        }

        // 4) sender 의 현재 teamId 조회
        GroupMember senderMember = groupMemberRepository.findByUserIdAndGroupId(senderId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("요청 보낸 사용자가 그룹에 속해 있지 않습니다."));
        Integer senderTeamId = senderMember.getTeamId();

        // 5) 요청 저장
        TeamRequest request = TeamRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .groupId(groupId)
                .teamId(senderTeamId)
                .status("대기중")
                .requestedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        teamRequestRepository.save(request);
    }

    public List<TeamRequestResponseDto> getReceivedTeamRequests(Integer receiverId) {
        List<TeamRequest> requests = teamRequestRepository.findAllByReceiverId(receiverId);
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


    public void acceptTeamRequest(Integer receiverId, TeamRequestResponseDto reqdto) {
        // 1) 요청 엔티티 확인
        TeamRequest request = teamRequestRepository
                .findBySenderIdAndReceiverId(reqdto.getSenderId(), receiverId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요청입니다."));

        // 2) 이미 처리된 요청인지 체크
        if (!"대기중".equals(request.getStatus())) {
            throw new IllegalArgumentException("이미 처리된 요청입니다.");
        }

        Integer senderId = reqdto.getSenderId();
        Integer groupId  = reqdto.getGroupId();

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
            throw new IllegalArgumentException("팀 참가 요청은 팀장만 수락할 수 있습니다.");
        }

        // 5) 항상 두 팀(1인 팀)을 병합
        mergeTeams(senderMember.getTeamId(), receiverMember.getTeamId());

        // 6) 요청 상태 업데이트
        request.setStatus("수락됨");
        teamRequestRepository.save(request);
    }

    // '수락 API 에서 팀을 합치는 로직
    private void mergeTeams(Integer primaryTeamId, Integer secondaryTeamId) {
        if (primaryTeamId.equals(secondaryTeamId)) {
            throw new IllegalArgumentException("이미 같은 팀입니다.");
        }

        // 기준 팀/병합 대상 팀 조회
        Team primary   = teamRepository.findById(primaryTeamId)
                .orElseThrow(() -> new IllegalArgumentException("기준 팀이 없습니다."));
        Team secondary = teamRepository.findById(secondaryTeamId)
                .orElseThrow(() -> new IllegalArgumentException("병합 대상 팀이 없습니다."));

        // 최대 인원 검사
        TeamType teamType = teamTypeRepository.findById(
                projectGroupRepository.findById(primary.getGroupId())
                        .orElseThrow(() -> new IllegalArgumentException("그룹이 없습니다."))
                        .getTeamMakeType()
        ).orElseThrow(() -> new IllegalArgumentException("팀 빌딩 타입이 없습니다."));

        long size1 = groupMemberRepository.countByTeamId(primaryTeamId);
        long size2 = groupMemberRepository.countByTeamId(secondaryTeamId);
        if (size1 + size2 > teamType.getMaxMembers()) {
            throw new IllegalArgumentException("병합 후 팀 최대 인원을 초과합니다.");
        }

        // secondary 팀원 모두를 primary 팀으로 이동
        List<GroupMember> toMove = groupMemberRepository.findAllByTeamId(secondaryTeamId);
        toMove.forEach(gm -> gm.setTeamId(primaryTeamId));
        groupMemberRepository.saveAll(toMove);

        // secondary 팀 삭제
        teamRepository.delete(secondary);
    }

    public void rejectTeamRequest(Integer receiverId, TeamRequestResponseDto reqdto) {
        // 1) 요청 엔티티 조회
        TeamRequest request = teamRequestRepository
                .findBySenderIdAndReceiverId(reqdto.getSenderId(), receiverId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요청입니다."));

        // 2) 이미 처리된 요청인지 검사
        if (!"대기중".equals(request.getStatus())) {
            throw new IllegalArgumentException("이미 처리된 요청입니다.");
        }

        // 3) 요청 삭제
        teamRequestRepository.delete(request);
    }


    // TeamRequestService.java — getMyTeamMembers 메서드만 수정
    public List<TeamMemberDto> getMyTeamMembers(Integer userId) {
        // 1) 내 GroupMember 조회
        GroupMember me = groupMemberRepository.findByUserIdAndIsAcceptedTrueAndTeamIdNotNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("팀에 속해 있지 않습니다."));

        Integer teamId = me.getTeamId();

        // 2) 같은 팀의 멤버 모두 조회
        List<GroupMember> members = groupMemberRepository.findAllByTeamId(teamId);

        // 3) DTO 변환 (필요한 4가지 필드만 매핑)
        return members.stream()
                .map(gm -> {
                    User u = userRepository.findById(gm.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
                    return TeamMemberDto.builder()
                            .userId(u.getId())
                            .userName(u.getUserName())
                            .profileImgUrl(u.getProfileImgUrl())
                            .position(gm.getPosition())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<List<TeamMemberDto>> getGroupTeamStatus(Integer userId) {
        // 1) 내 그룹+팀 확인
        GroupMember me = groupMemberRepository
                .findByUserIdAndIsAcceptedTrueAndTeamIdNotNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("팀에 속해 있지 않습니다."));
        Integer groupId = me.getGroupId();

        // 2) 그룹의 전체 멤버 조회
        List<GroupMember> all = groupMemberRepository.findAllByGroupId(groupId);

        // 3) teamId 별로 묶어서 TeamMemberDto 로 매핑한 뒤 List<List<...>> 로
        Map<Integer, List<GroupMember>> byTeam = all.stream()
                .collect(Collectors.groupingBy(GroupMember::getTeamId));

        return byTeam.values().stream()
                .map(gmList -> gmList.stream()
                        .map(gm -> {
                            User u = userRepository.findById(gm.getUserId())
                                    .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
                            return TeamMemberDto.builder()
                                    .userId(u.getId())
                                    .userName(u.getUserName())
                                    .profileImgUrl(u.getProfileImgUrl())
                                    .position(gm.getPosition())
                                    .build();
                        })
                        .collect(Collectors.toList())
                )
                .collect(Collectors.toList());
    }
}
