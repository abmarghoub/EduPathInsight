package ens.edupath.ingestion.service;

import ens.edupath.ingestion.model.neo4j.Activity;
import ens.edupath.ingestion.model.neo4j.Evaluation;
import ens.edupath.ingestion.model.neo4j.Module;
import ens.edupath.ingestion.model.neo4j.Student;
import ens.edupath.ingestion.repository.neo4j.ActivityRepository;
import ens.edupath.ingestion.repository.neo4j.EvaluationRepository;
import ens.edupath.ingestion.repository.neo4j.ModuleRepository;
import ens.edupath.ingestion.repository.neo4j.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class GraphService {

    private final StudentRepository studentRepository;
    private final ModuleRepository moduleRepository;
    private final EvaluationRepository evaluationRepository;
    private final ActivityRepository activityRepository;

    public GraphService(StudentRepository studentRepository,
                       ModuleRepository moduleRepository,
                       EvaluationRepository evaluationRepository,
                       ActivityRepository activityRepository) {
        this.studentRepository = studentRepository;
        this.moduleRepository = moduleRepository;
        this.evaluationRepository = evaluationRepository;
        this.activityRepository = activityRepository;
    }

    public Student createOrUpdateStudent(Map<String, String> data) {
        String studentId = data.getOrDefault("student_id", data.getOrDefault("id", UUID.randomUUID().toString()));
        
        Student student = studentRepository.findByStudentId(studentId)
                .orElse(new Student());
        
        student.setStudentId(studentId);
        student.setUsername(data.getOrDefault("username", student.getUsername()));
        student.setEmail(data.getOrDefault("email", student.getEmail()));
        student.setFirstName(data.getOrDefault("first_name", data.getOrDefault("firstname", student.getFirstName())));
        student.setLastName(data.getOrDefault("last_name", data.getOrDefault("lastname", student.getLastName())));
        
        return studentRepository.save(student);
    }

    public Module createOrUpdateModule(Map<String, String> data) {
        String moduleId = data.getOrDefault("module_id", data.getOrDefault("id", UUID.randomUUID().toString()));
        
        Module module = moduleRepository.findByModuleId(moduleId)
                .orElse(new Module());
        
        module.setModuleId(moduleId);
        module.setCode(data.getOrDefault("code", module.getCode()));
        module.setName(data.getOrDefault("name", data.getOrDefault("module_name", module.getName())));
        module.setDescription(data.getOrDefault("description", module.getDescription()));
        
        String creditsStr = data.getOrDefault("credits", "0");
        try {
            module.setCredits(Integer.parseInt(creditsStr));
        } catch (NumberFormatException e) {
            module.setCredits(0);
        }
        
        return moduleRepository.save(module);
    }

    public Evaluation createEvaluation(Map<String, String> data, Student student, Module module) {
        String evaluationId = data.getOrDefault("evaluation_id", data.getOrDefault("id", UUID.randomUUID().toString()));
        
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId(evaluationId);
        evaluation.setType(data.getOrDefault("type", data.getOrDefault("evaluation_type", "Exam")));
        evaluation.setTitle(data.getOrDefault("title", data.getOrDefault("evaluation_title", "")));
        
        String scoreStr = data.getOrDefault("score", "0");
        String maxScoreStr = data.getOrDefault("max_score", data.getOrDefault("maxscore", "100"));
        try {
            evaluation.setScore(Double.parseDouble(scoreStr));
            evaluation.setMaxScore(Double.parseDouble(maxScoreStr));
        } catch (NumberFormatException e) {
            evaluation.setScore(0.0);
            evaluation.setMaxScore(100.0);
        }
        
        String dateStr = data.getOrDefault("date", data.getOrDefault("evaluation_date", ""));
        if (!dateStr.isEmpty()) {
            try {
                evaluation.setDate(LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME));
            } catch (Exception e) {
                // Ignorer si le format n'est pas valide
            }
        }
        
        evaluation.setStatus(data.getOrDefault("status", "Completed"));
        evaluation.setStudent(student);
        evaluation.setModule(module);
        
        return evaluationRepository.save(evaluation);
    }

    public Activity createActivity(Map<String, String> data, Student student, Module module) {
        String activityId = data.getOrDefault("activity_id", data.getOrDefault("id", UUID.randomUUID().toString()));
        
        Activity activity = new Activity();
        activity.setActivityId(activityId);
        activity.setType(data.getOrDefault("type", data.getOrDefault("activity_type", "Lecture")));
        activity.setTitle(data.getOrDefault("title", data.getOrDefault("activity_title", "")));
        
        String dateStr = data.getOrDefault("date", data.getOrDefault("activity_date", ""));
        if (!dateStr.isEmpty()) {
            try {
                activity.setDate(LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME));
            } catch (Exception e) {
                // Ignorer si le format n'est pas valide
            }
        }
        
        String durationStr = data.getOrDefault("duration", "0");
        try {
            activity.setDuration(Integer.parseInt(durationStr));
        } catch (NumberFormatException e) {
            activity.setDuration(0);
        }
        
        String presentStr = data.getOrDefault("present", data.getOrDefault("presence", "true"));
        activity.setPresent(Boolean.parseBoolean(presentStr.toLowerCase()));
        
        activity.setStudent(student);
        activity.setModule(module);
        
        return activityRepository.save(activity);
    }
}

