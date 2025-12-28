package ens.edupath.notification.controller;

import ens.edupath.notification.dto.AlertRequest;
import ens.edupath.notification.entity.Alert;
import ens.edupath.notification.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    public ResponseEntity<Alert> createAlert(@Valid @RequestBody AlertRequest request) {
        try {
            Alert.AlertSeverity severity = Alert.AlertSeverity.valueOf(request.getSeverity());
            Alert alert = alertService.createAlert(
                    request.getRecipientId(),
                    request.getAlertType(),
                    request.getTitle(),
                    request.getMessage(),
                    severity,
                    request.getAlertData()
            );
            return ResponseEntity.ok(alert);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<List<Alert>> getAlertsByRecipient(@PathVariable String recipientId) {
        return ResponseEntity.ok(alertService.getAlertsByRecipient(recipientId));
    }

    @GetMapping("/recipient/{recipientId}/unacknowledged")
    public ResponseEntity<List<Alert>> getUnacknowledgedAlerts(@PathVariable String recipientId) {
        return ResponseEntity.ok(alertService.getUnacknowledgedAlerts(recipientId));
    }

    @PostMapping("/{alertId}/acknowledge")
    public ResponseEntity<Alert> acknowledgeAlert(@PathVariable Long alertId) {
        try {
            Alert alert = alertService.acknowledgeAlert(alertId);
            return ResponseEntity.ok(alert);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{alertId}/resolve")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long alertId) {
        try {
            Alert alert = alertService.resolveAlert(alertId);
            return ResponseEntity.ok(alert);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}


