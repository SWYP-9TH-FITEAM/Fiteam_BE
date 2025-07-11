package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.ConfigEnum.GlobalEnum.NotificationEventType;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.SenderType;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;
import com.backend.Fiteam.ConfigQuartz.TeamBuildingSchedulerService;
import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
import com.backend.Fiteam.Domain.Group.Dto.GroupNoticeRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupTeamTypeSettingDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Notification.Service.NotificationService;
import com.backend.Fiteam.Domain.Team.Entity.Team;
import com.backend.Fiteam.Domain.Team.Repository.TeamRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.Team.Service.TeamService;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
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
public class TeamBuildingService {

    private final GroupMemberRepository groupMemberRepository;
    private final TeamTypeRepository teamTypeRepository;
    private final TeamRepository teamRepository;
    private final GroupNoticeService groupNoticeService;
    private final GroupService groupService;
    private final TeamBuildingRandomAlgorithm teamBuildingRandomAlgorithm;


    @Transactional
    public void RandomTeamBuilding(ProjectGroup projectGroup) {
        Integer groupId = projectGroup.getId();

        // 1) TeamType 조회 & 검증
        TeamType teamType = projectGroup.getTeamMakeType();
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
        teamBuildingRandomAlgorithm.executeRandomTeamBuilding(groupId, acceptedMembers, teamType);

        // 5) 랜덤 빌딩 완료후 멤버들에게 알림 전송
        groupService.notifyAllGroupMembersById(
                groupId,
                SenderType.MANAGER,
                NotificationEventType.RANDOM_TEAM_BUILDING_RESULT,
                "그룹의 랜덤 팀빌딩이 완료되었습니다. 팀 결과를 확인해 주세요."
        );

        // 6) 랜덤 팀빌딩 완료 공지 생성
        GroupNoticeRequestDto noticeDto = new GroupNoticeRequestDto();
        noticeDto.setGroupId(groupId);
        noticeDto.setTitle("랜덤 팀빌딩 완료 안내");
        noticeDto.setContext("그룹의 랜덤 팀빌딩이 완료되었습니다. 결과를 확인해 주세요.");
        // projectGroup.getManagerId()가 매니저 ID 입니다
        groupNoticeService.createNotice(projectGroup.getManager().getId(), noticeDto);
    }

    @Transactional
    public void openPositionBasedRequests(ProjectGroup group) {
        List<Team> teams = teamRepository.findAllByGroupIdAndTeamStatus(group.getId(), TeamStatus.WAITING);

        teams.forEach(t -> t.setTeamStatus(TeamStatus.RECRUITING));
        teamRepository.saveAll(teams);

        // 팀빌딩 시작하면서 유저들에게 알림 전송
        groupService.notifyAllGroupMembersById(
                group.getId(),
                SenderType.MANAGER,
                NotificationEventType.TEAM_BUILDING_START,
                group.getName()+"의 팀 빌딩이 시작되었습니다."
        );

        GroupNoticeRequestDto noticeDto = new GroupNoticeRequestDto();
        noticeDto.setGroupId(group.getId());
        noticeDto.setTitle(group.getName()+"팀 빌딩 시작되었습니다.");
        noticeDto.setContext(group.getName()+"그룹의 직군별 팀 빌딩이 시작되었습니다. 팀 빌딩을 진행해 주세요.");
        // projectGroup.getManagerId()가 매니저 ID 입니다
        groupNoticeService.createNotice(group.getManager().getId(), noticeDto);

        TeamType teamType = group.getTeamMakeType();
        if (teamType == null) {
            throw new NoSuchElementException("TeamType이 설정되지 않았습니다: 그룹 ID " + group.getId());
        }
        teamType.setStartDatetime(LocalDateTime.now());
        teamTypeRepository.save(teamType);
    }

    @Transactional
    public void closeTeamBuilding(ProjectGroup group) {
        Integer groupId = group.getId();

        // 1) 팀 상태 변경
        List<Team> teams = teamRepository.findAllByGroupIdAndTeamStatus(groupId, TeamStatus.RECRUITING);
        teams.forEach(t -> t.setTeamStatus(TeamStatus.CLOSED));
        teamRepository.saveAll(teams);

        // 2) 멤버 teamStatus 변경
        List<GroupMember> members = groupMemberRepository.findAllByGroupId(groupId);
        members.forEach(m -> m.setTeamStatus(TeamStatus.FIXED));
        groupMemberRepository.saveAll(members);

        // 3) 모든 멤버에게 알림
        groupService.notifyAllGroupMembersById(
                groupId,
                SenderType.MANAGER,                     // 발신자 타입
                NotificationEventType.TEAM_BUILDING_END,           // 알림 유형
                group.getName() + " 팀 빌딩이 종료되었습니다."
        );

        // 4) 팀 빌딩 종료 후 팀장에게 팀원들 연락처 알림으로 전송
        // notifyTeamLeadersWithContacts(groupId);

        // 5) TeamType.endDatetime 을 현재 시간으로 덮어쓰기
        TeamType teamType = group.getTeamMakeType();
        if (teamType == null) {
            throw new NoSuchElementException("TeamType이 설정되지 않았습니다: 그룹 ID " + group.getId());
        }
        teamType.setEndDatetime(LocalDateTime.now());
        teamTypeRepository.save(teamType);
    }
}
