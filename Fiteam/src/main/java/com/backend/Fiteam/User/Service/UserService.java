package com.backend.Fiteam.User.Service;

import com.backend.Fiteam.Character.Entity.CharacterCard;
import com.backend.Fiteam.Character.Repository.CharacterCardRepository;
import com.backend.Fiteam.Group.Entity.GroupMember;
import com.backend.Fiteam.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Notification.Repository.NotificationRepository;
import com.backend.Fiteam.User.Dto.UserCardDto;
import com.backend.Fiteam.User.Dto.UserNotifyDto;
import com.backend.Fiteam.User.Dto.UserProfileDto;
import com.backend.Fiteam.User.Entity.User;
import com.backend.Fiteam.User.Repository.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.AllArgsConstructor;
import lombok.Data;
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
    private final NotificationRepository notificationRepository;
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

    public List<UserNotifyDto> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserId(userId).stream()
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt())) // 최신순 정렬
                .map(notification -> UserNotifyDto.builder()
                        .senderType(notification.getSenderType())
                        .senderId(notification.getSenderId())
                        .content(notification.getContent())
                        .isRead(notification.getIsRead())
                        .createdAt(notification.getCreatedAt())
                        .build())
                .toList();
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

}
