package com.backend.Fiteam.Domain.Chat.Repository;

import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    Optional<ChatMessage> findTopByChatRoomIdOrderBySentAtDesc(Integer chatRoomId);
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Integer chatRoomId);

    long countByChatRoomIdAndSenderIdNotAndIsReadFalse(Integer chatRoomId, Integer userId);

    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.chatRoomId = :chatRoomId AND m.senderId <> :userId AND m.isRead = false")
    void markAllAsRead(@Param("chatRoomId") Integer chatRoomId, @Param("userId") Integer userId);

    Page<ChatMessage> findByChatRoomIdOrderBySentAtDesc(Integer chatRoomId, Pageable pageable);
}
