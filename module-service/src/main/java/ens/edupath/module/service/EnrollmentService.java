package ens.edupath.module.service;

import ens.edupath.module.dto.EnrollmentRequest;
import ens.edupath.module.dto.EnrollmentResponse;
import ens.edupath.module.entity.Enrollment;
import ens.edupath.module.entity.EnrollmentPeriod;
import ens.edupath.module.entity.Module;
import ens.edupath.module.repository.EnrollmentPeriodRepository;
import ens.edupath.module.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentPeriodRepository enrollmentPeriodRepository;
    private final ModuleService moduleService;
    private final NotificationService notificationService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                           EnrollmentPeriodRepository enrollmentPeriodRepository,
                           ModuleService moduleService,
                           NotificationService notificationService) {
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentPeriodRepository = enrollmentPeriodRepository;
        this.moduleService = moduleService;
        this.notificationService = notificationService;
    }

    public EnrollmentResponse enrollStudent(Long moduleId, String studentId, String studentUsername, String studentEmail, boolean byAdmin) {
        Module module = moduleService.getModuleEntity(moduleId);

        // Vérifier si l'étudiant est déjà inscrit
        if (enrollmentRepository.existsByModuleIdAndStudentId(moduleId, studentId)) {
            throw new RuntimeException("L'étudiant est déjà inscrit à ce module");
        }

        // Si ce n'est pas un admin, vérifier que les inscriptions sont ouvertes
        if (!byAdmin) {
            EnrollmentPeriod period = enrollmentPeriodRepository.findByModuleId(moduleId).orElse(null);
            if (period == null || period.isEnrollmentClosed()) {
                throw new RuntimeException("Les inscriptions pour ce module sont fermées");
            }
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setModule(module);
        enrollment.setStudentId(studentId);
        enrollment.setStudentUsername(studentUsername);
        enrollment.setStudentEmail(studentEmail);
        
        // Si c'est un admin, approuver automatiquement, sinon en attente
        if (byAdmin) {
            enrollment.setStatus(Enrollment.EnrollmentStatus.APPROVED);
            enrollment.setApprovedAt(LocalDateTime.now());
        } else {
            enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING);
        }

        enrollment = enrollmentRepository.save(enrollment);

        // Envoyer une notification
        notificationService.sendEnrollmentNotification(enrollment, byAdmin);

        return toResponse(enrollment);
    }

    public EnrollmentResponse approveEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée avec l'ID: " + enrollmentId));

        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.PENDING) {
            throw new RuntimeException("L'inscription n'est pas en attente d'approbation");
        }

        enrollment.setStatus(Enrollment.EnrollmentStatus.APPROVED);
        enrollment.setApprovedAt(LocalDateTime.now());
        enrollment = enrollmentRepository.save(enrollment);

        notificationService.sendEnrollmentApprovalNotification(enrollment);

        return toResponse(enrollment);
    }

    public EnrollmentResponse rejectEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée avec l'ID: " + enrollmentId));

        enrollment.setStatus(Enrollment.EnrollmentStatus.REJECTED);
        enrollment = enrollmentRepository.save(enrollment);

        notificationService.sendEnrollmentRejectionNotification(enrollment);

        return toResponse(enrollment);
    }

    public EnrollmentResponse cancelEnrollment(Long enrollmentId, String studentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée avec l'ID: " + enrollmentId));

        // Vérifier que l'étudiant est bien le propriétaire de l'inscription
        if (!enrollment.getStudentId().equals(studentId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à annuler cette inscription");
        }

        enrollment.setStatus(Enrollment.EnrollmentStatus.CANCELLED);
        enrollment = enrollmentRepository.save(enrollment);

        notificationService.sendEnrollmentCancellationNotification(enrollment);

        return toResponse(enrollment);
    }

    public List<EnrollmentResponse> getEnrollmentsByStudent(String studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponse> getEnrollmentsByModule(Long moduleId) {
        return enrollmentRepository.findByModuleId(moduleId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponse> getPendingEnrollments(Long moduleId) {
        return enrollmentRepository.findByModuleIdAndStatus(moduleId, Enrollment.EnrollmentStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setModuleId(enrollment.getModule().getId());
        response.setModuleCode(enrollment.getModule().getCode());
        response.setModuleName(enrollment.getModule().getName());
        response.setStudentId(enrollment.getStudentId());
        response.setStudentUsername(enrollment.getStudentUsername());
        response.setStudentEmail(enrollment.getStudentEmail());
        response.setStatus(enrollment.getStatus().name());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setApprovedAt(enrollment.getApprovedAt());
        return response;
    }
}


