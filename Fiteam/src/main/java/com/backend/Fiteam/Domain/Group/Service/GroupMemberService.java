package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.Domain.Character.Dto.CompatibilityUserInfoDto;
import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import com.backend.Fiteam.Domain.Character.Service.CharacterCardService;
import com.backend.Fiteam.Domain.Character.Service.CharacterCardService.CompatibilityResult;
import com.backend.Fiteam.Domain.Group.Dto.CompatibilityResultDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupMemberMiniProfileResponseDto;
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
import com.backend.Fiteam.Domain.User.Entity.UserLike;
import com.backend.Fiteam.Domain.User.Repository.UserLikeRepository;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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
    private final UserLikeRepository userLikeRepository;
    private final CharacterCardService characterCardService;

    // 로그인한 상태의 User가 해당 그룹에 소속된 사람인지 확인하는 검증코드
    public void validateGroupMembership(Integer userId, Integer groupId) {
        boolean isMember = groupMemberRepository.existsByGroupIdAndUserIdAndIsAcceptedTrue(groupId, userId);
        if (!isMember) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }
    }

    @Transactional
    public void updateGroupMemberProfile(Integer groupId, Integer userId, UserGroupProfileDto requestDto) {
        GroupMember groupMember = groupMemberRepository.findByUserIdAndGroupIdAndIsAcceptedTrue(userId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("본인의 그룹 멤버 정보만 수정할 수 있습니다."));

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
        if (requestDto.getProjectPurpose() != null) {
            groupMember.setProjectPurpose(requestDto.getProjectPurpose());
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

    @Transactional(readOnly = true)
    public GroupMemberMiniProfileResponseDto getMemberMiniProfile(Integer groupId, Integer userId) {
        // 1) groupId + userId 로 승인된 GroupMember 조회
        GroupMember gm = groupMemberRepository
                .findByUserIdAndGroupIdAndIsAcceptedTrue(userId, groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹 멤버 정보를 찾을 수 없습니다."));

        // 2) User 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자 정보를 찾을 수 없습니다."));

        return GroupMemberMiniProfileResponseDto.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .imageUrl(user.getProfileImgUrl())
                .position(gm.getPosition())
                .teamStatus(gm.getTeamStatus())
                .teamId(gm.getTeamId())
                .projectGoal(gm.getProjectGoal())
                .cardId(user.getCardId1())
                .build();
    }

    @Transactional(readOnly = true)
    public GroupMemberProfileResponseDto getMemberProfile(Integer groupId, Integer userId) {
        // 1) groupId + userId 로 승인된 GroupMember 조회
        GroupMember gm = groupMemberRepository
                .findByUserIdAndGroupIdAndIsAcceptedTrue(userId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 그룹 프로필이 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저 정보가 없습니다."));

        return GroupMemberProfileResponseDto.builder()
                .cardId(user.getCardId1())
                .numEI(user.getNumEI())
                .numPD(user.getNumPD())
                .numVA(user.getNumVA())
                .numCL(user.getNumCL())
                .position(gm.getPosition())
                .workHistory(gm.getWorkHistory())
                .projectGoal(gm.getProjectGoal())
                .projectPurpose(gm.getProjectPurpose())
                .url(gm.getUrl())
                .introduction(gm.getIntroduction())
                .build();
    }

    @Transactional(readOnly = true)
    public GroupMemberProfileResponseDto getMemberProfile(Integer memberId) {
        // 1) groupId + userId 로 승인된 GroupMember 조회
        GroupMember gm = groupMemberRepository
                .findByIdAndIsAcceptedTrue(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 그룹 프로필이 없습니다."));

        User user = userRepository.findById(gm.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저 정보가 없습니다."));

        return GroupMemberProfileResponseDto.builder()
                .cardId(user.getCardId1())
                .numEI(user.getNumEI())
                .numPD(user.getNumPD())
                .numVA(user.getNumVA())
                .numCL(user.getNumCL())
                .position(gm.getPosition())
                .workHistory(gm.getWorkHistory())
                .projectGoal(gm.getProjectGoal())
                .projectPurpose(gm.getProjectPurpose())
                .url(gm.getUrl())
                .introduction(gm.getIntroduction())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean isUserInGroup(Integer groupId, Integer userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    // userLike 하고, 마감된 으로 정렬해야함.
    @Transactional(readOnly = true)
    public List<GroupMemberResponseDto> getGroupMembers(Integer userId, Integer groupId, boolean isUser) {
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(groupId);
        List<GroupMemberResponseDto> result = new ArrayList<>();

        for (GroupMember member : groupMembers) {
            if (Boolean.TRUE.equals(member.getIsAccepted())) {
                User targetUser = userRepository.findById(member.getUserId())
                        .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));

                // Manager인 경우 무조건 null, User인 경우 체크
                Integer likeId = null;
                if (isUser) {
                    likeId = userLikeRepository.findBySenderIdAndReceiverIdAndGroupId(userId, targetUser.getId(), groupId)
                            .map(UserLike::getId).orElse(null);
                }

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

        // 정렬 우선순위:
        result.sort(Comparator
                .comparing((GroupMemberResponseDto dto) -> dto.getLikeId() == null)  // false 먼저
                .thenComparing(dto -> "마감".equals(dto.getTeamStatus()))             // false 먼저
        );

        return result;
    }

    @Transactional(readOnly = true)
    public CompatibilityResult getCompatibility(Integer myUserId, Integer otherUserId) {
        // 1. 유저 정보 조회
        User myUser = userRepository.findById(myUserId)
                .orElseThrow(() -> new NoSuchElementException("내 정보를 찾을 수 없습니다."));
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new NoSuchElementException("상대방 정보를 찾을 수 없습니다."));

        if (myUser.getCardId1() == null || otherUser.getCardId1() == null) {
            throw new IllegalArgumentException("두 사용자 모두 성향검사를 완료해야 합니다.");
        }

        CompatibilityUserInfoDto userA = CompatibilityUserInfoDto.builder()
                .cardId(myUser.getCardId1())
                .numEI(myUser.getNumEI())
                .numPD(myUser.getNumPD())
                .numVA(myUser.getNumVA())
                .numCL(myUser.getNumCL())
                .build();

        CompatibilityUserInfoDto userB = CompatibilityUserInfoDto.builder()
                .cardId(otherUser.getCardId1())
                .numEI(otherUser.getNumEI())
                .numPD(otherUser.getNumPD())
                .numVA(otherUser.getNumVA())
                .numCL(otherUser.getNumCL())
                .build();

        return characterCardService.calculateCompatibilityScore(userA, userB);
    }


}
