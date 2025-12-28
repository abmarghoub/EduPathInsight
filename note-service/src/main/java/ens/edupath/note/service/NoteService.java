package ens.edupath.note.service;

import ens.edupath.note.client.ModuleServiceClient;
import ens.edupath.note.dto.NoteRequest;
import ens.edupath.note.dto.NoteResponse;
import ens.edupath.note.entity.Note;
import ens.edupath.note.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final KPIService kpiService;
    private final AlertService alertService;
    private final ModuleServiceClient moduleServiceClient;

    @Value("${kpi.auto-calculate:true}")
    private boolean autoCalculateKPI;

    public NoteService(NoteRepository noteRepository, KPIService kpiService, AlertService alertService,
                      ModuleServiceClient moduleServiceClient) {
        this.noteRepository = noteRepository;
        this.kpiService = kpiService;
        this.alertService = alertService;
        this.moduleServiceClient = moduleServiceClient;
    }

    public NoteResponse createNote(NoteRequest request) {
        // Récupérer les informations du module depuis module-service
        Map<String, Object> moduleInfo = moduleServiceClient.getModuleById(request.getModuleId());
        String moduleCode = (String) moduleInfo.get("code");
        String moduleName = (String) moduleInfo.get("name");
        
        // Pour studentUsername, on peut le récupérer depuis auth-service ou l'utiliser depuis request si disponible
        // Pour l'instant, on utilise un placeholder ou on le récupère depuis un service utilisateur
        String studentUsername = request.getStudentId(); // Par défaut, utiliser studentId
        
        Note note = new Note();
        note.setStudentId(request.getStudentId());
        note.setStudentUsername(studentUsername);
        note.setModuleId(request.getModuleId());
        note.setModuleCode(moduleCode);
        note.setModuleName(moduleName);
        note.setEvaluationType(request.getEvaluationType());
        note.setEvaluationTitle(request.getEvaluationTitle());
        note.setScore(request.getScore());
        note.setMaxScore(request.getMaxScore());
        note.setComments(request.getComments());
        note.setEvaluationDate(request.getEvaluationDate());

        note = noteRepository.save(note);

        // Calculer les KPIs et vérifier les alertes si activé
        if (autoCalculateKPI) {
            kpiService.calculateAndUpdateKPI(request.getStudentId(), request.getModuleId());
            alertService.checkAndCreateAlerts(request.getStudentId(), request.getModuleId());
        }

        return toResponse(note);
    }

    public NoteResponse updateNote(Long id, NoteRequest request) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note non trouvée avec l'ID: " + id));

        // Récupérer les informations du module depuis module-service
        Map<String, Object> moduleInfo = moduleServiceClient.getModuleById(request.getModuleId());
        String moduleCode = (String) moduleInfo.get("code");
        String moduleName = (String) moduleInfo.get("name");
        
        String studentUsername = request.getStudentId(); // Par défaut, utiliser studentId

        note.setStudentId(request.getStudentId());
        note.setStudentUsername(studentUsername);
        note.setModuleId(request.getModuleId());
        note.setModuleCode(moduleCode);
        note.setModuleName(moduleName);
        note.setEvaluationType(request.getEvaluationType());
        note.setEvaluationTitle(request.getEvaluationTitle());
        note.setScore(request.getScore());
        note.setMaxScore(request.getMaxScore());
        note.setComments(request.getComments());
        note.setEvaluationDate(request.getEvaluationDate());

        note = noteRepository.save(note);

        // Recalculer les KPIs et vérifier les alertes
        if (autoCalculateKPI) {
            kpiService.calculateAndUpdateKPI(request.getStudentId(), request.getModuleId());
            alertService.checkAndCreateAlerts(request.getStudentId(), request.getModuleId());
        }

        return toResponse(note);
    }

    public NoteResponse getNoteById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note non trouvée avec l'ID: " + id));
        return toResponse(note);
    }

    public List<NoteResponse> getNotesByStudent(String studentId) {
        return noteRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<NoteResponse> getNotesByModule(Long moduleId) {
        return noteRepository.findByModuleId(moduleId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<NoteResponse> getNotesByStudentAndModule(String studentId, Long moduleId) {
        return noteRepository.findByStudentIdAndModuleId(studentId, moduleId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteNote(Long id) {
        if (!noteRepository.existsById(id)) {
            throw new RuntimeException("Note non trouvée avec l'ID: " + id);
        }
        noteRepository.deleteById(id);
    }

    public List<NoteResponse> getAllNotes() {
        return noteRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private NoteResponse toResponse(Note note) {
        NoteResponse response = new NoteResponse();
        response.setId(note.getId());
        response.setStudentId(note.getStudentId());
        response.setStudentUsername(note.getStudentUsername());
        response.setModuleId(note.getModuleId());
        response.setModuleCode(note.getModuleCode());
        response.setModuleName(note.getModuleName());
        response.setEvaluationType(note.getEvaluationType());
        response.setEvaluationTitle(note.getEvaluationTitle());
        response.setScore(note.getScore());
        response.setMaxScore(note.getMaxScore());
        response.setPercentage(note.getPercentage());
        response.setPassing(note.isPassing());
        response.setComments(note.getComments());
        response.setEvaluationDate(note.getEvaluationDate());
        response.setCreatedAt(note.getCreatedAt());
        response.setUpdatedAt(note.getUpdatedAt());
        return response;
    }
}

