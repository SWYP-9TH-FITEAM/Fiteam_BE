package com.backend.Fiteam.Domain.Chat.Repository;

import com.backend.Fiteam.Domain.Chat.Entity.ManagerChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerChatRoomRepository extends JpaRepository<ManagerChatRoom, Integer> {
    Optional<ManagerChatRoom> findByManagerIdAndUserIdAndGroupId(Integer managerId, Integer userId, Integer groupId);
    List<ManagerChatRoom> findAllByManagerId(Integer managerId);
    List<ManagerChatRoom> findAllByUserId(Integer userId);

    Optional<ManagerChatRoom> findByManagerIdAndUserId(Integer managerId, Integer userId);
}
