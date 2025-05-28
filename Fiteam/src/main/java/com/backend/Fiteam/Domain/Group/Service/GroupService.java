package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.ConfigQuartz.TeamBuildingSchedulerService;
import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
import com.backend.Fiteam.Domain.Chat.Entity.ChatRoom;
import com.backend.Fiteam.Domain.Chat.Entity.ManagerChatRoom;
import com.backend.Fiteam.Domain.Chat.Repository.ChatMessageRepository;
import com.backend.Fiteam.Domain.Chat.Repository.ChatRoomRepository;
import com.backend.Fiteam.Domain.Chat.Repository.ManagerChatRoomRepository;
import com.backend.Fiteam.Domain.Group.Dto.CreateGroupRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupDetailResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupInvitedResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupTeamTypeSettingDto;
import com.backend.Fiteam.Domain.Group.Dto.UpdateGroupRequestDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.GroupNoticeRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Notification.Service.NotificationService;
import com.backend.Fiteam.Domain.Team.Dto.TeamContactResponseDto;
import com.backend.Fiteam.Domain.Team.Entity.Team;
import com.backend.Fiteam.Domain.Team.Repository.TeamRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.Team.Service.TeamService;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserLikeRepository;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import com.backend.Fiteam.Domain.User.Service.UserService;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final GroupNoticeService groupNoticeService;
    private final GroupNoticeRepository groupNoticeRepository;
    private final UserLikeRepository userLikeRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ManagerChatRoomRepository managerChatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;


    @Transactional(readOnly = true)
    public ProjectGroup getProjectGroup(Integer groupId) {
        return projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found with id: " + groupId));
    }
    private void notifyAllGroupMembersById(Integer groupId, String senderType, String type, String content) {
        Integer managerId = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹이 없습니다."))
                .getManagerId();

        List<GroupMember> members =
                groupMemberRepository.findAllByGroupIdAndIsAcceptedTrue(groupId);

        for (GroupMember gm : members) {
            notificationService.createAndPushNotification(
                    gm.getUserId(),
                    managerId,
                    senderType,
                    type,
                    content
            );
        }
    }

    public void notifyTeamLeadersWithContacts(Integer groupId) {
        // 발신자(그룹 관리자) ID
        Integer managerId = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹이 없습니다: " + groupId))
                .getManagerId();

        // 1) 그룹 내 모든 팀 가져오기
        List<Team> teams = teamRepository.findAllByGroupId(groupId);

        for (Team team : teams) {
            Integer masterId = team.getMasterUserId();
            if (masterId == null) continue;

            // 2) 해당 팀의 팀원 연락처(팀장 자신 제외) 수집
            List<TeamContactResponseDto> contacts = groupMemberRepository
                    .findAllByTeamId(team.getId()).stream()
                    .filter(gm -> !gm.getUserId().equals(masterId))
                    .map(gm -> userService.getContactForUser(gm.getUserId()))
                    .collect(Collectors.toList());

            if (contacts.isEmpty()) continue;

            // 3) 메시지 조립
            StringBuilder msg = new StringBuilder()
                    .append("팀 [").append(team.getName()).append("] 연락처:\n");
            contacts.forEach(c -> {
                msg.append(c.getUserName())
                        .append(": ").append(c.getPhoneNumber());
                msg.append("\n");
            });

            // 4) 알림 발송
            notificationService.createAndPushNotification(
                    masterId,            // 수신자: 팀장
                    managerId,           // 발신자: 그룹 관리자
                    "SYSTEM",            // 발신자 타입
                    "TEAM_CONTACTS",     // 알림 유형
                    msg.toString()       // 알림 내용
            );
        }
    }
    // -------------------------

    @Transactional
    public Integer createGroup(Integer managerId, CreateGroupRequestDto requestDto) {
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
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        projectGroupRepository.save(projectGroup);
        return projectGroup.getId();
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

        // 5) 랜덤 빌딩 완료후 멤버들에게 알림 전송
        notifyAllGroupMembersById(
                groupId,
                "MANAGER",
                "RANDOM_TEAM_BUILDING_RESULT",
                "그룹의 랜덤 팀빌딩이 완료되었습니다. 팀 결과를 확인해 주세요."
        );

        // 6) 랜덤 팀빌딩 완료 공지 생성
        GroupNoticeRequestDto noticeDto = new GroupNoticeRequestDto();
        noticeDto.setGroupId(groupId);
        noticeDto.setTitle("랜덤 팀빌딩 완료 안내");
        noticeDto.setContext("그룹의 랜덤 팀빌딩이 완료되었습니다. 결과를 확인해 주세요.");
        // projectGroup.getManagerId()가 매니저 ID 입니다
        groupNoticeService.createNotice(projectGroup.getManagerId(), noticeDto);
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
                    .teamId(i+1)
                    .masterUserId(masterId)
                    .name("Team " + (i + 1))
                    .maxMembers(tmembers.size())
                    .description("랜덤 팀빌딩 결과")
                    .status("모집마감")
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            newTeam = teamRepository.save(newTeam);

            // 멤버들 teamId, teamStatus 업데이트
            Team finalNewTeam = newTeam;
            tmembers.forEach(gm -> {
                gm.setTeamId(finalNewTeam.getId());
                gm.setTeamStatus("모집마감");
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

        // 4) 소속된 팀이 있으면 삭제하고, teamId 클리어
        Integer teamId = gm.getTeamId();
        if (teamId != null) {
            teamRepository.deleteById(teamId);   // 팀 자체를 삭제
            gm.setTeamId(null);                 // 연관 필드도 비워줌
        }
    }

    @Transactional
    public void cancelGroupInvitationByEmail(Integer groupId, String email) {
        // 1) 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일의 사용자가 없습니다."));

        // 2) GroupMember 조회 (수락 전)
        GroupMember gm = groupMemberRepository
                .findByUserIdAndGroupId(user.getId(),groupId)
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
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다: " + groupId));

        // 2) 그룹 공지 삭제
        groupNoticeRepository.deleteAllByGroupId(groupId);

        // 3) 사용자 좋아요(유저-그룹 매핑) 삭제
        userLikeRepository.deleteAllByGroupId(groupId);

        // 4) 채팅방 및 채팅메시지 삭제
        // 4-1) 일반 사용자 채팅방
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByGroupId(groupId);
        if (!chatRooms.isEmpty()) {
            List<Integer> roomIds = chatRooms.stream()
                    .map(ChatRoom::getId).toList();
            chatMessageRepository.deleteAllByChatRoomIdIn(roomIds);
            chatRoomRepository.deleteAll(chatRooms);
        }

        // 4-2) 매니저-사용자 채팅방
        List<ManagerChatRoom> mgrRooms = managerChatRoomRepository.findAllByGroupId(groupId);
        if (!mgrRooms.isEmpty()) {
            managerChatRoomRepository.deleteAll(mgrRooms);
            // ChatMessage 는 일반 ChatRoom 과만 연관되어 있으므로 추가 삭제 불필요
        }

        // 5) 그룹 멤버 모두 삭제
        List<GroupMember> members = groupMemberRepository.findAllByGroupId(groupId);
        if (!members.isEmpty()) {
            groupMemberRepository.deleteAll(members);
        }

        // 6) 팀 + 팀 요청 삭제
        teamService.deleteTeamsAndRequestsByGroupId(groupId);

        // 7) 마지막으로 그룹 자체 삭제
        projectGroupRepository.delete(group);

        // 8) 관련 TeamType 설정 삭제
        Integer teamTypeId = group.getTeamMakeType();
        if (teamTypeId != null) {
            teamTypeRepository.deleteById(teamTypeId);
        }


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

    @Transactional
    public void openPositionBasedRequests(ProjectGroup group) {
        List<Team> teams = teamRepository.findAllByGroupIdAndStatus(group.getId(), "대기중");

        teams.forEach(t -> t.setStatus("모집중"));
        teamRepository.saveAll(teams);

        // 팀빌딩 시작하면서 유저들에게 알림 전송
        notifyAllGroupMembersById(
                group.getId(),
                "manager",
                "TEAM_BUILDING_START",
                group.getName()+"의 팀 빌딩이 시작되었습니다."
        );

        GroupNoticeRequestDto noticeDto = new GroupNoticeRequestDto();
        noticeDto.setGroupId(group.getId());
        noticeDto.setTitle(group.getName()+"팀 빌딩 시작되었습니다.");
        noticeDto.setContext(group.getName()+"그룹의 직군별 팀 빌딩이 시작되었습니다. 팀 빌딩을 진행해 주세요.");
        // projectGroup.getManagerId()가 매니저 ID 입니다
        groupNoticeService.createNotice(group.getManagerId(), noticeDto);
    }

    @Transactional
    public void closeTeamBuilding(ProjectGroup group) {
        Integer groupId = group.getId();

        // 1) 팀 상태 변경
        List<Team> teams = teamRepository.findAllByGroupIdAndStatus(groupId, "모집중");
        teams.forEach(t -> t.setStatus("모집마감"));
        teamRepository.saveAll(teams);

        // 2) 멤버 teamStatus 변경
        List<GroupMember> members = groupMemberRepository.findAllByGroupId(groupId);
        members.forEach(m -> m.setTeamStatus("팀확정"));
        groupMemberRepository.saveAll(members);

        // 3) 모든 멤버에게 알림
        notifyAllGroupMembersById(
                groupId,
                "manager",                      // 발신자 타입
                "TEAM_BUILDING_END",           // 알림 유형
                group.getName() + " 팀 빌딩이 종료되었습니다."
        );

        // 4) 팀 빌딩 종료 후 팀장에게 팀원들 연락처 알림으로 전송
        // notifyTeamLeadersWithContacts(groupId);
    }

}
