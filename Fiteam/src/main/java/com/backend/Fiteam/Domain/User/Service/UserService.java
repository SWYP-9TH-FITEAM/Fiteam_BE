package com.backend.Fiteam.Domain.User.Service;

import com.backend.Fiteam.Domain.Character.Entity.CharacterCard;
import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Notification.Repository.NotificationRepository;
import com.backend.Fiteam.Domain.User.Dto.UserCardDto;
import com.backend.Fiteam.Domain.User.Dto.UserGroupProfileDto;
import com.backend.Fiteam.Domain.User.Dto.UserProfileDto;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@RestControllerAdvice
public class UserService {

    private final UserRepository userRepository;
    private final CharacterCardRepository characterCardRepository;
    private final GroupMemberRepository groupMemberRepository;

    public UserProfileDto getUserProfile(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        return new UserProfileDto(user.getUserName(), user.getProfileImgUrl());
    }

    public UserCardDto getUserProfileCard(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다. id: " + userId));

        if (user.getCardId1() == null) {
            throw new NoSuchElementException("해당 유저는 CharactorCard 가 없습니다.");
        }

        CharacterCard card = characterCardRepository.findById(user.getCardId1())
                .orElseThrow(() -> new NoSuchElementException("해당 카드 정보를 찾을 수 없습니다. id: " + user.getCardId1()));

        return UserCardDto.builder()
                .code(card.getCode())
                .name(card.getName())
                .summary(card.getSummary())
                .teamStrength(card.getTeamStrength())
                .caution(card.getCaution())
                .bestMatchCode(card.getBestMatchCode())
                .bestMatchReason(card.getBestMatchReason())
                .worstMatchCode(card.getWorstMatchCode())
                .worstMatchReason(card.getWorstMatchReason())
                .details(user.getDetails())
                .ei(user.getNumEI())
                .pd(user.getNumPD())
                .ia(user.getNumIA())
                .cl(user.getNumCL())
                .build();
    }



    @Transactional
    public void acceptGroupInvitation(Integer groupId, Integer userId) {
        GroupMember groupMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("초대 내역이 존재하지 않습니다."));

        if (Boolean.TRUE.equals(groupMember.getIsAccepted())) {
            throw new IllegalArgumentException("이미 수락한 초대입니다.");
        }

        groupMember.setIsAccepted(true);
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


}
