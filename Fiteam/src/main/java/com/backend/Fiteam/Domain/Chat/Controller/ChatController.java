package com.backend.Fiteam.Domain.Chat.Controller;

import com.backend.Fiteam.Domain.Chat.Dto.ChatMessageDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatMessageResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomCreateRequestDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomListResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomResponseDto;
import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage;
import com.backend.Fiteam.Domain.Chat.Repository.ChatMessageRepository;
import com.backend.Fiteam.Domain.Chat.Service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    /*
    1.채팅방 생성 (채팅신청하기)
    2.채팅방 리스트 조회-대화 마지막 시간순서대로..?
    3.채팅방 메시지 조회
    4.채팅 메시지 전송	POST
    */

    private final ChatService chatRoomService;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 1.채팅방 생성 (채팅신청하기)
    @Operation(summary = "채팅방 생성", description = "상대방과 채팅방을 생성합니다. 이미 존재하면 기존 방을 반환합니다.")
    @PostMapping("/room")
    public ResponseEntity<ChatRoomResponseDto> createChatRoom(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody ChatRoomCreateRequestDto dto) {
        try {
            Integer senderId = Integer.parseInt(userDetails.getUsername());
            ChatRoomResponseDto room = chatRoomService.createChatRoom(senderId, dto);
            return ResponseEntity.ok(room);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2.채팅방 리스트 조회-대화 마지막 시간순서대로..?
    @Operation(summary = "로그인한 사용자의 채팅방 리스트 조회", description = "현재 유저가 속한 모든 채팅방을 최근 메시지 기준으로 정렬해서 반환합니다.")
    @GetMapping("/list")
    public ResponseEntity<List<ChatRoomListResponseDto>> getChatRooms(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            List<ChatRoomListResponseDto> rooms = chatRoomService.getChatRoomsForUser(userId);
            return ResponseEntity.ok(rooms);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 3.채팅방 메시지 조회
    @Operation(summary = "채팅방 메시지 조회", description = "채팅방 ID를 기반으로 메시지를 시간순으로 조회합니다.")
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getMessagesForRoom(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer roomId) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());

            // roomId 접근 권한 체크를 수행하려면 여기다 로직 추가

            List<ChatMessageResponseDto> messages = chatRoomService.getMessagesForRoom(roomId);
            return ResponseEntity.ok(messages);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4.채팅 메시지 전송	STOMP 방식
    @MessageMapping("/chat.sendMessage")
    public void handleChatMessage(ChatMessageDto dto) {

        ChatMessage message = ChatMessage.builder()
                .chatRoomId(dto.getChatRoomId())
                .senderId(dto.getSenderId())
                .content(dto.getContent())
                .isRead(false)
                .sentAt(new Timestamp(System.currentTimeMillis()))
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // 구독자에게 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chatroom." + dto.getChatRoomId(), saved);
    }
}
