package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.Domain.Group.Dto.GroupMemberProfileResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupMemberResponseDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.User.Dto.UserGroupProfileDto;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Service
@RequiredArgsConstructor
@RestControllerAdvice
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;
    private final ProjectGroupRepository projectGroupRepository;
    private final TeamTypeRepository teamTypeRepository;
    private final UserRepository userRepository;


    // 로그인한 상태의 User가 해당 그룹에 소속된 사람인지 확인하는 검증코드
    public void validateGroupMembership(Integer userId, Integer groupId) {
        boolean isMember = groupMemberRepository.existsByGroupIdAndUserIdAndIsAcceptedTrue(groupId, userId);
        if (!isMember) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }
    }

    @Transactional
    public void updateGroupMemberProfile(Integer groupMemberId, Integer userId, UserGroupProfileDto requestDto) {
        GroupMember groupMember = groupMemberRepository.findById(groupMemberId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 멤버를 찾을 수 없습니다."));

        if (!groupMember.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 그룹 멤버 정보만 수정할 수 있습니다.");
        }

        // null 체크 후 값이 있을 때만 수정
        if (requestDto.getPosition() != null) {
            groupMember.setPosition(requestDto.getPosition());
        }
        if (requestDto.getWorkHistory() != null) {
            groupMember.setWorkHistory(requestDto.getWorkHistory());
        }
        if (requestDto.getProjectGoal() != null) {
            groupMember.setProjectGoal(requestDto.getProjectGoal());
        }
        if (requestDto.getUrl() != null) {
            groupMember.setUrl(requestDto.getUrl());
        }
        if (requestDto.getIntroduction() != null) {
            groupMember.setIntroduction(requestDto.getIntroduction());
        }
    }

    // Config Josn 에서 직군 정리를 어떻게 할지에 따라 수정이 필요할수도 있음.
    public List<String> getPositionListForGroup(Integer groupId) throws JsonProcessingException {
        ProjectGroup group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));

        Integer typeId = group.getTeamMakeType();
        TeamType teamType = teamTypeRepository.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팀 빌딩 방식이 존재하지 않습니다."));

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


    public GroupMemberProfileResponseDto getMemberProfile(Integer targetUserId) {
        GroupMember member = groupMemberRepository.findTopByUserIdAndIsAcceptedTrue(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 그룹 프로필이 없습니다."));

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저 정보가 없습니다."));

        return GroupMemberProfileResponseDto.builder()
                .workHistory(member.getWorkHistory())
                .projectGoal(member.getProjectGoal())
                .url(member.getUrl())
                .introduction(member.getIntroduction())
                .details(user.getDetails())
                .numEI(user.getNumEI())
                .numPD(user.getNumPD())
                .numVA(user.getNumVA())
                .numCL(user.getNumCL())
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
