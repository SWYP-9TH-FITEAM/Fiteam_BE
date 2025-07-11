package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.ConfigEnum.GlobalEnum;
import com.backend.Fiteam.ConfigQuartz.TeamBuildingSchedulerService;
import com.backend.Fiteam.Domain.Group.Dto.GroupDetailResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupMemberResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupTeamTypeSettingDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Notification.Service.NotificationService;
import com.backend.Fiteam.Domain.Team.Entity.Team;
import com.backend.Fiteam.Domain.Team.Repository.TeamRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Entity.UserLike;
import com.backend.Fiteam.Domain.User.Repository.UserLikeRepository;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final ProjectGroupRepository projectGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TeamTypeRepository teamTypeRepository;
    private final TeamRepository teamRepository;
    private final TeamBuildingSchedulerService schedulerService;
    private final NotificationService notificationService;
    private final UserLikeRepository userLikeRepository;
    private final UserRepository userRepository;

    private final @Lazy ManagerService managerService;


    public void notifyAllGroupMembersById(Integer groupId, GlobalEnum.SenderType senderType,
            GlobalEnum.NotificationEventType type, String content) {
        Integer managerId = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹이 없습니다."))
                .getManager().getId();

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

    public boolean authorizeManagerOrMember(Integer groupId, Integer userId) {
        // 권한 가져오기
        boolean isManager = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_Manager"));
        if (isManager) {
            // 매니저 검증
            managerService.authorizeManager(groupId, userId);
        } else {
            // 일반 회원 검증
            boolean joined = groupMemberRepository
                    .existsByGroupIdAndUserIdAndIsAcceptedTrue(groupId, userId);
            if (!joined) {
                throw new IllegalArgumentException("해당 그룹 유저가 아닙니다.");
            }
        }
        return isManager;
    }

    /*
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
    */
    // -------------------------

    @Transactional(readOnly = true)
    public GroupDetailResponseDto getGroupDetail(Integer groupId) {
        // 1) ProjectGroup 조회
        ProjectGroup pg = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        // 2) 관련 TeamType 조회 (teamMakeType 필드가 없다면 예외 또는 null 처리)
        TeamType tt = pg.getTeamMakeType();
        if (tt == null) {
            throw new NoSuchElementException("팀 빌딩 타입이 설정되지 않았습니다.");
        }

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

    // 로그인한 상태의 User가 해당 그룹에 소속된 사람인지 확인하는 검증코드
    public void validateGroupMembership(Integer userId, Integer groupId) {
        boolean isMember = groupMemberRepository.existsByGroupIdAndUserIdAndIsAcceptedTrue(groupId, userId);
        if (!isMember) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }
    }

    // Config Josn 에서 직군 정리를 어떻게 할지에 따라 수정이 필요할수도 있음.
    @Transactional(readOnly = true)
    public List<String> getPositionListForGroup(Integer groupId) throws JsonProcessingException {
        ProjectGroup group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));

        TeamType teamType = group.getTeamMakeType();

        List<String> positions = new ArrayList<>();
        if (Boolean.FALSE.equals(teamType.getPositionBased())) {
            positions.add("normal");
            return positions;
        }

        String configJson = teamType.getConfigJson();
        if (configJson == null || configJson.isBlank()) {
            return Collections.emptyList();
        }

        // JSON 파싱: Object 형태로 가정
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(configJson);

        if (!root.isObject()) {
            return Collections.emptyList();
        }

        root.fieldNames().forEachRemaining(positions::add); // PM, DS, FE, BE 등 key만 추출
        return positions;
    }


    protected boolean isValidDatetimeRange(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        if (start == null || end == null) {
            return false;
        }

        return now.isBefore(start) && start.isBefore(end);
    }

    @Transactional
    public void setTeamType(Integer groupId, GroupTeamTypeSettingDto requestDto) throws SchedulerException {
        // 1) 그룹 조회
        ProjectGroup projectGroup = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        // Set 설정 유효시간인지 확인
        if (!isValidDatetimeRange(requestDto.getStartDatetime(), requestDto.getEndDatetime())) {
            throw new IllegalArgumentException("시작/종료 시간이 유효하지 않습니다.");
        }

        TeamType teamType = projectGroup.getTeamMakeType();

        // configJson을 문자열로 변환 (positionBased=true인 경우에만)
        String configJsonStr = null;
        if (Boolean.TRUE.equals(requestDto.getPositionBased())) {
            try {
                configJsonStr = new ObjectMapper().writeValueAsString(requestDto.getConfigJson());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("configJson 직렬화 실패", e);
            }
        }


        if (teamType == null) {
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
            projectGroup.setTeamMakeType(teamType);
            projectGroupRepository.save(projectGroup);
        } else {
            // B) 기존 TeamType 수정
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


    /*
    getGroupMembers 함수
    1. 시간복잡도 : O(n) - n < 100
    2. DB Read : 3회
        - groupMemberRepository.findByGroupId(groupId) → 1회
        - userRepository.findAllById(userIds) → 1회
        - userLikeRepository.findBySenderIdAndGroupId(userId, groupId) → 1회
    3. DB Write : 없음
    4. 병렬처리 필요 여부 : ❌
    5. 개선점
        - N+1 문제 발생 가능성 있음 → userRepository.findById() 및 userLikeRepository 호출이 개별 반복문 내 존재
            ➜ userId 리스트로 미리 userRepository.findAllById() 호출 후 Map으로 캐싱
            ➜ userLike도 (senderId, receiverId, groupId) 조합으로 한 번에 조회
        - 정렬 조건이 명확하지 않음: `likeId == null`, `"마감".equals(teamStatus)` 순서 명확히 문서화 필요
        - Stream API 사용 시 코드 간결성 개선 가능
    */
    @Transactional(readOnly = true)
    public List<GroupMemberResponseDto> getGroupMembers(Integer userId, Integer groupId, boolean isUser) {
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(groupId);
        List<GroupMemberResponseDto> result = new ArrayList<>();

        // 1. 유저 ID 리스트 수집
        List<Integer> userIds = groupMembers.stream()
                .filter(member -> Boolean.TRUE.equals(member.getIsAccepted()))
                .map(GroupMember::getUserId)
                .toList();

        // 2. 유저들 미리 조회
        Map<Integer, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 3. 좋아요 정보 미리 조회
        Map<Integer, Integer> likeIdMap = new HashMap<>();
        if (isUser) {
            List<UserLike> userLikes = userLikeRepository.findBySenderIdAndGroupId(userId, groupId);
            likeIdMap = userLikes.stream()
                    .collect(Collectors.toMap(UserLike::getReceiverId, UserLike::getId));
        }

        // 4. DTO 구성
        for (GroupMember member : groupMembers) {
            if (Boolean.TRUE.equals(member.getIsAccepted())) {
                User targetUser = userMap.get(member.getUserId());
                if (targetUser == null) {
                    throw new NoSuchElementException("유저 정보를 찾을 수 없습니다.");
                }

                Integer likeId = isUser ? likeIdMap.getOrDefault(targetUser.getId(), null) : null;

                GroupMemberResponseDto dto = GroupMemberResponseDto.builder()
                        .memberId(member.getId())
                        .userId(targetUser.getId())
                        .userName(targetUser.getUserName())
                        .profileImageUrl(targetUser.getProfileImgUrl())
                        .cardId1(targetUser.getCardId1())
                        .teamStatus(member.getTeamStatus())
                        .position(member.getPosition())
                        .teamId(member.getTeamId())
                        .likeId(likeId)
                        .build();

                result.add(dto);
            }
        }

        // 5. 정렬: 좋아요 먼저, 마감되지 않은사람 먼저
        result.sort(Comparator
                .comparing((GroupMemberResponseDto dto) -> dto.getLikeId() == null) // false (좋아요 있음) 먼저
                .thenComparing(dto -> "마감".equals(dto.getTeamStatus()))            // false (마감 아님) 먼저
        );

        return result;
    }
}
