package com.backend.Fiteam.Chat.Repository;

import com.backend.Fiteam.Chat.Entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
}
