package ens.edupath.notification.repository;

import ens.edupath.notification.entity.Notification;
import ens.edupath.notification.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByTemplateKey(String templateKey);
    List<NotificationTemplate> findByNotificationTypeAndChannel(String notificationType, Notification.NotificationChannel channel);
    List<NotificationTemplate> findByActiveTrue();
}

