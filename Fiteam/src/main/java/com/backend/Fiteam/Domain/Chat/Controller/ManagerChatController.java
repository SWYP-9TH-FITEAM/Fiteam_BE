package com.backend.Fiteam.Domain.Chat.Controller;

import com.backend.Fiteam.Domain.Chat.Dto.CreateManagerChatRoomRequestDto;
import com.backend.Fiteam.Domain.Chat.Dto.ManagerChatRoomListResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.ManagerChatRoomResponseDto;
import com.backend.Fiteam.Domain.Chat.Dto.ManagerChatMessageDto;
import com.backend.Fiteam.Domain.Chat.Dto.ChatMessageResponseDto;
import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage;
import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage.SenderType;
import com.backend.Fiteam.Domain.Chat.Service.ManagerChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/manager-chat")
@RequiredArgsConstructor
@Tag(name = "ManagerChatController - 매니저↔유저 채팅")
public class ManagerChatController {
    private final ManagerChatService managerChatService;
    private final @Lazy SimpMessagingTemplate messagingTemplate;

    @Operation(summary = "Manager-User 채팅방 생성",
            description = "매니저와 유저 간 1:1 채팅방을 생성하거나 기존 방을 반환합니다.\n" +
                    "- 매니저로 로그인 시 body에 userId, groupId만 담아 호출\n" +
                    "- 유저로 로그인 시 body에 managerId, groupId만 담아 호출")
    @PostMapping("/room")
    public ManagerChatRoomResponseDto createChatRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateManagerChatRoomRequestDto dto
    ) {
        Integer principalId = Integer.parseInt(userDetails.getUsername());
        boolean isManager = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        if (isManager) {
            dto.setManagerId(principalId);
            if (dto.getUserId() == null) {
                throw new IllegalArgumentException("매니저 방 생성 시 userId를 포함해야 합니다.");
            }
        } else {
            dto.setUserId(principalId);
            if (dto.getManagerId() == null) {
                throw new IllegalArgumentException("유저 방 생성 시 managerId를 포함해야 합니다.");
            }
        }

        return managerChatService.createRoom(dto);
    }

    @Operation(summary = "매니저↔유저 채팅방 목록 조회",
            description = "매니저면 자신이 관리하는 방, 유저면 자신이 참여한 방을 반환합니다.")
    @GetMapping("/list")
    public List<ManagerChatRoomListResponseDto> getRooms(@AuthenticationPrincipal UserDetails userDetails) {
        Integer principalId = Integer.parseInt(userDetails.getUsername());
        boolean isManager = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        return isManager
                ? managerChatService.getRoomsByManager(principalId)
                : managerChatService.getRoomsByUser(principalId);
    }

    @Operation(summary = "메시지 조회", description = "채팅방 메시지를 최신순으로 페이징 조회합니다.")
    @GetMapping("/{roomId}/messages")
    public Page<ChatMessageResponseDto> getMessagesForRoomPaged(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Integer principalId = Integer.parseInt(userDetails.getUsername());
        // 매니저든 유저든 방 참여 여부만 확인
        managerChatService.verifyManagerInRoom(roomId, principalId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        return managerChatService.getMessagesForRoomPaged(roomId, pageable);
    }

    @MessageMapping("/manager.chat.sendMessage")
    public void handleManagerChatMessage(ManagerChatMessageDto dto) {
        ChatMessage saved = managerChatService.saveMessage(dto);
        String topic = (saved.getSenderType() == SenderType.USER
                ? "/topic/manager/"
                : "/topic/user/") + saved.getChatRoomId();
        messagingTemplate.convertAndSend(topic, saved);
    }

    @Operation(summary = "읽음 처리", description = "안 읽은 메시지를 모두 읽음 처리합니다.")
    @PatchMapping("/{roomId}/read")
    public void markMessagesAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer roomId
    ) {
        Integer principalId = Integer.parseInt(userDetails.getUsername());
        managerChatService.markAllAsRead(roomId, principalId);
    }

    @Operation(summary = "채팅방 정보 조회", description = "채팅방의 매니저 ID, 유저 ID, 생성 일시 등을 반환합니다.")
    @GetMapping("/{roomId}/data")
    public ManagerChatRoomResponseDto getChatRoomById(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer roomId) {
        Integer principalId = Integer.parseInt(userDetails.getUsername());
        boolean isManager = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        // principalId, isManager 정보를 넘겨서 DTO 생성
        return managerChatService.getChatRoomInfo(roomId, principalId, isManager);
    }
}
