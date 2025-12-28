package ens.edupath.note.service;

import ens.edupath.note.entity.Alert;
import ens.edupath.note.entity.KPI;
import ens.edupath.note.entity.Note;
import ens.edupath.note.repository.AlertRepository;
import ens.edupath.note.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;
    private final NoteRepository noteRepository;
    private final KPIService kpiService;
    private final AlertNotificationService alertNotificationService;

    @Value("${alerts.low-grade-threshold:10.0}")
    private Double lowGradeThreshold;

    @Value("${alerts.average-grade-threshold:12.0}")
    private Double averageGradeThreshold;

    @Value("${alerts.failing-grade-threshold:10.0}")
    private Double failingGradeThreshold;

    @Autowired
    public AlertService(AlertRepository alertRepository,
                       NoteRepository noteRepository,
                       KPIService kpiService,
                       AlertNotificationService alertNotificationService) {
        this.alertRepository = alertRepository;
        this.noteRepository = noteRepository;
        this.kpiService = kpiService;
        this.alertNotificationService = alertNotificationService;
    }

    public void checkAndCreateAlerts(String studentId, Long moduleId) {
        List<Note> notes = noteRepository.findByStudentIdAndModuleId(studentId, moduleId);
        KPI kpi = kpiService.getKPI(studentId, moduleId);

        if (notes.isEmpty()) {
            return;
        }

        // Vérifier les notes récentes pour des alertes de notes basses
        for (Note note : notes) {
            double percentage = note.getPercentage();
            
            // Alerte pour note échouée (< 10/20)
            if (percentage < 50.0) { // 50% = 10/20
                createOrUpdateAlert(studentId, moduleId, Alert.AlertType.FAILING_GRADE,
                        "Note échouée",
                        String.format("La note %s (%.2f%%) est en dessous de la note de passage (50%%)", 
                                note.getEvaluationTitle(), percentage),
                        failingGradeThreshold, percentage);
            }
            
            // Alerte pour note très basse
            if (percentage < lowGradeThreshold) {
                createOrUpdateAlert(studentId, moduleId, Alert.AlertType.LOW_GRADE,
                        "Note très basse",
                        String.format("La note %s (%.2f%%) est très basse (seuil: %.2f%%)", 
                                note.getEvaluationTitle(), percentage, lowGradeThreshold),
                        lowGradeThreshold, percentage);
            }
        }

        // Vérifier la moyenne pour alerte de moyenne faible
        if (kpi != null && kpi.getAverageScore() < averageGradeThreshold) {
            createOrUpdateAlert(studentId, moduleId, Alert.AlertType.LOW_AVERAGE,
                    "Moyenne faible",
                    String.format("La moyenne (%.2f%%) est en dessous du seuil (%.2f%%)", 
                            kpi.getAverageScore(), averageGradeThreshold),
                    averageGradeThreshold, kpi.getAverageScore());
        }

        // Vérifier plusieurs échecs
        if (kpi != null && kpi.getFailingCount() >= 3) {
            createOrUpdateAlert(studentId, moduleId, Alert.AlertType.MULTIPLE_FAILURES,
                    "Plusieurs échecs",
                    String.format("L'étudiant a %d notes échouées dans ce module", kpi.getFailingCount()),
                    (double) kpi.getFailingCount(), (double) kpi.getFailingCount());
        }
    }

    private void createOrUpdateAlert(String studentId, Long moduleId, Alert.AlertType type,
                                    String title, String message, Double thresholdValue, Double actualValue) {
        // Vérifier si une alerte du même type existe déjà et est active
        List<Alert> existingAlerts = alertRepository.findByStudentIdAndModuleId(studentId, moduleId);
        Alert existingAlert = existingAlerts.stream()
                .filter(a -> a.getType() == type && a.getStatus() == Alert.AlertStatus.ACTIVE)
                .findFirst()
                .orElse(null);

        if (existingAlert != null) {
            // Mettre à jour l'alerte existante
            existingAlert.setMessage(message);
            existingAlert.setActualValue(actualValue);
            existingAlert.setThresholdValue(thresholdValue);
            alertRepository.save(existingAlert);
        } else {
            // Créer une nouvelle alerte
            Alert alert = new Alert();
            alert.setStudentId(studentId);
            alert.setModuleId(moduleId);
            alert.setType(type);
            alert.setTitle(title);
            alert.setMessage(message);
            alert.setStatus(Alert.AlertStatus.ACTIVE);
            alert.setThresholdValue(thresholdValue);
            alert.setActualValue(actualValue);
            alert.setCreatedAt(LocalDateTime.now());

            alert = alertRepository.save(alert);

            // Envoyer une notification
            alertNotificationService.sendAlertNotification(alert);
        }
    }

    public List<Alert> getAlertsByStudent(String studentId) {
        return alertRepository.findByStudentId(studentId);
    }

    public List<Alert> getActiveAlertsByStudent(String studentId) {
        return alertRepository.findByStudentId(studentId).stream()
                .filter(a -> a.getStatus() == Alert.AlertStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    public List<Alert> getAlertsByModule(Long moduleId) {
        return alertRepository.findByModuleId(moduleId);
    }

    public Alert acknowledgeAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée avec l'ID: " + alertId));

        alert.setStatus(Alert.AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(LocalDateTime.now());
        
        return alertRepository.save(alert);
    }

    public Alert resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée avec l'ID: " + alertId));

        alert.setStatus(Alert.AlertStatus.RESOLVED);
        
        return alertRepository.save(alert);
    }
}

