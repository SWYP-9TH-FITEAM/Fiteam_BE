package com.backend.Fiteam.Group.Service;

import com.backend.Fiteam.Group.Dto.CreateGroupRequestDto;
import com.backend.Fiteam.Group.Dto.GroupInvitedResponseDto;
import com.backend.Fiteam.Group.Entity.GroupMember;
import com.backend.Fiteam.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.User.Entity.User;
import com.backend.Fiteam.User.Repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
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

    @Transactional
    public void createGroup(CreateGroupRequestDto requestDto) {
        ProjectGroup projectGroup = ProjectGroup.builder()
                .managerId(requestDto.getManagerId())
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .maxUserCount(requestDto.getMaxUserCount())
                .teamMakeType(requestDto.getTeamMakeType())
                .contactPolicy(requestDto.getContactPolicy())
                .build();

        projectGroupRepository.save(projectGroup);
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





}
