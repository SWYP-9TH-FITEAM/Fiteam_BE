package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.ConfigEnum.GlobalEnum;
import com.backend.Fiteam.ConfigQuartz.TeamBuildingSchedulerService;
import com.backend.Fiteam.Domain.Group.Dto.GroupDetailResponseDto;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class GroupService {

    private final ProjectGroupRepository projectGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TeamTypeRepository teamTypeRepository;
    private final TeamRepository teamRepository;
    private final TeamBuildingSchedulerService schedulerService;
    private final NotificationService notificationService;


    @Transactional(readOnly = true)
    public ProjectGroup getProjectGroup(Integer groupId) {
        return projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found with id: " + groupId));
    }
    public void notifyAllGroupMembersById(Integer groupId, GlobalEnum.SenderType senderType,
            GlobalEnum.NotificationEventType type, String content) {
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

}
