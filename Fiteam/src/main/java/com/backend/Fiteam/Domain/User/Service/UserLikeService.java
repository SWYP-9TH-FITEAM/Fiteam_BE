package com.backend.Fiteam.Domain.User.Service;

import com.backend.Fiteam.Domain.Character.Repository.CharacterCardRepository;
import com.backend.Fiteam.Domain.Group.Repository.GroupMemberRepository;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.User.Dto.UserLikeRequestDto;
import com.backend.Fiteam.Domain.User.Dto.UserLikeResponseDto;
import com.backend.Fiteam.Domain.User.Entity.UserLike;
import com.backend.Fiteam.Domain.User.Repository.UserLikeRepository;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Service
@RequiredArgsConstructor
@RestControllerAdvice
public class UserLikeService {

    private final GroupMemberRepository groupMemberRepository;
    private final UserLikeRepository userLikeRepository;

    public void likeUser(Integer senderId, UserLikeRequestDto dto) {
        Integer receiverId = dto.getReceiverId();
        Integer groupId    = dto.getGroupId();

        // 1) 동일 그룹의 정식 멤버인지 검사
        boolean senderInGroup   = groupMemberRepository.existsByGroupIdAndUserIdAndIsAcceptedTrue(groupId, senderId);
        boolean receiverInGroup = groupMemberRepository.existsByGroupIdAndUserIdAndIsAcceptedTrue(groupId, receiverId);
        if (!senderInGroup || !receiverInGroup) {
            throw new IllegalArgumentException("같은 그룹 멤버만 좋아요할 수 있습니다.");
        }

        // 2) 자기 자신은 좋아요 불가
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("자기 자신에게는 좋아요할 수 없습니다.");
        }

        // 3) UserLike 엔티티 생성 및 저장
        UserLike like = UserLike.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .groupId(groupId)
                .number(dto.getNumber())
                .memo(dto.getMemo())
                .build();
        userLikeRepository.save(like);
    }

    public void unlikeUser(Integer senderId, Integer likeId) {
        UserLike like = userLikeRepository.findById(likeId)
                .orElseThrow(() -> new IllegalArgumentException("취소할 좋아요가 존재하지 않습니다."));
        if (!like.getSenderId().equals(senderId)) {
            throw new IllegalArgumentException("본인이 남긴 좋아요만 취소할 수 있습니다.");
        }
        userLikeRepository.delete(like);
    }

    public List<UserLikeResponseDto> getMyLikes(Integer senderId) {
        return userLikeRepository.findAllBySenderId(senderId).stream()
                .map(like -> UserLikeResponseDto.builder()
                        .likeId(like.getId())
                        .receiverId(like.getReceiverId())
                        .groupId(like.getGroupId())
                        .number(like.getNumber())
                        .createdAt(like.getCreatedAt())
                        .build()
                )
                .collect(Collectors.toList());
    }

    public String getLikeMemo(Integer senderId, Integer likeId) {
        UserLike like = userLikeRepository.findById(likeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좋아요입니다."));
        if (!like.getSenderId().equals(senderId)) {
            throw new AccessDeniedException("본인의 좋아요 메모만 조회할 수 있습니다.");
        }
        return like.getMemo();
    }

}
