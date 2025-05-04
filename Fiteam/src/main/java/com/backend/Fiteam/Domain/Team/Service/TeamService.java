package com.backend.Fiteam.Domain.Team.Service;

import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Team.Entity.Team;
import com.backend.Fiteam.Domain.Team.Repository.TeamRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamRequestRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TeamRequestRepository teamRequestRepository;

    @Transactional
    public void createTeams(Integer groupId, List<List<GroupMember>> teams) {
        for (int i = 0; i < teams.size(); i++) {
            List<GroupMember> teamMembers = teams.get(i);

            Team team = Team.builder()
                    .groupId(groupId)
                    .name("최적화 팀 #" + (i + 1))
                    .build();

            teamRepository.save(team);

            for (GroupMember gm : teamMembers) {
                gm.setTeamId(team.getId());
                gm.setTeamStatus("JOINED");
                groupMemberRepository.save(gm);
            }
        }
    }

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
}

