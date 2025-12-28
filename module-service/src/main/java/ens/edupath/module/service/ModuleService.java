package ens.edupath.module.service;

import ens.edupath.module.dto.ModuleRequest;
import ens.edupath.module.dto.ModuleResponse;
import ens.edupath.module.entity.Module;
import ens.edupath.module.repository.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ModuleService {

    private final ModuleRepository moduleRepository;

    public ModuleService(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    public ModuleResponse createModule(ModuleRequest request) {
        if (moduleRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Un module avec le code " + request.getCode() + " existe déjà");
        }

        Module module = new Module();
        module.setCode(request.getCode());
        module.setName(request.getName());
        module.setDescription(request.getDescription());
        module.setCredits(request.getCredits());
        module.setActive(request.getActive() != null ? request.getActive() : true);

        module = moduleRepository.save(module);
        return toResponse(module);
    }

    public ModuleResponse updateModule(Long id, ModuleRequest request) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec l'ID: " + id));

        // Vérifier que le code n'est pas déjà utilisé par un autre module
        if (!module.getCode().equals(request.getCode()) && moduleRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Un module avec le code " + request.getCode() + " existe déjà");
        }

        module.setCode(request.getCode());
        module.setName(request.getName());
        module.setDescription(request.getDescription());
        module.setCredits(request.getCredits());
        if (request.getActive() != null) {
            module.setActive(request.getActive());
        }

        module = moduleRepository.save(module);
        return toResponse(module);
    }

    public ModuleResponse getModuleById(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec l'ID: " + id));
        return toResponse(module);
    }

    public ModuleResponse getModuleByCode(String code) {
        Module module = moduleRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec le code: " + code));
        return toResponse(module);
    }

    public List<ModuleResponse> getAllModules() {
        return moduleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ModuleResponse> getActiveModules() {
        return moduleRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteModule(Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new RuntimeException("Module non trouvé avec l'ID: " + id);
        }
        moduleRepository.deleteById(id);
    }

    private ModuleResponse toResponse(Module module) {
        ModuleResponse response = new ModuleResponse();
        response.setId(module.getId());
        response.setCode(module.getCode());
        response.setName(module.getName());
        response.setDescription(module.getDescription());
        response.setCredits(module.getCredits());
        response.setActive(module.getActive());
        response.setCreatedAt(module.getCreatedAt());
        response.setUpdatedAt(module.getUpdatedAt());

        if (module.getEnrollmentPeriod() != null) {
            ens.edupath.module.dto.EnrollmentPeriodResponse periodResponse = new ens.edupath.module.dto.EnrollmentPeriodResponse();
            periodResponse.setId(module.getEnrollmentPeriod().getId());
            periodResponse.setStartDate(module.getEnrollmentPeriod().getStartDate());
            periodResponse.setEndDate(module.getEnrollmentPeriod().getEndDate());
            periodResponse.setActive(module.getEnrollmentPeriod().getActive());
            periodResponse.setEnrollmentOpen(module.getEnrollmentPeriod().isEnrollmentOpen());
            response.setEnrollmentPeriod(periodResponse);
        }

        return response;
    }

    public Module getModuleEntity(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec l'ID: " + id));
    }
}


