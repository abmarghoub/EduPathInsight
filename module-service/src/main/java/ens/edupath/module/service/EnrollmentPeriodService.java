package ens.edupath.module.service;

import ens.edupath.module.dto.EnrollmentPeriodRequest;
import ens.edupath.module.dto.EnrollmentPeriodResponse;
import ens.edupath.module.entity.EnrollmentPeriod;
import ens.edupath.module.entity.Module;
import ens.edupath.module.repository.EnrollmentPeriodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class EnrollmentPeriodService {

    private final EnrollmentPeriodRepository enrollmentPeriodRepository;
    private final ModuleService moduleService;

    public EnrollmentPeriodService(EnrollmentPeriodRepository enrollmentPeriodRepository,
                                 ModuleService moduleService) {
        this.enrollmentPeriodRepository = enrollmentPeriodRepository;
        this.moduleService = moduleService;
    }

    public EnrollmentPeriodResponse setEnrollmentPeriod(Long moduleId, EnrollmentPeriodRequest request) {
        Module module = moduleService.getModuleEntity(moduleId);

        // Vérifier que la date de fin est après la date de début
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("La date de fin doit être après la date de début");
        }

        EnrollmentPeriod period = enrollmentPeriodRepository.findByModuleId(moduleId).orElse(new EnrollmentPeriod());
        
        if (period.getId() == null) {
            period.setModule(module);
        }

        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());
        period.setActive(request.getActive() != null ? request.getActive() : true);

        period = enrollmentPeriodRepository.save(period);

        return toResponse(period);
    }

    public EnrollmentPeriodResponse getEnrollmentPeriod(Long moduleId) {
        EnrollmentPeriod period = enrollmentPeriodRepository.findByModuleId(moduleId)
                .orElseThrow(() -> new RuntimeException("Période d'inscription non trouvée pour le module: " + moduleId));
        return toResponse(period);
    }

    public void deleteEnrollmentPeriod(Long moduleId) {
        EnrollmentPeriod period = enrollmentPeriodRepository.findByModuleId(moduleId)
                .orElseThrow(() -> new RuntimeException("Période d'inscription non trouvée pour le module: " + moduleId));
        enrollmentPeriodRepository.delete(period);
    }

    private EnrollmentPeriodResponse toResponse(EnrollmentPeriod period) {
        EnrollmentPeriodResponse response = new EnrollmentPeriodResponse();
        response.setId(period.getId());
        response.setStartDate(period.getStartDate());
        response.setEndDate(period.getEndDate());
        response.setActive(period.getActive());
        response.setEnrollmentOpen(period.isEnrollmentOpen());
        return response;
    }
}


