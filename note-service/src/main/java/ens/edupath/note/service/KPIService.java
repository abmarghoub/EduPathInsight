package ens.edupath.note.service;

import ens.edupath.note.entity.KPI;
import ens.edupath.note.entity.Note;
import ens.edupath.note.repository.KPIRepository;
import ens.edupath.note.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class KPIService {

    private final KPIRepository kpiRepository;
    private final NoteRepository noteRepository;

    public KPIService(KPIRepository kpiRepository, NoteRepository noteRepository) {
        this.kpiRepository = kpiRepository;
        this.noteRepository = noteRepository;
    }

    public KPI calculateAndUpdateKPI(String studentId, Long moduleId) {
        List<Note> notes = noteRepository.findByStudentIdAndModuleId(studentId, moduleId);
        
        if (notes.isEmpty()) {
            // Supprimer le KPI s'il n'y a plus de notes
            kpiRepository.findByStudentIdAndModuleId(studentId, moduleId)
                    .ifPresent(kpiRepository::delete);
            return null;
        }

        // Calculer les métriques
        double totalScore = notes.stream().mapToDouble(Note::getScore).sum();
        double totalMaxScore = notes.stream().mapToDouble(Note::getMaxScore).sum();
        double averageScore = totalMaxScore > 0 ? (totalScore / totalMaxScore) * 100 : 0.0;
        
        long passingCount = notes.stream().filter(Note::isPassing).count();
        long failingCount = notes.size() - passingCount;
        
        double highestScore = notes.stream()
                .mapToDouble(n -> n.getPercentage())
                .max()
                .orElse(0.0);
        
        double lowestScore = notes.stream()
                .mapToDouble(n -> n.getPercentage())
                .min()
                .orElse(0.0);

        // Créer ou mettre à jour le KPI
        KPI kpi = kpiRepository.findByStudentIdAndModuleId(studentId, moduleId)
                .orElse(new KPI());
        
        kpi.setStudentId(studentId);
        kpi.setModuleId(moduleId);
        kpi.setAverageScore(averageScore);
        kpi.setTotalScore(totalScore);
        kpi.setTotalMaxScore(totalMaxScore);
        kpi.setNumberOfEvaluations(notes.size());
        kpi.setPassingCount((int) passingCount);
        kpi.setFailingCount((int) failingCount);
        kpi.setHighestScore(highestScore);
        kpi.setLowestScore(lowestScore);
        kpi.setLastCalculatedAt(LocalDateTime.now());
        
        if (kpi.getId() == null) {
            kpi.setCreatedAt(LocalDateTime.now());
        }

        return kpiRepository.save(kpi);
    }

    public KPI getKPI(String studentId, Long moduleId) {
        return kpiRepository.findByStudentIdAndModuleId(studentId, moduleId)
                .orElse(null);
    }

    public List<KPI> getKPIsByStudent(String studentId) {
        return kpiRepository.findByStudentId(studentId);
    }

    public List<KPI> getKPIsByModule(Long moduleId) {
        return kpiRepository.findByModuleId(moduleId);
    }
}


