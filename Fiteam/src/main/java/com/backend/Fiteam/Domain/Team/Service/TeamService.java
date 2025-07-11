package com.backend.Fiteam.Domain.Team.Service;

import com.backend.Fiteam.ConfigEnum.GlobalEnum.NotificationEventType;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.SenderType;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Notification.Service.NotificationService;
import com.backend.Fiteam.Domain.Team.Dto.TeamContactResponseDto;
import com.backend.Fiteam.Domain.Team.Entity.Team;
import com.backend.Fiteam.Domain.Team.Repository.TeamRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamRequestRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import com.backend.Fiteam.Domain.User.Service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TeamRequestRepository teamRequestRepository;
    private final UserService userService;
    private final ProjectGroupRepository projectGroupRepository;
    private final TeamTypeRepository teamTypeRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;


    @Transactional
    public void deleteTeamsAndRequestsByGroupId(Integer groupId) {
        // 1) 그룹에 속한 팀 조회
        List<Team> teams = teamRepository.findAllByGroupId(groupId);

        // 2) 각 팀에 달린 요청 삭제
        for (Team team : teams) {
            // 이 팀에 달린 모든 TeamRequest 레코드 삭제
            teamRequestRepository.deleteAllByTeamId(team.getId());
        }

        // 3) 팀 레코드 전체 삭제
        if (!teams.isEmpty()) {
            teamRepository.deleteAll(teams);
        }
    }

    @Transactional
    public void changeTeamMaster(Integer teamId, Integer userId, Integer new_master_id) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));
        if (!team.getMasterUserId().equals(userId)) {
            throw new IllegalArgumentException("현재 팀장만 팀장을 변경할 수 있습니다.");
        }

        boolean isMember = groupMemberRepository.findAllByTeamId(teamId).stream()
                .anyMatch(gm -> gm.getUserId().equals(new_master_id));
        if (!isMember) {
            throw new IllegalArgumentException("새 팀장은 현재 팀의 멤버여야 합니다.");
        }

        team.setMasterUserId(new_master_id);
        teamRepository.save(team);

        // 팀장 변경하고, 새로운 팀장한테 알림 보내기

        User prevLeader = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        String content = prevLeader.getUserName() + "님이 당신에게 팀장 권한을 넘겼습니다.";

        notificationService.createAndPushNotification(
                new_master_id,
                userId,
                SenderType.USER,
                NotificationEventType.TEAM_LEADER_CHANGE,
                content
        );
    }

    @Transactional(readOnly = true)
    public List<TeamContactResponseDto> getTeamContacts(Integer teamId, Integer currentUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));
        if (!team.getMasterUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("팀장만 접근할 수 있습니다.");
        }

        List<GroupMember> members = groupMemberRepository.findAllByTeamId(teamId);
        return members.stream()
                .map(gm -> userService.getContactForUser(gm.getUserId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void leaveTeam(Integer teamId, Integer userId) {
        // 1) 원래팀 존재 확인
        Team oldTeam = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        // 2) 팀장인지 검사
        if (oldTeam.getMasterUserId() != null && oldTeam.getMasterUserId().equals(userId)) {
            throw new IllegalArgumentException("팀장은 탈퇴할 수 없습니다. 먼저 팀장을 변경하세요.");
        }

        // 3) GroupMember 엔티티 조회 (팀 소속 여부 확인)
        GroupMember me = groupMemberRepository
                .findByUserIdAndTeamId(userId, teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팀의 멤버가 아닙니다."));

        // 4) 1인 팀 생성
        Team newTeam = Team.builder()
                .groupId(oldTeam.getGroupId())
                .masterUserId(userId)
                .maxMembers(oldTeam.getMaxMembers())
                .description(null)
                .teamStatus(TeamStatus.TEMP)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        newTeam.setTeamId(newTeam.getId());
        teamRepository.save(newTeam);

        // GroupMember 테이블에도 팀 정보 반영
        me.setTeamId(newTeam.getId());
        me.setTeamStatus(TeamStatus.WAITING);

        groupMemberRepository.save(me);
    }

    @Transactional
    public void confirmTeam(Integer teamId, Integer masterId) throws Exception {
        // 1) 팀 & 팀장 검증
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));
        if (!team.getMasterUserId().equals(masterId)) {
            throw new IllegalArgumentException("팀장만 팀 확정을 할 수 있습니다.");
        }

        // 2) 팀 빌딩 타입 조회
        Integer groupId = team.getGroupId();
        Integer typeId = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹이 존재하지 않습니다."))
                .getTeamMakeType().getId();
        TeamType type = teamTypeRepository.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("팀 빌딩 타입이 존재하지 않습니다."));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(type.getStartDatetime()) || now.isAfter(type.getEndDatetime())) {
            throw new IllegalStateException(
                    String.format("팀 확정은 %s부터 %s 사이에만 가능합니다.",
                            type.getStartDatetime(), type.getEndDatetime())
            );
        }

        // 2-1) 전체 팀원 수 검사
        List<GroupMember> members = groupMemberRepository.findAllByTeamId(teamId);
        int size = members.size();
        if (size < type.getMinMembers() || size > type.getMaxMembers()) {
            throw new IllegalArgumentException(
                    String.format("팀원 수(%d명)가 최소 %d명, 최대 %d명을 벗어납니다.",
                            size, type.getMinMembers(), type.getMaxMembers())
            );
        }

        // 2-2) positionBased=true 시 configJson 기반 직군별 인원 검사 (정수 또는 "min~max" 허용)
        if (Boolean.TRUE.equals(type.getPositionBased())) {
            String configJson = type.getConfigJson();
            if (configJson == null || configJson.isBlank()) {
                throw new IllegalArgumentException("직군별 구성 설정이 비어 있습니다.");
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(configJson);

            for (Iterator<Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                String position = entry.getKey();
                JsonNode constraint = entry.getValue();

                int minReq, maxReq;
                if (constraint.isInt()) {
                    minReq = maxReq = constraint.asInt();
                } else if(constraint.isTextual() && !constraint.asText().contains("~")){
                    minReq = maxReq = Integer.parseInt(constraint.asText().trim());
                } else if (constraint.isTextual() && constraint.asText().contains("~")) {
                    String[] parts = constraint.asText().split("~");
                    minReq = Integer.parseInt(parts[0].trim());
                    maxReq = Integer.parseInt(parts[1].trim());
                } else {
                    throw new IllegalArgumentException(
                            String.format("직군 %s 의 요구값(%s) 형식이 잘못되었습니다.", position, constraint.toString())
                    );
                }

                long actual = members.stream()
                        .filter(gm -> position.equals(gm.getPosition()))
                        .count();

                if (actual < minReq || actual > maxReq) {
                    throw new IllegalArgumentException(
                            String.format("직군 %s 인원(%d명)이 요구 범위[%d~%d]에 맞지 않습니다.",
                                    position, actual, minReq, maxReq)
                    );
                }
            }
        }

        // 3) 상태 변경
        members.forEach(gm -> gm.setTeamStatus(TeamStatus.FIXED));
        groupMemberRepository.saveAll(members);

        team.setTeamStatus(TeamStatus.CLOSED);
        teamRepository.save(team);
    }


}

