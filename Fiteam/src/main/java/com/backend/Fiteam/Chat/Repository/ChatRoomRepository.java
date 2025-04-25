package com.backend.Fiteam.Chat.Repository;

import com.backend.Fiteam.Chat.Entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
}
