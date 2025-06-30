package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;
import com.backend.Fiteam.ConfigQuartz.TeamBuildingSchedulerService;
import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Notification.Service.NotificationService;
import com.backend.Fiteam.Domain.Team.Entity.Team;
import com.backend.Fiteam.Domain.Team.Repository.TeamRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamBuildingRandomAlgorithm {

    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final CharacterCardRepository characterCardRepository;
    private final TeamRepository teamRepository;

    @Transactional
    protected void executeRandomTeamBuilding(Integer groupId, List<GroupMember> members, TeamType teamType) {
        int minMembers   = teamType.getMinMembers();
        int maxMembers   = teamType.getMaxMembers();
        int totalMembers = members.size();

        // 1) 팀 개수 및 기본 크기 계산
        int numTeams   = totalMembers / minMembers;
        int remainder  = totalMembers % minMembers;
        List<Integer> teamSizes = new ArrayList<>();
        for (int i = 0; i < numTeams; i++) teamSizes.add(minMembers);
        // 남은 인원 분배
        for (int idx = 0; remainder > 0; idx = (idx + 1) % teamSizes.size()) {
            if (teamSizes.get(idx) < maxMembers) {
                teamSizes.set(idx, teamSizes.get(idx) + 1);
                remainder--;
            }
        }

        // 2) 셔플 & 최적화 로직
        Collections.shuffle(members);
        Map<Integer,String> userChar = new HashMap<>();
        Map<String, CharacterCard> cardMap = new HashMap<>();
        for (GroupMember gm : members) {
            User user = userRepository.findById(gm.getUserId())
                    .orElseThrow(() -> new NoSuchElementException("User Not Found"));
            CharacterCard card = characterCardRepository.findById(user.getCardId1())
                    .orElseThrow(() -> new NoSuchElementException("Card Not Found"));
            userChar.put(gm.getUserId(), card.getCode());
            cardMap.put(card.getCode(), card);
        }
        List<List<GroupMember>> teams = new ArrayList<>();
        for (int sz : teamSizes)
            teams.add(new ArrayList<>());
        Set<Integer> assigned = new HashSet<>();

        for (GroupMember gm : members) {
            if (assigned.contains(gm.getUserId())) continue;
            String code  = userChar.get(gm.getUserId());
            CharacterCard card = cardMap.get(code);

            // best/worst 코드 조회
            String best1  = card.getBestMatchCode1();
            String best2  = card.getBestMatchCode2();
            String worst1 = card.getWorstMatchCode1();
            String worst2 = card.getWorstMatchCode2();

            int bestIdx = -1, bestScore = Integer.MIN_VALUE;
            // 점수 계산
            for (int i = 0; i < teams.size(); i++) {
                List<GroupMember> t = teams.get(i);
                if (t.size() >= teamSizes.get(i)) continue;
                int score = 0;
                for (GroupMember member : t) {
                    String mcode = userChar.get(member.getUserId());
                    if (mcode.equals(best1) || mcode.equals(best2)) score += 2;
                    if (mcode.equals(worst1) || mcode.equals(worst2)) score -= 3;
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestIdx   = i;
                }
            }
            // 매칭
            if (bestIdx != -1) {
                teams.get(bestIdx).add(gm);
                assigned.add(gm.getUserId());
            } else {
                teams.stream()
                        .filter(t -> t.size() < maxMembers)
                        .min(Comparator.comparingInt(List::size))
                        .ifPresent(t -> {
                            t.add(gm);
                            assigned.add(gm.getUserId());
                        });
            }
        }

        // 3) 새 팀 생성 & 멤버 재배치
        for (int i = 0; i < teams.size(); i++) {
            List<GroupMember> tmembers = teams.get(i);
            if (tmembers.isEmpty()) continue;

            // 마스터는 첫 번째 멤버
            Integer masterId = tmembers.get(0).getUserId();

            Team newTeam = Team.builder()
                    .groupId(groupId)
                    .teamId(i+1)
                    .masterUserId(masterId)
                    .name("Team " + (i + 1))
                    .maxMembers(tmembers.size())
                    .description("랜덤 팀빌딩 결과")
                    .teamStatus(TeamStatus.CLOSED)
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            newTeam = teamRepository.save(newTeam);

            // 멤버들 teamId, teamStatus 업데이트
            Team finalNewTeam = newTeam;
            tmembers.forEach(gm -> {
                gm.setTeamId(finalNewTeam.getId());
                gm.setTeamStatus(TeamStatus.CLOSED);
            });
            groupMemberRepository.saveAll(tmembers);
        }
    }

}
