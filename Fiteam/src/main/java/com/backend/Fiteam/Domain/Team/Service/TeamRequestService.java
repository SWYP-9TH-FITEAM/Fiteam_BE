package com.backend.Fiteam.Domain.Team.Service;

import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Team.Dto.TeamRequestResponseDto;
import com.backend.Fiteam.Domain.Team.Entity.Team;
import com.backend.Fiteam.Domain.Team.Entity.TeamMember;
import com.backend.Fiteam.Domain.Team.Entity.TeamRequest;
import com.backend.Fiteam.Domain.Team.Entity.TeamType;
import com.backend.Fiteam.Domain.Team.Repository.TeamMemberRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamRequestRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Service
@RequiredArgsConstructor
@RestControllerAdvice
public class TeamRequestService {

    private final ProjectGroupRepository projectGroupRepository;

    private final TeamRequestRepository teamRequestRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamTypeRepository teamTypeRepository;
    private final TeamRepository teamRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;


    public void sendTeamRequest(Integer senderId, Integer receiverId, Integer groupId) {
        // 1. 이미 보낸 요청 여부
        if (teamRequestRepository.existsBySenderIdAndReceiverIdAndGroupId(senderId, receiverId, groupId)) {
            throw new IllegalArgumentException("이미 요청을 보냈습니다.");
        }

        // 2. 반대로 받은 요청이 있는 경우 → 수락으로 전환 예정
        if (teamRequestRepository.existsBySenderIdAndReceiverIdAndGroupId(receiverId, senderId, groupId)) {
            // 추후 수락 처리 연결 예정
            return;
        }

        // 3. 그룹 멤버 확인
        boolean valid = groupMemberRepository.existsByGroupIdAndUserIdAndIsAcceptedTrue(groupId, senderId)
                && groupMemberRepository.existsByGroupIdAndUserIdAndIsAcceptedTrue(groupId, receiverId);
        if (!valid) {
            throw new IllegalArgumentException("같은 그룹이 아닙니다.");
        }

        // 4. sender의 teamId 조회 (1인 팀 포함)
        TeamMember senderMember = teamMemberRepository.findByUserIdAndGroupId(senderId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("요청자는 팀에 소속되어 있어야 합니다."));

        // 5. 요청 저장
        TeamRequest request = TeamRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .groupId(groupId)
                .teamId(senderMember.getTeamId())
                .status("대기중")
                .requestedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        teamRequestRepository.save(request);
    }

    // 받은요청 전체 list
    public List<TeamRequestResponseDto> getReceivedTeamRequests(Integer receiverId) {
        List<TeamRequest> requests = teamRequestRepository.findAllByReceiverId(receiverId);

        return requests.stream()
                .map(req -> {
                    User sender = userRepository.findById(req.getSenderId())
                            .orElseThrow(() -> new IllegalArgumentException("보낸 유저 정보가 없습니다."));

                    return TeamRequestResponseDto.builder()
                            .senderId(sender.getId())
                            .senderName(sender.getUserName())
                            .groupId(req.getGroupId())
                            .status(req.getStatus())
                            .requestedAt(req.getRequestedAt())
                            .build();
                })
                .toList();
    }

    public Optional<TeamRequestResponseDto> getRequestFromUser(Integer senderId, Integer receiverId) {
        return teamRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId)
                .map(req -> {
                    User sender = userRepository.findById(senderId)
                            .orElseThrow(() -> new IllegalArgumentException("보낸 유저 정보가 없습니다."));

                    return TeamRequestResponseDto.builder()
                            .senderId(sender.getId())
                            .senderName(sender.getUserName())
                            .groupId(req.getGroupId())
                            .status(req.getStatus())
                            .requestedAt(req.getRequestedAt())
                            .build();
                });
    }






    //------------- 밑에는 미완성
    public void handleTeamRequest(Integer senderId, Integer receiverId, Integer groupId) {
        Optional<TeamMember> senderTeamOpt = teamMemberRepository.findByUserIdAndGroupId(senderId, groupId);
        Optional<TeamMember> receiverTeamOpt = teamMemberRepository.findByUserIdAndGroupId(receiverId, groupId);

        // case 1: 단독 ↔ 단독
        if (senderTeamOpt.isEmpty() && receiverTeamOpt.isEmpty()) {
            createNewTeam(senderId, receiverId, groupId);
            return;
        }

        // case 2: 팀 ↔ 단독
        if (senderTeamOpt.isPresent() && receiverTeamOpt.isEmpty()) {
            Integer teamId = senderTeamOpt.get().getTeamId();
            addUserToTeam(teamId, receiverId);
            return;
        }

        // case 3: 단독 ↔ 팀
        if (senderTeamOpt.isEmpty() && receiverTeamOpt.isPresent()) {
            Integer teamId = receiverTeamOpt.get().getTeamId();
            addUserToTeam(teamId, senderId);
            return;
        }

        // case 4: 팀 ↔ 팀
        Integer teamId1 = senderTeamOpt.get().getTeamId();
        Integer teamId2 = receiverTeamOpt.get().getTeamId();

        // 리더는 sender의 기존 팀 리더 유지
        mergeTeams(teamId1, teamId2);
    }

    private void addUserToTeam(Integer senderId, Integer receiverId){}
    private void mergeTeams(Integer senderId, Integer receiverId){}


    private void createNewTeam(Integer senderId, Integer receiverId, Integer groupId) {
        // 1. 팀 타입 및 maxMembers 확인
        ProjectGroup group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));

        Integer typeId = group.getTeamMakeType();
        TeamType teamType = teamTypeRepository.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팀 빌딩 방식이 없습니다."));

        int max = teamType.getMaxMembers();
        if (2 > max) { // 사실상 팀이라 2명..체크를 안해도 되긴함.
            throw new IllegalArgumentException("팀 최대 인원 초과입니다.");
        }

        // 2. 팀 생성
        Team team = Team.builder()
                .groupId(groupId)
                .masterUserId(senderId) // 방장은 요청자(sender)
                .maxMembers(2)
                .status("모집중")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        teamRepository.save(team);

        // 3. 팀원 추가 (sender, receiver)
        List<TeamMember> members = List.of(
                TeamMember.builder()
                        .teamId(team.getId())
                        .userId(senderId)
                        .build(),
                TeamMember.builder()
                        .teamId(team.getId())
                        .userId(receiverId)
                        .build()
        );

        teamMemberRepository.saveAll(members);
    }

}
