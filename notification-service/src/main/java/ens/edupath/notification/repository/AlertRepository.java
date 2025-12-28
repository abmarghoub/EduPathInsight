package ens.edupath.notification.repository;

import ens.edupath.notification.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByRecipientId(String recipientId);
    List<Alert> findByRecipientIdAndAcknowledgedAtIsNull(String recipientId);
    List<Alert> findByAlertType(String alertType);
    List<Alert> findBySeverity(Alert.AlertSeverity severity);
}

