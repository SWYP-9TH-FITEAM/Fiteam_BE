package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.Domain.Chat.Entity.ChatRoom;
import com.backend.Fiteam.Domain.Chat.Entity.ManagerChatRoom;
import com.backend.Fiteam.Domain.Chat.Repository.ChatMessageRepository;
import com.backend.Fiteam.Domain.Chat.Repository.ChatRoomRepository;
import com.backend.Fiteam.Domain.Chat.Repository.ManagerChatRoomRepository;
import com.backend.Fiteam.Domain.Group.Dto.CreateGroupRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupListDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupStatusDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerProfileResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.UpdateGroupRequestDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Entity.Manager;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.GroupNoticeRepository;
import com.backend.Fiteam.Domain.Group.Repository.ManagerRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.Team.Service.TeamService;
import com.backend.Fiteam.Domain.User.Repository.UserLikeRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ProjectGroupRepository projectGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TeamTypeRepository teamTypeRepository;
    private final ManagerRepository managerRepository;
    private final TeamService teamService;
    private final GroupNoticeRepository groupNoticeRepository;
    private final UserLikeRepository userLikeRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ManagerChatRoomRepository managerChatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(readOnly = true)
    public void authorizeManager(Integer groupId, Integer managerId) {
        ProjectGroup group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found with id: " + groupId));
        if (!group.getManager().getId().equals(managerId)) {
            throw new IllegalArgumentException("이 그룹을 관리할 권한이 없습니다.");
        }
    }


    @Transactional(readOnly = true)
    public ManagerProfileResponseDto getManagerBasicProfile(Integer managerId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 매니저입니다."));
        return new ManagerProfileResponseDto(
                manager.getId(),
                manager.getManagerName(),
                manager.getProfileImgUrl()
        );
    }

    private String calculateStatus(TeamType tt, LocalDateTime now) {
        if (now.isBefore(tt.getStartDatetime())) {
            return "PENDING";
        } else if (now.isAfter(tt.getEndDatetime())) {
            return "ENDED";
        } else {
            return "ONGOING";
        }
    }

    @Transactional(readOnly = true)
    public List<ManagerGroupListDto> getManagerGroupList(Integer managerId) {
        LocalDateTime now = LocalDateTime.now();

        List<ProjectGroup> projectGroups = projectGroupRepository.findAllWithTeamTypeByManagerId(managerId);
        List<ManagerGroupListDto> result = new ArrayList<>();

        for (ProjectGroup pg : projectGroups) {
            TeamType tt = pg.getTeamMakeType();
            ManagerGroupListDto dto = ManagerGroupListDto.builder()
                    .id(pg.getId())
                    .name(pg.getName())
                    .status(calculateStatus(tt, now))
                    .build();
            result.add(dto);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ManagerGroupStatusDto> getManagedGroupStatuses(Integer managerId) {
        List<ProjectGroup> groups = projectGroupRepository.findAllByManagerId(managerId);
        LocalDateTime now = LocalDateTime.now();
        List<ManagerGroupStatusDto> result = new ArrayList<>();

        for (ProjectGroup pg : groups) {
            int memberCount = groupMemberRepository.countByGroupIdAndIsAcceptedTrue(pg.getId());
            TeamType tt = pg.getTeamMakeType();

            ManagerGroupStatusDto dto = ManagerGroupStatusDto.builder()
                    .groupId(pg.getId())
                    .groupName(pg.getName())
                    .memberCount(memberCount)
                    .positionBased(tt.getPositionBased())
                    .status(calculateStatus(tt, now))
                    .build();

            result.add(dto);
        }

        return result;
    }

    @Transactional
    public List<ManagerGroupResponseDto> getManagedGroups(Integer managerId) {
        LocalDateTime now = LocalDateTime.now();

        List<ProjectGroup> projectGroups = projectGroupRepository.findAllWithTeamTypeByManagerId(managerId);
        List<ManagerGroupResponseDto> result = new ArrayList<>();

        for (ProjectGroup pg : projectGroups) {
            TeamType tt = pg.getTeamMakeType();
            if (tt != null && tt.getEndDatetime().isAfter(now)) {
                ManagerGroupResponseDto dto = new ManagerGroupResponseDto(
                        pg.getId(),
                        pg.getName(),
                        pg.getDescription(),
                        pg.getCreatedAt(),
                        tt.getEndDatetime()
                );
                result.add(dto);
            }
        }

        return result;
    }

    @Transactional
    public Integer createGroup(Integer managerId, CreateGroupRequestDto requestDto) {
        // 중복 그룹 이름 검증 (같은 매니저가 동일 이름으로 생성했는지)
        if (projectGroupRepository.existsByManagerIdAndName(managerId, requestDto.getName())) {
            throw new IllegalArgumentException("이미 동일한 이름의 그룹이 존재합니다.");
        }
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new NoSuchElementException("관리자 정보를 찾을 수 없습니다."));

        ProjectGroup projectGroup = ProjectGroup.builder()
                .manager(manager)
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

    @Transactional(readOnly = true)
    public ProjectGroup getProjectGroup(Integer groupId) {
        return projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found with id: " + groupId));
    }

    @Transactional
    public void updateGroup(Integer groupId, UpdateGroupRequestDto requestDto) {
        ProjectGroup projectGroup = getProjectGroup(groupId);
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
    public void deleteGroup(Integer groupId) {
        // 1) 그룹 존재 확인
        ProjectGroup group = getProjectGroup(groupId);

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
        TeamType teamType = group.getTeamMakeType();
        if (teamType != null) {
            teamTypeRepository.delete(teamType);
        }
    }

}
