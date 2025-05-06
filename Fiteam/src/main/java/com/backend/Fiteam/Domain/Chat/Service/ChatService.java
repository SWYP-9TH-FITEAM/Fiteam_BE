package com.backend.Fiteam.Domain.Chat.Service;

import com.backend.Fiteam.Domain.Chat.Dto.ChatMessageResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomCreateRequestDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomListResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomResponseDto;
import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage;
import com.backend.Fiteam.Domain.Chat.Entity.ChatRoom;
import com.backend.Fiteam.Domain.Chat.Repository.ChatMessageRepository;
import com.backend.Fiteam.Domain.Chat.Repository.ChatRoomRepository;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatRoomResponseDto createChatRoom(Integer senderId, ChatRoomCreateRequestDto dto) {
        Integer user1 = Math.min(senderId, dto.getReceiverId());
        Integer user2 = Math.max(senderId, dto.getReceiverId());

        Optional<ChatRoom> existing = chatRoomRepository.findByUser1IdAndUser2Id(user1, user2);
        if (existing.isPresent()) {
            ChatRoom room = existing.get();
            return ChatRoomResponseDto.builder()
                    .chatRoomId(room.getId())
                    .user1Id(room.getUser1Id())
                    .user2Id(room.getUser2Id())
                    .createdAt(room.getCreatedAt())
                    .build();
        }

        ChatRoom newRoom = ChatRoom.builder()
                .user1Id(user1)
                .user2Id(user2)
                .build();
        chatRoomRepository.save(newRoom);

        return ChatRoomResponseDto.builder()
                .chatRoomId(newRoom.getId())
                .user1Id(newRoom.getUser1Id())
                .user2Id(newRoom.getUser2Id())
                .createdAt(newRoom.getCreatedAt())
                .build();
    }


    public List<ChatRoomListResponseDto> getChatRoomsForUser(Integer userId) {
        List<ChatRoom> allRooms = chatRoomRepository.findByUser1IdOrUser2Id(userId, userId);

        return allRooms.stream().map(room -> {
                    Integer otherId = room.getUser1Id().equals(userId) ? room.getUser2Id() : room.getUser1Id();
                    Optional<User> otherUserOpt = userRepository.findById(otherId);

                    ChatMessage lastMsg = chatMessageRepository.findTopByChatRoomIdOrderBySentAtDesc(room.getId()).orElse(null);
                    long unreadCount = chatMessageRepository.countByChatRoomIdAndSenderIdNotAndIsReadFalse(room.getId(), userId);
                    return ChatRoomListResponseDto.builder()
                            .chatRoomId(room.getId())
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

    public List<ChatMessageResponseDto> getMessagesForRoom(Integer chatRoomId) {
        // DB에서 찾을때 asc를 하는 것과 데이터를 가져와서 sort 하는 방식중 어떤것이 더 좋을지 고민
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);

        return messages.stream().map(msg -> ChatMessageResponseDto.builder()
                .id(msg.getId())
                .chatRoomId(msg.getChatRoomId())
                .senderId(msg.getSenderId())
                .content(msg.getContent())
                .isRead(msg.getIsRead())
                .sentAt(msg.getSentAt())
                .build()
        ).collect(Collectors.toList());
    }

    // TeamService에서 사용함
    public void sendTeamRequestMessage(Integer senderId, Integer receiverId) {
        Integer user1 = Math.min(senderId, receiverId);
        Integer user2 = Math.max(senderId, receiverId);

        ChatRoom room = chatRoomRepository.findByUser1IdAndUser2Id(user1, user2)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        ChatMessage teamRequestMessage = ChatMessage.builder()
                .chatRoomId(room.getId())
                .senderId(senderId)
                .content("OO님이 팀 제안을 보냈습니다!")  // 프론트에서 적절히 파싱
                .isRead(false)
                .sentAt(new Timestamp(System.currentTimeMillis()))
                .messageType("TEAM_REQUEST")
                .build();

        chatMessageRepository.save(teamRequestMessage);
    }
    // TeamService에서 사용함
    public void sendTeamAcceptMessage(Integer senderId, Integer receiverId) {
        Integer user1 = Math.min(senderId, receiverId);
        Integer user2 = Math.max(senderId, receiverId);

        ChatRoom room = chatRoomRepository.findByUser1IdAndUser2Id(user1, user2)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        ChatMessage acceptMessage = ChatMessage.builder()
                .chatRoomId(room.getId())
                .senderId(receiverId)  // 수락한 사람(팀장)
                .content("OO님이 팀 제안을 수락했습니다!")  // 프론트에서 카드로 보여주기
                .isRead(false)
                .sentAt(new Timestamp(System.currentTimeMillis()))
                .messageType("TEAM_RESPONSE")
                .build();

        chatMessageRepository.save(acceptMessage);
    }

}
