package ens.edupath.notification.repository;

import ens.edupath.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientId(String recipientId);
    List<Notification> findByRecipientIdAndStatus(String recipientId, Notification.NotificationStatus status);
    List<Notification> findByType(String type);
    List<Notification> findByChannel(Notification.NotificationChannel channel);
}


