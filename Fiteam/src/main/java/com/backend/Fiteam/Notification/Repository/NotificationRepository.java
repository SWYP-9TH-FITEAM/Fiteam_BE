package com.backend.Fiteam.Notification.Repository;

import com.backend.Fiteam.Notification.Entity.Notification;
import com.backend.Fiteam.User.Dto.UserNotifyDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserId(Integer userId);
}
