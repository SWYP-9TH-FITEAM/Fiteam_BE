package com.backend.Fiteam.Domain.Chat.Controller;

import com.backend.Fiteam.Domain.Chat.Dto.ChatMessageDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatMessageResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomCreateRequestDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomListResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.UserPresenceDto;
import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage;
import com.backend.Fiteam.Domain.Chat.Repository.ChatMessageRepository;
import com.backend.Fiteam.Domain.Chat.Service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Lazy;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
@Tag(name = "9. ChatController - 1대1 채팅")
public class ChatController {

    /*
    1.채팅방 생성 (채팅신청하기)
    2.채팅방 리스트 조회-대화 마지막 시간순서대로..?
    3.채팅방 메시지 조회
    4.채팅 메시지 전송	POST
    5. 채팅 메시지 읽음 처리
    */

    private final ChatService chatService;
    private final ChatMessageRepository chatMessageRepository;
    private final @Lazy SimpMessagingTemplate messagingTemplate;
    //private final PresenceRegistry presenceRegistry;


    // 1.채팅방 생성 (채팅신청하기)
    @Operation(summary = "채팅방 생성", description = "상대방과 채팅방을 생성합니다. 이미 존재하면 기존 방을 반환합니다.")
    @PostMapping("/room")
    public ResponseEntity<ChatRoomResponseDto> createChatRoom(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody ChatRoomCreateRequestDto dto) {
        try {
            Integer senderId = Integer.parseInt(userDetails.getUsername());
            ChatRoomResponseDto room = chatService.createChatRoom(senderId, dto);
            return ResponseEntity.ok(room);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2.채팅방 리스트 조회-대화 마지막 시간순서대로
    @Operation(summary = "로그인한 사용자의 채팅방 리스트 조회", description = "현재 유저가 속한 모든 채팅방을 최근 메시지 기준으로 정렬해서 반환합니다.")
    @GetMapping("/list")
    public ResponseEntity<List<ChatRoomListResponseDto>> getChatRooms(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());

            List<ChatRoomListResponseDto> rooms = chatService.getChatRoomsForUser(userId);
            return ResponseEntity.ok(rooms);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<Page<ChatMessageResponseDto>> getMessagesForRoomPaged(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer roomId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());

            // 1. 권한 검사
            chatService.verifyUserInRoom(roomId, userId);

            // 2. 최신 메시지부터 조회 (sentAt DESC)
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
            Page<ChatMessageResponseDto> messages = chatService.getMessagesForRoomPaged(roomId, pageable);
            return ResponseEntity.ok(messages);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4.채팅 메시지 전송	STOMP 방식
    @MessageMapping("/chat.sendMessage")
    public void handleChatMessage(ChatMessageDto dto) {
        try {
            // 1. 유효성 검사
            if (dto.getChatRoomId() == null || dto.getSenderId() == null || dto.getContent() == null) {
                throw new IllegalArgumentException("필수 값 누락");
            }

            // 2. 메시지 생성 및 저장
            ChatMessage message = ChatMessage.builder()
                    .chatRoomId(dto.getChatRoomId())
                    .senderId(dto.getSenderId())
                    .messageType(dto.getMessageType() != null ? dto.getMessageType() : "TEXT")
                    .content(dto.getContent())
                    .isRead(false)
                    .sentAt(new Timestamp(System.currentTimeMillis()))
                    .build();

            ChatMessage saved = chatMessageRepository.save(message);

            // 3. 메시지 브로드캐스트
            messagingTemplate.convertAndSend("/topic/chatroom." + dto.getChatRoomId(), saved);

        } catch (Exception e) {
            // 4. 에러 로그
            System.err.println("채팅 메시지 전송 실패: " + e.getMessage());
            // 5. 실패 응답 (해당 유저의 개인 큐로)
            messagingTemplate.convertAndSend("/queue/errors/" + dto.getSenderId(), "메시지 전송 실패: " + e.getMessage());
        }
    }


    // 5. 채팅 메시지 읽음 처리
    @Operation(summary = "채팅 메시지 읽음 처리", description = "로그인한 사용자가 지정된 채팅방(roomId)에 있는 자신의 읽지 않은 메시지를 모두 읽음 처리합니다.")
    @PatchMapping("/{roomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer roomId) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            chatMessageRepository.markAllAsRead(roomId, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
