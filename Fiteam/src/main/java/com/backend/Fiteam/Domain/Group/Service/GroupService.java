package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.Domain.Group.Dto.CreateGroupRequestDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupInvitedResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupMemberResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupTeamTypeSettingDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Team.Entity.TeamType;
import com.backend.Fiteam.Domain.Team.Repository.TeamRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class GroupService {

    private final ProjectGroupRepository projectGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final TeamTypeRepository teamTypeRepository;

    @Transactional(readOnly = true)
    public ProjectGroup getProjectGroup(Integer groupId) {
        return projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found with id: " + groupId));
    }

    @Transactional
    public void createGroup(Integer managerId, CreateGroupRequestDto requestDto) {
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
    public void setTeamType(Integer groupId, GroupTeamTypeSettingDto requestDto) {
        // 1. 그룹 조회
        ProjectGroup projectGroup = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        if (projectGroup.getTeamMakeType() == null) {
            // 팀타입이 없는 경우 → 새로 생성
            TeamType newTeamType = TeamType.builder()
                    .name(requestDto.getName())
                    .description(requestDto.getDescription())
                    .startDatetime(requestDto.getStartDatetime())
                    .endDatetime(requestDto.getEndDatetime())
                    .minMembers(requestDto.getMinMembers())
                    .maxMembers(requestDto.getMaxMembers())
                    .positionBased(requestDto.getPositionBased())
                    .configJson(requestDto.getConfigJson())
                    .build();
            teamTypeRepository.save(newTeamType);

            // 그룹에 연결
            projectGroup.setTeamMakeType(newTeamType.getId());
        } else {
            // 팀타입이 이미 존재하는 경우 → 수정
            TeamType existingTeamType = teamTypeRepository.findById(projectGroup.getTeamMakeType())
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 TeamType입니다."));

            existingTeamType.setName(requestDto.getName());
            existingTeamType.setDescription(requestDto.getDescription());
            existingTeamType.setStartDatetime(requestDto.getStartDatetime());
            existingTeamType.setEndDatetime(requestDto.getEndDatetime());
            existingTeamType.setMinMembers(requestDto.getMinMembers());
            existingTeamType.setMaxMembers(requestDto.getMaxMembers());
            existingTeamType.setPositionBased(requestDto.getPositionBased());
            existingTeamType.setConfigJson(requestDto.getConfigJson());
        }
    }




    @Transactional
    public GroupInvitedResponseDto inviteUsersToGroup(Integer groupId, List<String> emails) {
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

    @Transactional(readOnly = true)
    public boolean isUserInGroup(Integer groupId, Integer userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponseDto> getGroupMembers(Integer groupId) {
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(groupId);
        List<GroupMemberResponseDto> result = new ArrayList<>();

        for (GroupMember member : groupMembers) {
            if (Boolean.TRUE.equals(member.getIsAccepted())) {
                User user = userRepository.findById(member.getUserId())
                        .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));

                GroupMemberResponseDto dto = new GroupMemberResponseDto(
                        user.getId(),
                        user.getUserName(),
                        user.getCardId1(),
                        member.getTeamStatus(),
                        member.getPosition()
                );

                result.add(dto);
            }
        }
        return result;
    }



}
