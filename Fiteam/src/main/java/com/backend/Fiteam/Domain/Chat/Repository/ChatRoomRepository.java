package com.backend.Fiteam.Domain.Chat.Repository;

import com.backend.Fiteam.Domain.Chat.Entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
}
