package com.backend.Fiteam.Notification.Repository;

import com.backend.Fiteam.Notification.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
}
