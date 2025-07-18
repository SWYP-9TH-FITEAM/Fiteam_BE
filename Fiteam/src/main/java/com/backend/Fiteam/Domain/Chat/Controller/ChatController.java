package com.backend.Fiteam.Domain.Chat.Controller;

import com.backend.Fiteam.ConfigSecurity.JwtTokenProvider;
import com.backend.Fiteam.Domain.Chat.Dto.ChatMessageDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatMessageResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.CreateUserChatRoomRequestDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomListResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatRoomResponseDto;
import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage;
import com.backend.Fiteam.Domain.Chat.Repository.ChatMessageRepository;
import com.backend.Fiteam.Domain.Chat.Service.ChatService;
import com.backend.Fiteam.Domain.Chat.Service.SseEmitterService;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/v1/user-chat")
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
    private final SseEmitterService sseService;
    private final JwtTokenProvider jwtTokenProvider;


    // 1.채팅방 생성 (채팅신청하기)
    @Operation(summary = "채팅방 생성", description = "상대방과 채팅방을 생성합니다. 이미 존재하면 기존 방을 반환합니다.")
    @PostMapping("/room")
    public ResponseEntity<ChatRoomResponseDto> createChatRoom(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateUserChatRoomRequestDto dto) {
        Integer senderId = Integer.parseInt(userDetails.getUsername());
        ChatRoomResponseDto room = chatService.createChatRoom(senderId, dto);
        return ResponseEntity.ok(room);
    }

    // 2.채팅방 리스트 조회-대화 마지막 시간순서대로
    @Operation(summary = "2. 로그인한 사용자의 전체 채팅방 리스트 조회", description = "현재 유저가 속한 모든 채팅방을 최근 메시지 기준으로 정렬해서 반환합니다.")
    @GetMapping("/list")
    public ResponseEntity<List<ChatRoomListResponseDto>> getChatRooms(@AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = Integer.parseInt(userDetails.getUsername());

        List<ChatRoomListResponseDto> rooms = chatService.getChatRoomsForUser(userId, null);
        return ResponseEntity.ok(rooms);
    }

    @Operation(summary = "2-1.그룹별 채팅방 리스트 조회", description = "로그인한 사용자가 속한 그룹(groupId)에 해당하는 채팅방만 최근 메시지 기준으로 정렬해서 반환합니다.")
    @GetMapping("/group/{groupId}/list")
    public ResponseEntity<List<ChatRoomListResponseDto>> getChatRoomsByGroup(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer groupId) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        List<ChatRoomListResponseDto> rooms = chatService.getChatRoomsForUser(userId, groupId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<Page<ChatMessageResponseDto>> getMessagesForRoomPaged(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer roomId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Integer userId = Integer.parseInt(userDetails.getUsername());

        // 1. 권한 검사
        chatService.verifyUserInRoom(roomId, userId);

        // 2. 최신 메시지부터 조회 (sentAt DESC)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<ChatMessageResponseDto> messages = chatService.getMessagesForRoomPaged(roomId, pageable);
        return ResponseEntity.ok(messages);
    }

    // 4.채팅 메시지 전송	STOMP 방식
    @MessageMapping("/chat.sendMessage")
    @Operation(summary = "채팅 메시지 전송 및 실시간 알림")
    public void handleChatMessage(@Payload ChatMessageDto dto) {
        try {
            // 1. 유효성 검사
            if (dto.getChatRoomId() == null || dto.getSenderId() == null || dto.getContent() == null || dto.getSenderType() == null) {
                throw new IllegalArgumentException("필수 값 누락");
            }
            //Integer senderId = Integer.parseInt(userDetails.getUsername());
            // 서비스 레이어에서 메시지 저장, 채팅방 메타데이터(createdAt) 업데이트, SSE 델타 푸시
            ChatMessageResponseDto saved = chatService.sendMessage(dto);

            // WebSocket 구독자에게 메시지 브로드캐스트
            messagingTemplate.convertAndSend("/topic/chatroom." + saved.getChatRoomId(), saved);
        } catch (Exception e) {
            // 에러 로그
            System.err.println("채팅 메시지 전송 실패: " + e.getMessage());
            // 전송 실패 알림 (개인 큐)
            messagingTemplate.convertAndSend(
                    "/queue/errors/" + dto.getSenderId(),
                    "메시지 전송 실패: " + e.getMessage()
            );
        }
    }

    @GetMapping(value = "/rooms/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeChatRoomList(@AuthenticationPrincipal UserDetails userDetails) {
        // 1) 인증된 회원 정보가 없으면 401
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다");
        }
        // 2) UserDetails의 username 필드에 userId를 담아서 사용했다면 파싱
        Integer userId;
        try {
            userId = Integer.parseInt(userDetails.getUsername());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 사용자 정보");
        }
        // 3) SSE 구독 시작
        return sseService.subscribe(userId);
    }

    /*
    @GetMapping(
            value = "/rooms/subscribe/{groupId}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter subscribeChatRoomList(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId    // PathVariable 추가
    ) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다");
        }

        Integer userId;
        try {
            userId = Integer.parseInt(userDetails.getUsername());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 사용자 정보");
        }

        // 1) SSE 구독 시작, 그룹 필터도 함께 전달
        SseEmitter emitter = sseService.subscribe(userId);

        // 2) 초기 채팅방 리스트 조회 (groupId 파라미터 적용)
        List<ChatRoomListResponseDto> initialRooms =
                chatService.getChatRoomsForUser(userId, groupId);
        try {
            emitter.send(SseEmitter.event()
                    .name("chat-room-updated")
                    .data(initialRooms)
            );
        } catch (IOException ex) {
            System.out.printf("초기 채팅방 리스트 전송 실패: userId={}, groupId={}, error={}",
                    userId, groupId, ex.getMessage());
        }

        return emitter;
    }
     */

    // 5. 채팅 메시지 읽음 처리
    @Operation(summary = "채팅 메시지 읽음 처리", description = "로그인한 사용자가 지정된 채팅방(roomId)에 있는 자신의 읽지 않은 메시지를 모두 읽음 처리합니다.")
    @PatchMapping("/{roomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer roomId) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        chatMessageRepository.markAllAsRead(roomId, userId);
        return ResponseEntity.ok().build();
    }

    // 6. RoomId 가지고, 채팅방 정보 GET
    @Operation(summary = "6. RoomId 가지고, 채팅방 정보 GET", description = "roomId를 이용해 채팅방의 user1Id, user2Id, groupId, 생성 시각을 반환합니다.")
    @GetMapping("/{roomId}/data")
    public ResponseEntity<ChatRoomResponseDto> getChatRoomById(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer roomId) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        chatService.verifyUserInRoom(roomId, userId);

        ChatRoomResponseDto dto = chatService.getChatRoomInfo(userId,roomId);
        return ResponseEntity.ok(dto);
    }

    // 7. 채팅방 검색
    @Operation(summary = "7. 채팅방 검색-그룹 구분없이", description = "상대방 이름으로 채팅방을 검색합니다.")
    @GetMapping("/search/{name}")
    public List<ChatRoomListResponseDto> searchChatRooms(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String name) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        return chatService.searchChatRoomsForUser(userId, name);
    }

    @Operation(summary = "8. 채팅방 검색-그룹으로 구분해서", description = "상대방 이름으로 채팅방을 검색합니다.")
    @GetMapping("/search/{groupId}/{name}")
    public List<ChatRoomListResponseDto> searchChatRooms(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer groupId,@PathVariable String name) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        return chatService.searchChatRoomsForUser(userId, name);
    }

}
