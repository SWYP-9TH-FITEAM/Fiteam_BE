package com.backend.Fiteam.Domain.Chat.Service;

import com.backend.Fiteam.Domain.Chat.Dto.ChatMessageDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatMessageResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.CreateUserChatRoomRequestDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomListResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomResponseDto;
import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage;
import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage.SenderType;
import com.backend.Fiteam.Domain.Chat.Entity.ChatRoom;
import com.backend.Fiteam.Domain.Chat.Repository.ChatMessageRepository;
import com.backend.Fiteam.Domain.Chat.Repository.ChatRoomRepository;
import com.backend.Fiteam.Domain.User.Dto.UserProfileDto;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import com.backend.Fiteam.Domain.User.Service.UserService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SseEmitterService sseService;

    public ChatRoomResponseDto createChatRoom(Integer senderId, CreateUserChatRoomRequestDto dto) {
        if (senderId.equals(dto.getReceiverId())) {
            throw new IllegalArgumentException("자기 자신과의 채팅방은 생성할 수 없습니다.");
        }
        Integer user1 = Math.min(senderId, dto.getReceiverId());
        Integer user2 = Math.max(senderId, dto.getReceiverId());
        Integer groupId = dto.getGroupId();  // 그룹 ID

        Optional<ChatRoom> existing = chatRoomRepository.findByUser1IdAndUser2IdAndGroupId(user1, user2,groupId);
        if (existing.isPresent()) {
            ChatRoom room = existing.get();
            return ChatRoomResponseDto.builder()
                    .chatRoomId(room.getId())
                    .user1Id(room.getUser1Id())
                    .user2Id(room.getUser2Id())
                    .groupId(room.getGroupId())
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
        }

        ChatRoom newRoom = ChatRoom.builder()
                .user1Id(user1)
                .user2Id(user2)
                .groupId(groupId)
                .build();
        ChatRoom saved = chatRoomRepository.save(newRoom);

        return ChatRoomResponseDto.builder()
                .chatRoomId(saved.getId())
                .user1Id(saved.getUser1Id())
                .user2Id(saved.getUser2Id())
                .groupId(saved.getGroupId())
                .createdAt(saved.getCreatedAt())
                .build();
    }


    // N+1 DB repository접근 문제..
    public List<ChatRoomListResponseDto> getChatRoomsForUser(Integer userId, Integer groupId) {
        List<ChatRoom> allRooms = chatRoomRepository.findByUser1IdOrUser2Id(userId, userId);

        if(groupId != null){
            allRooms = allRooms.stream().filter(room -> groupId.equals(room.getGroupId())).collect(Collectors.toList());
        }

        return allRooms.stream().map(room -> {
                    Integer otherId = room.getUser1Id().equals(userId) ? room.getUser2Id() : room.getUser1Id();
                    Optional<User> otherUserOpt = userRepository.findById(otherId);

                    ChatMessage lastMsg = chatMessageRepository.findTopByChatRoomIdOrderBySentAtDesc(room.getId()).orElse(null);
                    long unreadCount = chatMessageRepository.countByChatRoomIdAndSenderIdNotAndIsReadFalse(room.getId(), userId);
                    return ChatRoomListResponseDto.builder()
                            .chatRoomId(room.getId())
                            .groupId(room.getGroupId())
                            .userId(userId)
                            .otherUserId(otherId)
                            .otherUserName(otherUserOpt.map(User::getUserName).orElse("탈퇴한 유저"))
                            .otherUserProfileImgUrl(otherUserOpt.map(User::getProfileImgUrl).orElse(null))
                            .lastMessageContent(lastMsg != null ? lastMsg.getContent() : "")
                            .lastMessageTime(lastMsg != null ? lastMsg.getSentAt() : null)
                            .unreadMessageCount(unreadCount)
                            .build();
                }).sorted(Comparator.comparing(ChatRoomListResponseDto::getLastMessageTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }
    /* 이게 이제 최적화 JOIN을 구현해야 사용할 수 있는..
    public List<ChatRoomListResponseDto> getChatRoomsForUser(Integer userId, Integer groupId) {
        return chatRoomRepository.findChatRoomListForUser(userId, groupId).stream()
                .map(p -> ChatRoomListResponseDto.builder()
                        .chatRoomId(p.getChatRoomId())
                        .groupId(p.getGroupId())
                        .userId(userId)
                        .otherUserId(p.getOtherUserId())
                        .otherUserName(Optional.ofNullable(p.getOtherUserName()).orElse("탈퇴한 유저"))
                        .otherUserProfileImgUrl(p.getOtherUserProfileImgUrl())
                        .lastMessageContent(Optional.ofNullable(p.getLastMessageContent()).orElse(""))
                        .lastMessageTime(p.getLastMessageTime())
                        .unreadMessageCount(p.getUnreadMessageCount() != null ? p.getUnreadMessageCount() : 0)
                        .build())
                .collect(Collectors.toList());
    }
    */

    public Page<ChatMessageResponseDto> getMessagesForRoomPaged(Integer chatRoomId, Pageable pageable) {
        Page<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderBySentAtDesc(chatRoomId, pageable);

        return messages.map(msg -> ChatMessageResponseDto.builder()
                .id(msg.getId())
                .chatRoomId(msg.getChatRoomId())
                .senderType(msg.getSenderType())  // 여기는 항상 User 타입
                .senderId(msg.getSenderId())
                .messageType(msg.getMessageType())
                .content(msg.getContent())
                .isRead(msg.getIsRead())
                .sentAt(msg.getSentAt())
                .build());
    }

    // TeamService에서 사용함
    public void sendTeamRequestMessage(Integer senderId, Integer receiverId, Integer groupId) {
        Integer user1 = Math.min(senderId, receiverId);
        Integer user2 = Math.max(senderId, receiverId);

        ChatRoom room = chatRoomRepository
                .findByUser1IdAndUser2IdAndGroupId(user1, user2, groupId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        String senderName = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."))
                .getUserName();

        /*
        ChatMessage teamRequestMessage = ChatMessage.builder()
                .chatRoomId(room.getId())
                .senderType(SenderType.USER)                  // 팀요청은 User만 가능해서
                .senderId(senderId)
                .content(senderName + "님이 팀 제안을 보냈습니다!")
                .isRead(false)
                .sentAt(new Timestamp(System.currentTimeMillis()))
                .messageType("TEAM_REQUEST")
                .build();

        ChatMessage saved = chatMessageRepository.save(teamRequestMessage);
        */

        // -- SSE 추가하기 --
        // 1) ChatRoom 메타데이터(createdAt을 updateAt 대신 사용) 갱신
        room.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        chatRoomRepository.save(room);

        // 2) 델타 DTO 생성
        Integer otherUserId = (room.getUser1Id().equals(senderId) ? room.getUser2Id() : room.getUser1Id());
        ChatRoomListResponseDto delta = ChatRoomListResponseDto.builder()
                .chatRoomId(room.getId())
                .otherUserId(otherUserId)
                .lastMessageContent(senderName+"이 팀 제안을 보냈습니다!") // content 필드 이름 확인
                .lastMessageTime(room.getCreatedAt())
                .unreadMessageCount(
                        chatMessageRepository.countByChatRoomIdAndSenderIdNotAndIsReadFalse(room.getId(), senderId)
                )
                .build();

        // 3) SSE 푸시 (보낸 사람 & 받는 사람)
        sseService.pushRoomUpdate(senderId, delta);
        sseService.pushRoomUpdate(otherUserId, delta);
    }

    // TeamService에서 사용함
    public void sendTeamResponseMessage(Integer senderId, Integer receiverId, Integer groupId, String type) {
        Integer user1 = Math.min(senderId, receiverId);
        Integer user2 = Math.max(senderId, receiverId);

        ChatRoom room = chatRoomRepository
                .findByUser1IdAndUser2IdAndGroupId(user1, user2, groupId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        String name = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."))
                .getUserName();

        /*
        ChatMessage acceptMessage = ChatMessage.builder()
                .chatRoomId(room.getId())
                .senderType(SenderType.USER)                  // 팀요청은 User만 가능해서
                .senderId(receiverId)
                .content(name + "님이 팀 제안을 " + type + "했습니다!")
                .isRead(false)
                .sentAt(new Timestamp(System.currentTimeMillis()))
                .messageType("TEAM_RESPONSE")
                .build();

        ChatMessage saved = chatMessageRepository.save(acceptMessage);
        */


        // ——— SSE 델타 푸시 ———
        room.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        chatRoomRepository.save(room);

        Integer otherUserId = (room.getUser1Id().equals(senderId) ? room.getUser2Id() : room.getUser1Id());
        ChatRoomListResponseDto delta = ChatRoomListResponseDto.builder()
                .chatRoomId(room.getId())
                .otherUserId(otherUserId)
                .lastMessageContent(name + "님이 팀 제안을 " + type + "했습니다!")
                .lastMessageTime(room.getCreatedAt())
                .unreadMessageCount(
                        chatMessageRepository.countByChatRoomIdAndSenderIdNotAndIsReadFalse(room.getId(), senderId)
                )
                .build();

        sseService.pushRoomUpdate(senderId, delta);
        sseService.pushRoomUpdate(otherUserId, delta);
    }

    @Transactional
    public ChatMessageResponseDto sendMessage(ChatMessageDto dto) {
        Integer senderId = dto.getSenderId();
        // 1) 메시지 저장
        ChatMessage message = ChatMessage.builder()
                .chatRoomId(dto.getChatRoomId())
                .senderType(dto.getSenderType()) // 여기는 무조건 User
                .senderId(dto.getSenderId())
                .messageType(dto.getMessageType() != null ? dto.getMessageType() : "TEXT")
                .content(dto.getContent())
                .isRead(false)
                .sentAt(new Timestamp(System.currentTimeMillis()))
                .build();
        ChatMessage saved = chatMessageRepository.save(message);

        // 2) ChatRoom 메타데이터 업데이트
        ChatRoom room = chatRoomRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new NoSuchElementException("채팅방이 없습니다: " + dto.getChatRoomId()));
        //room.setLastMessageContent(saved.getContent());
        room.setCreatedAt(saved.getSentAt());
        chatRoomRepository.save(room);

        // 3) 델타 DTO 생성
        Integer otherUserId = room.getUser1Id().equals(senderId) ? room.getUser2Id() : room.getUser1Id();
        Optional<User> otherOpt = userRepository.findById(otherUserId);
        ChatRoomListResponseDto delta = ChatRoomListResponseDto.builder()
                .chatRoomId(room.getId())
                .userId(senderId)
                .otherUserId(otherUserId)
                .otherUserName(otherOpt.map(User::getUserName).orElse("탈퇴한 유저"))
                .otherUserProfileImgUrl(otherOpt.map(User::getProfileImgUrl).orElse(null))
                .lastMessageContent(saved.getContent())
                .lastMessageTime(room.getCreatedAt())
                .unreadMessageCount(
                        chatMessageRepository.countByChatRoomIdAndSenderIdNotAndIsReadFalse(
                                room.getId(), senderId)
                )
                .build();

        // 4) SSE 푸시
        sseService.pushRoomUpdate(senderId, delta);
        sseService.pushRoomUpdate(otherUserId, delta);

        // 5) 응답 생성
        return ChatMessageResponseDto.builder()
                .id(saved.getId())
                .chatRoomId(saved.getChatRoomId())
                .senderType(saved.getSenderType())
                .senderId(saved.getSenderId())
                .messageType(saved.getMessageType())
                .content(saved.getContent())
                .isRead(saved.getIsRead())
                .sentAt(saved.getSentAt())
                .build();
    }

    public void verifyUserInRoom(Integer roomId, Integer userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 없습니다."));

        if (!room.getUser1Id().equals(userId) && !room.getUser2Id().equals(userId)) {
            throw new AccessDeniedException("채팅방 접근 권한이 없습니다.");
        }
    }

    // UserProfileDto profile = userService.getUserProfile(userId);
    public ChatRoomResponseDto getChatRoomInfo(Integer userId, Integer roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채팅방입니다."));

        // UserProfileDto 가져오기
        UserProfileDto profile1;
        UserProfileDto profile2;
        Integer userId2;
        if(room.getUser1Id() == userId){
            profile1 = userService.getUserProfile(room.getUser1Id());
            profile2 = userService.getUserProfile(room.getUser2Id());
            userId2 = room.getUser2Id();
        }else{
            profile1 = userService.getUserProfile(room.getUser2Id());
            profile2 = userService.getUserProfile(room.getUser1Id());
            userId2 = room.getUser1Id();
        }

        return ChatRoomResponseDto.builder()
                .chatRoomId(room.getId())
                .user1Id(userId)
                .user2Id(userId2)
                .groupId(room.getGroupId())
                .createdAt(room.getCreatedAt())

                // 유저1 정보
                .user1Name(profile1.getUserName())
                .user1ProfileImgUrl(profile1.getProfileImgUrl())
                .user1Job(profile1.getJob())

                // 유저2 정보
                .user2Name(profile2.getUserName())
                .user2ProfileImgUrl(profile2.getProfileImgUrl())
                .user2Job(profile2.getJob())
                .build();
    }

    public List<ChatRoomListResponseDto> searchChatRoomsForUser(Integer userId, String name) {
        String lowerName = name.toLowerCase();
        return getChatRoomsForUser(userId, null).stream()
                .filter(dto -> {
                    String other = dto.getOtherUserName();
                    return other != null && other.toLowerCase().contains(lowerName);
                }).collect(Collectors.toList());
    }

    public List<ChatRoomListResponseDto> searchChatRoomsForUser2(Integer userId, String name, Integer groupId) {
        String lowerName = name.toLowerCase();
        return getChatRoomsForUser(userId, groupId).stream()
                // 2) 그룹아이디가 일치하는 것만
                .filter(dto -> dto.getGroupId() != null && dto.getGroupId().equals(groupId))
                // 3) otherUserName에 검색어 포함 여부
                .filter(dto -> {
                    String other = dto.getOtherUserName();
                    return other != null && other.toLowerCase().contains(lowerName);
                })
                .collect(Collectors.toList());
    }


}
