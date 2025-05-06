package com.backend.Fiteam.Domain.Chat.Repository;

import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    Optional<ChatMessage> findTopByChatRoomIdOrderBySentAtDesc(Integer chatRoomId);
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Integer chatRoomId);

    long countByChatRoomIdAndSenderIdNotAndIsReadFalse(Integer chatRoomId, Integer userId);

}
