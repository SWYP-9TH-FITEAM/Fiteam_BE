package com.backend.Fiteam.Domain.Chat.Service;

import com.backend.Fiteam.Domain.Chat.Dto.CreateManagerChatRoomRequestDto;
import com.backend.Fiteam.Domain.Chat.Dto.ManagerChatRoomListResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.ManagerChatRoomResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.ManagerChatMessageDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatMessageResponseDto;
import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage;
import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage.SenderType;
import com.backend.Fiteam.Domain.Chat.Entity.ManagerChatRoom;
import com.backend.Fiteam.Domain.Chat.Repository.ManagerChatRoomRepository;
import com.backend.Fiteam.Domain.Chat.Repository.ChatMessageRepository;
import com.backend.Fiteam.Domain.Group.Entity.Manager;
import com.backend.Fiteam.Domain.Group.Repository.ManagerRepository;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import com.backend.Fiteam.Domain.User.Service.UserService;
import com.backend.Fiteam.Domain.User.Dto.UserProfileDto;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManagerChatService {
    private final ManagerChatRoomRepository managerChatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ManagerRepository managerRepository;

    /**
     * 매니저-유저 채팅방 생성 또는 조회
     */
    @Transactional
    public ManagerChatRoomResponseDto createRoom(CreateManagerChatRoomRequestDto dto) {
        ManagerChatRoom room = managerChatRoomRepository
                .findByManagerIdAndUserId(dto.getManagerId(), dto.getUserId())
                .orElseGet(() -> {
                    ManagerChatRoom m = ManagerChatRoom.builder()
                            .managerId(dto.getManagerId())
                            .userId(dto.getUserId())
                            .groupId(dto.getGroupId())
                            .createdAt(new Timestamp(System.currentTimeMillis()))
                            .build();
                    return managerChatRoomRepository.save(m);
                });

        return ManagerChatRoomResponseDto.builder()
                .id(room.getId())
                .managerId(room.getManagerId())
                .userId(room.getUserId())
                .groupId(room.getGroupId())
                .createdAt(room.getCreatedAt())
                .build();
    }

    public List<ManagerChatRoomListResponseDto> getRoomsByManager(Integer managerId) {
        List<ManagerChatRoom> rooms = managerChatRoomRepository.findAllByManagerId(managerId);

        return rooms.stream()
                .map(r -> {
                    Integer otherId = r.getUserId();
                    Optional<User> otherOpt = userRepository.findById(otherId);
                    ChatMessage lastMsg = chatMessageRepository.findTopByChatRoomIdOrderBySentAtDesc(r.getId()).orElse(null);
                    long unreadCount = chatMessageRepository
                            .countByChatRoomIdAndSenderIdNotAndIsReadFalse(r.getId(), managerId);

                    return ManagerChatRoomListResponseDto.builder()
                            .id(r.getId())
                            .user_or_manager_Id(managerId) // 로그인한 본인
                            .otherType(SenderType.USER)
                            .otherId(otherId)
                            .groupId(r.getGroupId())
                            .otherName(otherOpt.map(User::getUserName).orElse("탈퇴한 유저"))
                            .otherProfileImgUrl(otherOpt.map(User::getProfileImgUrl).orElse(null))
                            .lastMessageContent(lastMsg != null ? lastMsg.getContent() : "")
                            .lastMessageTime(lastMsg != null ? lastMsg.getSentAt() : null)
                            .unreadMessageCount(unreadCount)
                            .createdAt(r.getCreatedAt())
                            .build();
                })
                .sorted(Comparator.comparing(ManagerChatRoomListResponseDto::getLastMessageTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<ManagerChatRoomListResponseDto> getRoomsByUser(Integer userId) {
        List<ManagerChatRoom> rooms = managerChatRoomRepository.findAllByUserId(userId);

        return rooms.stream()
                .map(r -> {
                    Optional<Manager> other = managerRepository.findById(r.getManagerId());
                    ChatMessage lastMsg = chatMessageRepository
                            .findTopByChatRoomIdOrderBySentAtDesc(r.getId())
                            .orElse(null);
                    long unread = chatMessageRepository
                            .countByChatRoomIdAndSenderIdNotAndIsReadFalse(r.getId(), userId);

                    return ManagerChatRoomListResponseDto.builder()
                            .id(r.getId())
                            .user_or_manager_Id(userId)
                            .otherType(SenderType.MANAGER)
                            .otherId(r.getManagerId())
                            .groupId(r.getGroupId())
                            .otherName(other.map(Manager::getManagerName).orElse("탈퇴한 매니저"))
                            .otherProfileImgUrl(null)
                            .lastMessageContent(lastMsg != null ? lastMsg.getContent() : "")
                            .unreadMessageCount(unread)
                            .lastMessageTime(lastMsg != null ? lastMsg.getSentAt() : null)
                            .createdAt(r.getCreatedAt())
                            .build();
                })
                .sorted(Comparator.comparing(
                        ManagerChatRoomListResponseDto::getLastMessageTime,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());
    }


    /**
     * 권한 확인: 매니저가 해당 방에 속해있는지
     */
    public void verifyManagerInRoom(Integer roomId, Integer loginId) {
        managerChatRoomRepository.findById(roomId)
                .filter(r -> r.getManagerId().equals(loginId))
                .orElseThrow(() -> new AccessDeniedException("이 Manager는 채팅방 접근 권한이 없습니다."));

        managerChatRoomRepository.findById(roomId)
                .filter(r -> r.getUserId().equals(loginId))
                .orElseThrow(() -> new AccessDeniedException("채팅방 접근 권한이 없습니다."));
    }

    /**
     * 매니저↔유저 채팅 메시지 저장
     */
    @Transactional
    public ChatMessage saveMessage(ManagerChatMessageDto dto) {
        if (dto.getRoomId() == null
                || dto.getSenderId() == null
                || dto.getContent() == null
                || dto.getSenderType() == null) {
            throw new IllegalArgumentException("필수 값 누락");
        }

        ChatMessage msg = ChatMessage.builder()
                .chatRoomId(dto.getRoomId())
                .senderType(dto.getSenderType())
                .senderId(dto.getSenderId())
                .messageType(dto.getMessageType() != null ? dto.getMessageType() : "TEXT")
                .content(dto.getContent())
                .isRead(false)
                .sentAt(new Timestamp(System.currentTimeMillis()))
                .build();

        return chatMessageRepository.save(msg);
    }

    /**
     * 매니저-유저 메시지 페이징 조회
     */
    public Page<ChatMessageResponseDto> getMessagesForRoomPaged(Integer chatRoomId, Pageable pageable) {
        Page<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdOrderBySentAtDesc(chatRoomId, pageable);

        return messages.map(msg -> ChatMessageResponseDto.builder()
                .id(msg.getId())
                .chatRoomId(msg.getChatRoomId())
                .senderType(msg.getSenderType())
                .senderId(msg.getSenderId())
                .messageType(msg.getMessageType())
                .content(msg.getContent())
                .isRead(msg.getIsRead())
                .sentAt(msg.getSentAt())
                .build());
    }

    /**
     * 채팅방 정보 조회
     */
    public ManagerChatRoomResponseDto getChatRoomInfo(
            Integer roomId, Integer principalId, boolean isManager
    ) {
        ManagerChatRoom room = managerChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채팅방입니다."));  // :contentReference[oaicite:1]{index=1}

        if (isManager) {
            // 매니저가 볼 때: 상대는 유저
            UserProfileDto user = userService.getUserProfile(room.getUserId());
            return ManagerChatRoomResponseDto.builder()
                    .id(room.getId())
                    .managerId(room.getManagerId())
                    .userId(room.getUserId())
                    .groupId(room.getGroupId())
                    .createdAt(room.getCreatedAt())
                    .userName(user.getUserName())
                    .userProfileImgUrl(user.getProfileImgUrl())
                    .userJob(user.getJob())            // User의 직업
                    .build();
        } else {
            // 유저가 볼 때: 상대는 매니저
            Manager mgr = managerRepository.findById(room.getManagerId())
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 매니저입니다."));
            return ManagerChatRoomResponseDto.builder()
                    .id(room.getId())
                    .managerId(room.getManagerId())
                    .userId(room.getUserId())
                    .groupId(room.getGroupId())
                    .createdAt(room.getCreatedAt())
                    .userName(mgr.getManagerName())
                    .userProfileImgUrl(null)           // Manager에 프로필 URL 필드가 없어서.. null 처리
                    .userJob("Manager")                // 매니저 뷰에서는 Job을 "Manager"로 고정
                    .build();
        }
    }

    /**
     * 읽음 처리
     */
    public void markAllAsRead(Integer roomId, Integer managerId) {
        chatMessageRepository.markAllAsRead(roomId, managerId);
    }
}
