package com.notification.repository;

import com.notification.model.Notification;
import com.notification.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientId(String recipientId);

    List<Notification> findByRecipientIdAndStatus(String recipientId, NotificationStatus status);
}
