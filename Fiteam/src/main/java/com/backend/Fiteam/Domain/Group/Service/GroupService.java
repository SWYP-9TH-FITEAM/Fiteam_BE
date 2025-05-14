package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.ConfigQuartz.TeamBuildingSchedulerService;
import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
import com.backend.Fiteam.Domain.Group.Dto.CreateGroupRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupDetailResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupInvitedResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupTeamTypeSettingDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupListDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupStatusDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerProfileResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.UpdateGroupRequestDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Entity.Manager;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ManagerRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Notification.Service.NotificationService;
import com.backend.Fiteam.Domain.Team.Entity.Team;
import com.backend.Fiteam.Domain.Team.Repository.TeamRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamRequestRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.Team.Service.TeamService;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class GroupService {

    private final ProjectGroupRepository projectGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final TeamTypeRepository teamTypeRepository;
    private final CharacterCardRepository characterCardRepository;
    private final TeamService teamService;
    private final TeamRepository teamRepository;
    private final TeamBuildingSchedulerService schedulerService;
    private final NotificationService notificationService;


    @Transactional(readOnly = true)
    public ProjectGroup getProjectGroup(Integer groupId) {
        return projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found with id: " + groupId));
    }

    @Transactional
    public void createGroup(Integer managerId, CreateGroupRequestDto requestDto) {
        // 중복 그룹 이름 검증 (같은 매니저가 동일 이름으로 생성했는지)
        if (projectGroupRepository.existsByManagerIdAndName(managerId, requestDto.getName())) {
            throw new IllegalArgumentException("이미 동일한 이름의 그룹이 존재합니다.");
        }

        ProjectGroup projectGroup = ProjectGroup.builder()
                .managerId(managerId)
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .maxUserCount(requestDto.getMaxUserCount())
                .teamMakeType(null)
                .contactPolicy(requestDto.getContactPolicy())
                .build();

        projectGroupRepository.save(projectGroup);
    }

    @Transactional
    public void setTeamType(Integer groupId, GroupTeamTypeSettingDto requestDto) throws SchedulerException {
        // 1) 그룹 조회
        ProjectGroup projectGroup = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        Integer teamTypeId = projectGroup.getTeamMakeType();
        TeamType teamType;

        // configJson을 문자열로 변환 (positionBased=true인 경우에만)
        String configJsonStr = null;
        if (Boolean.TRUE.equals(requestDto.getPositionBased())) {
            try {
                configJsonStr = new ObjectMapper().writeValueAsString(requestDto.getConfigJson());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("configJson 직렬화 실패", e);
            }
        }

        if (teamTypeId == null) {
            // A) 새 TeamType 생성

            teamType = TeamType.builder()
                    .name(requestDto.getName())
                    .description(requestDto.getDescription())
                    .startDatetime(requestDto.getStartDatetime())
                    .endDatetime(requestDto.getEndDatetime())
                    .minMembers(requestDto.getMinMembers())
                    .maxMembers(requestDto.getMaxMembers())
                    .positionBased(requestDto.getPositionBased())
                    .configJson(configJsonStr)
                    .buildingDone(false)
                    .build();
            teamType = teamTypeRepository.save(teamType);

            // 그룹에 FK 연결 & 저장
            projectGroup.setTeamMakeType(teamType.getId());
            projectGroupRepository.save(projectGroup);
        } else {
            // B) 기존 TeamType 수정
            teamType = teamTypeRepository.findById(teamTypeId)
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 TeamType입니다."));
            teamType.setName(requestDto.getName());
            teamType.setDescription(requestDto.getDescription());
            teamType.setStartDatetime(requestDto.getStartDatetime());
            teamType.setEndDatetime(requestDto.getEndDatetime());
            teamType.setMinMembers(requestDto.getMinMembers());
            teamType.setMaxMembers(requestDto.getMaxMembers());
            teamType.setPositionBased(requestDto.getPositionBased());
            teamType.setConfigJson(configJsonStr);
            teamType.setBuildingDone(false);
            teamTypeRepository.save(teamType);

            // 현재 그룹의 팀 maxMembers 값 일괄 업데이트
            List<Team> teams = teamRepository.findByGroupId(groupId);
            for (Team team : teams) {
                team.setMaxMembers(requestDto.getMaxMembers());
            }
            teamRepository.saveAll(teams);
        }

        // ─────────── Quartz 스케줄러 등록 ───────────
        // TeamType.startDatetime 기준으로 자동 팀빌딩 예약
        // (이미 등록된 Job이 있으면 덮어쓰도록 구현되어 있음)
        schedulerService.scheduleTeamBuilding(projectGroup);
    }

    @Transactional
    protected boolean isValidDatetimeRange(
            LocalDateTime requestStart,
            LocalDateTime requestEnd,
            LocalDateTime existingStart,
            LocalDateTime existingEnd
    ) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime start = (requestStart != null) ? requestStart : existingStart;
        LocalDateTime end   = (requestEnd != null)   ? requestEnd   : existingEnd;

        if (start == null || end == null) {
            return false;
        }

        return now.isBefore(start) && start.isBefore(end);
    }



    @Transactional
    public GroupInvitedResponseDto inviteUsersToGroup(Integer groupId, List<String> emails, Integer managerId) {
        List<String> alreadyInGroupEmails = new ArrayList<>();
        List<String> notFoundEmails = new ArrayList<>();
        int successCount = 0;

        // 1. 현재 그룹 인원 수 조회
        int currentMemberCount = groupMemberRepository.countByGroupId(groupId);

        // 2. 그룹 최대 인원 수 조회
        ProjectGroup projectGroup = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));
        int maxUserCount = projectGroup.getMaxUserCount();

        for (String email : emails) {
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                boolean alreadyMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId());
                if (alreadyMember) {
                    alreadyInGroupEmails.add(email);
                    continue;
                }

                // 3. 초대 전에 최대 인원수 초과 여부 체크
                if ((currentMemberCount + successCount) >= maxUserCount) {
                    // 인원 초과 - 초대 실패 처리
                    alreadyInGroupEmails.add(email); // or 별도 초과 리스트로 관리할 수도 있음
                    continue;
                }

                // 4. 초대 진행
                GroupMember groupMember = GroupMember.builder()
                        .groupId(groupId)
                        .userId(user.getId())
                        .isAccepted(false)
                        .ban(false)
                        .build();
                groupMemberRepository.save(groupMember);
                successCount++;

                // —— 여기서 알림 생성 & 푸시 호출 ——
                notificationService.createAndPushNotification(
                        user.getId(),                                              // 수신자
                        managerId,                                                 // 발신자 (매니저)
                        "manager",                                                 // 발신자 타입
                        "Group invite",                                            // 알림 유형
                        groupId,                                                   // 관련 테이블 PK (groupId)
                        projectGroup.getName() + "에 초대되었습니다."              // 알림 내용
                );

            } else {
                notFoundEmails.add(email);
            }
        }

        return GroupInvitedResponseDto.builder()
                .successCount(successCount)
                .alreadyInGroupEmails(alreadyInGroupEmails)
                .notFoundEmails(notFoundEmails)
                .build();
    }

    @Transactional
    public void updateGroup(ProjectGroup projectGroup, UpdateGroupRequestDto requestDto) {
        if (requestDto.getName() != null) {
            projectGroup.setName(requestDto.getName());
        }
        if (requestDto.getDescription() != null) {
            projectGroup.setDescription(requestDto.getDescription());
        }
        if (requestDto.getMaxUserCount() != null) {
            projectGroup.setMaxUserCount(requestDto.getMaxUserCount());
        }
        if (requestDto.getContactPolicy() != null) {
            projectGroup.setContactPolicy(requestDto.getContactPolicy());
        }

        projectGroupRepository.save(projectGroup);
    }

    @Transactional
    public void RandomTeamBuilding(ProjectGroup projectGroup) {
        Integer groupId = projectGroup.getId();

        // 1) TeamType 조회 & 검증
        TeamType teamType = teamTypeRepository.findById(projectGroup.getTeamMakeType())
                .orElseThrow(() -> new NoSuchElementException("팀 빌딩 설정이 없습니다."));
        if (Boolean.TRUE.equals(teamType.getPositionBased())) {
            throw new IllegalArgumentException("랜덤 팀빌딩은 positionBased=false 일 때만 사용 가능합니다.");
        }

        // 2) 기존 팀 삭제 및 멤버 초기화
        List<Team> oldTeams = teamRepository.findByGroupId(groupId);
        if (!oldTeams.isEmpty()) {
            // 2-1) 멤버의 teamId, teamStatus 초기화
            List<GroupMember> allAccepted = groupMemberRepository
                    .findAllByGroupIdAndIsAcceptedTrue(groupId);
            allAccepted.forEach(gm -> {
                gm.setTeamId(null);
                gm.setTeamStatus(null);
            });
            groupMemberRepository.saveAll(allAccepted);

            // 2-2) 팀 삭제
            teamRepository.deleteAll(oldTeams);
        }

        // 3) 수락된 멤버 목록 재조회
        List<GroupMember> acceptedMembers = groupMemberRepository
                .findAllByGroupIdAndIsAcceptedTrue(groupId);

        // 4) 랜덤 팀빌딩 수행
        executeRandomTeamBuilding(groupId, acceptedMembers, teamType);
    }


    @Transactional
    protected void executeRandomTeamBuilding(Integer groupId,
            List<GroupMember> members,
            TeamType teamType) {
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
                    .masterUserId(masterId)
                    .name("Team " + (i + 1))
                    .maxMembers(tmembers.size())
                    .description("랜덤 팀빌딩 결과")
                    .status("모집종료")
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            newTeam = teamRepository.save(newTeam);

            // 멤버들 teamId, teamStatus 업데이트
            Team finalNewTeam = newTeam;
            tmembers.forEach(gm -> {
                gm.setTeamId(finalNewTeam.getId());
                gm.setTeamStatus("JOINED");
            });
            groupMemberRepository.saveAll(tmembers);
        }
    }

    @Transactional
    public void banGroupMember(Integer groupId, Integer groupMemberId) {
        // 1) 해당 멤버 조회
        GroupMember gm = groupMemberRepository.findById(groupMemberId)
                .orElseThrow(() -> new NoSuchElementException("해당 그룹 멤버가 없습니다."));

        // 2) 멤버가 요청된 그룹 소속인지 확인
        if (!gm.getGroupId().equals(groupId)) {
            throw new IllegalArgumentException("멤버가 해당 그룹에 속해있지 않습니다.");
        }

        // 3) 차단 처리: 수락 취소 + ban 플래그 설정
        gm.setIsAccepted(false);
        gm.setBan(true);
    }

    @Transactional
    public void cancelGroupInvitationByEmail(Integer groupId, String email) {
        // 1) 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일의 사용자가 없습니다."));

        // 2) GroupMember 조회 (수락 전)
        GroupMember gm = groupMemberRepository
                .findByGroupIdAndUserId(groupId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("해당 그룹에 초대된 기록이 없습니다."));

        if (Boolean.TRUE.equals(gm.getIsAccepted())) {
            throw new IllegalArgumentException("이미 수락된 멤버는 취소할 수 없습니다.");
        }

        // 3) 레코드 삭제
        groupMemberRepository.delete(gm);
    }

    @Transactional
    public void deleteGroup(Integer groupId) {
        // 1) 그룹 존재 확인
        ProjectGroup group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        // 2) 그룹 멤버 모두 삭제
        List<GroupMember> members = groupMemberRepository.findAllByGroupId(groupId);
        if (!members.isEmpty()) {
            groupMemberRepository.deleteAll(members);
        }

        // 3) 팀 + 팀 요청 삭제 (TeamService)
        teamService.deleteTeamsAndRequestsByGroupId(groupId);

        // 5) 그룹에 연결된 TeamType 설정 삭제
        Integer teamTypeId = group.getTeamMakeType();
        if (teamTypeId != null) {
            teamTypeRepository.deleteById(teamTypeId);
        }

        // 6) 마지막으로 그룹 자체 삭제
        projectGroupRepository.delete(group);
    }

    @Transactional(readOnly = true)
    public GroupDetailResponseDto getGroupDetail(Integer groupId) {
        // 1) ProjectGroup 조회
        ProjectGroup pg = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        // 2) 관련 TeamType 조회 (teamMakeType 필드가 없다면 예외 또는 null 처리)
        Integer ttId = pg.getTeamMakeType();
        if (ttId == null) {
            throw new NoSuchElementException("팀 빌딩 타입이 설정되지 않았습니다.");
        }
        TeamType tt = teamTypeRepository.findById(ttId)
                .orElseThrow(() -> new NoSuchElementException("등록된 팀 빌딩 타입이 없습니다."));

        // 3) DTO 생성 및 반환
        return new GroupDetailResponseDto(
                pg.getId(),
                pg.getName(),
                pg.getDescription(),
                pg.getMaxUserCount(),
                pg.getContactPolicy(),
                pg.getCreatedAt(),

                tt.getId(),
                tt.getName(),
                tt.getDescription(),
                tt.getStartDatetime(),
                tt.getEndDatetime(),
                tt.getMinMembers(),
                tt.getMaxMembers(),
                tt.getPositionBased(),
                tt.getConfigJson()
        );
    }



}
