package com.backend.Fiteam.Domain.Chat.Repository;

import com.backend.Fiteam.Domain.Chat.Entity.ChatMessage;
import com.backend.Fiteam.Domain.Chat.Entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    Optional<ChatRoom> findByUser1IdAndUser2Id(Integer user1Id, Integer user2Id);

    List<ChatRoom> findByUser1IdOrUser2Id(Integer user1Id, Integer user2Id);


    Optional<ChatRoom> findByUser1IdAndUser2IdAndGroupId(Integer user1Id, Integer user2Id, Integer groupId);
}
