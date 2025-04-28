package com.backend.Fiteam.Domain.Notification.Repository;

import com.backend.Fiteam.Domain.Notification.Entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserId(Integer userId);
}
