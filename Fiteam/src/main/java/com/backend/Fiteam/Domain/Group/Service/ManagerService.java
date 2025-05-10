package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupListDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerGroupStatusDto;
import com.backend.Fiteam.Domain.Group.Dto.ManagerProfileResponseDto;
import com.backend.Fiteam.Domain.Group.Entity.Manager;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ManagerRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.Team.Service.TeamService;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    @Transactional(readOnly = true)
    public ProjectGroup getProjectGroup(Integer groupId) {
        return projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found with id: " + groupId));
    }

    @Transactional(readOnly = true)
    public ManagerProfileResponseDto getManagerBasicProfile(Integer managerId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 매니저입니다."));
        return new ManagerProfileResponseDto(
                manager.getId(),
                manager.getManagerName()
        );
    }

    @Transactional(readOnly = true)
    public List<ManagerGroupResponseDto> getManagedGroups(Integer managerId) {
        LocalDateTime now = LocalDateTime.now();

        return projectGroupRepository.findAllByManagerId(managerId).stream()
                .flatMap(pg -> {
                    Integer typeId = pg.getTeamMakeType();
                    if (typeId == null) return Stream.empty();
                    return teamTypeRepository.findById(typeId)
                            .filter(tt -> tt.getEndDatetime().isAfter(now))
                            .map(tt -> new ManagerGroupResponseDto(
                                    pg.getId(),
                                    pg.getName(),
                                    pg.getDescription(),
                                    pg.getCreatedAt(),
                                    tt.getEndDatetime()
                            ))
                            .stream();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ManagerGroupStatusDto> getManagedGroupStatuses(Integer managerId) {
        // 1) 매니저가 생성한 모든 그룹 조회
        List<ProjectGroup> groups = projectGroupRepository.findAllByManagerId(managerId);
        LocalDateTime now = LocalDateTime.now();
        return groups.stream()
                .map(pg -> {
                    // 2) 현재 수락된 멤버 수 조회
                    int memberCount = groupMemberRepository.countByGroupIdAndIsAcceptedTrue(pg.getId());

                    // 3) 연결된 TeamType 조회
                    TeamType tt = teamTypeRepository.findById(pg.getTeamMakeType())
                            .orElseThrow(() -> new NoSuchElementException("TeamType이 설정되지 않았습니다: " + pg.getTeamMakeType()));

                    // 4) DTO 반환
                    return ManagerGroupStatusDto.builder()
                            .groupId(pg.getId())
                            .groupName(pg.getName())
                            .memberCount(memberCount)
                            .positionBased(tt.getPositionBased())
                            .status(calculateStatus(tt, now))
                            .build();
                })
                .collect(Collectors.toList());
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

        return projectGroupRepository.findAllByManagerId(managerId)        // :contentReference[oaicite:4]{index=4}:contentReference[oaicite:5]{index=5}
                .stream()
                .map(pg -> {
                    Integer ttId = pg.getTeamMakeType();
                    if (ttId == null) {
                        throw new NoSuchElementException("TeamType이 설정되지 않았습니다: 그룹 " + pg.getId());
                    }
                    TeamType tt = teamTypeRepository.findById(ttId)
                            .orElseThrow(() -> new NoSuchElementException(
                                    "등록된 팀 빌딩 타입이 없습니다: " + ttId));          // :contentReference[oaicite:6]{index=6}:contentReference[oaicite:7]{index=7}

                    return ManagerGroupListDto.builder()
                            .id(pg.getId())
                            .name(pg.getName())
                            .status(calculateStatus(tt, now))
                            .build();
                })
                .collect(Collectors.toList());
    }
}
