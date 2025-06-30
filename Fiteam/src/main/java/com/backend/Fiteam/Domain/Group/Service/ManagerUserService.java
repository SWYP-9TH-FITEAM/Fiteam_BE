package com.backend.Fiteam.Domain.Group.Service;


import com.backend.Fiteam.ConfigEnum.GlobalEnum.NotificationEventType;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.SenderType;
import com.backend.Fiteam.Domain.Group.Dto.GroupInvitedResponseDto;
import com.backend.Fiteam.Domain.Group.Dto.GroupMemberResponseDto;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Notification.Service.NotificationService;
import com.backend.Fiteam.Domain.Team.Repository.TeamRepository;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Entity.UserLike;
import com.backend.Fiteam.Domain.User.Repository.UserLikeRepository;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManagerUserService {

    private final ProjectGroupRepository projectGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final NotificationService notificationService;
    private final UserLikeRepository userLikeRepository;


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


    /*
    inviteUsersToGroup 함수
    1. 시간복잡도 : O(n + m) -> n+M < 300
        - n = 이메일 수, m = 현재 그룹의 기존 멤버 수
        - userRepository.findByEmailIn() → O(n)
        - groupMemberRepository.findByGroupId() → O(m)
        - for 루프 내 각 이메일에 대해 Set, Map 조회 및 저장/알림 처리 → O(n)
    2. DB Read : 총 4회
        - groupMemberRepository.countByGroupId(groupId) → 1회
        - projectGroupRepository.findById(groupId) → 1회
        - userRepository.findByEmailIn(emails) → 1회
        - groupMemberRepository.findByGroupId(groupId) → 1회
    3. DB Write : 최대 n회
        - groupMemberRepository.save() → 최대 n회
        - notificationService.createAndPushNotification() → 최대 n회
    4. 병렬처리 여부 : ❌
    */
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

        // 1. 이메일로 유저 일괄 조회
        List<User> users = userRepository.findByEmailIn(emails);
        Map<String, User> userByEmail = users.stream()
                .collect(Collectors.toMap(User::getEmail, Function.identity()));

        // 2. 현재 그룹 멤버 ID Set 생성
        Set<Integer> existingMemberIds = groupMemberRepository.findByGroupId(groupId).stream()
                .map(GroupMember::getUserId).collect(Collectors.toSet());
        for (String email : emails) {
            User user = userByEmail.get(email);
            if (user == null) {
                notFoundEmails.add(email);
                continue;
            }
            if (existingMemberIds.contains(user.getId())) {
                alreadyInGroupEmails.add(email);
                continue;
            }
            if ((currentMemberCount + successCount) >= maxUserCount) {
                alreadyInGroupEmails.add(email);
                continue;
            }

            // 초대 저장
            GroupMember groupMember = GroupMember.builder()
                    .groupId(groupId)
                    .userId(user.getId())
                    .isAccepted(false)
                    .ban(false)
                    .build();
            groupMemberRepository.save(groupMember);
            successCount++;

            // 그룹 초대 알림 발송
            notificationService.createAndPushNotification(
                    user.getId(),
                    managerId,
                    SenderType.MANAGER,
                    NotificationEventType.GROUP_INVITE,
                    projectGroup.getName() + "에 초대되었습니다."
            );
        }

        return GroupInvitedResponseDto.builder()
                .successCount(successCount)
                .alreadyInGroupEmails(alreadyInGroupEmails)
                .notFoundEmails(notFoundEmails)
                .build();
    }

    // 이 함수에서는 jpa로 한번에 삭제도 가능했지만, 예외처리 관점에서 아래 방식으로 구성함.
    @Transactional
    public void cancelGroupInvitationByEmail(Integer groupId, String email) {
        // 1) 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일의 사용자가 없습니다."));

        // 2) GroupMember 조회 (수락 전)
        GroupMember gm = groupMemberRepository.findByUserIdAndGroupId(user.getId(),groupId)
                .orElseThrow(() -> new NoSuchElementException("해당 그룹에 초대된 기록이 없습니다."));

        if (Boolean.TRUE.equals(gm.getIsAccepted())) {
            throw new IllegalArgumentException("이미 수락된 멤버는 취소할 수 없습니다.");
        }

        // 3) 레코드 삭제
        groupMemberRepository.delete(gm);
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

}
