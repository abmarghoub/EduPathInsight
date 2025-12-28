package ens.edupath.note.repository;

import ens.edupath.note.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStudentId(String studentId);
    List<Alert> findByModuleId(Long moduleId);
    List<Alert> findByStudentIdAndModuleId(String studentId, Long moduleId);
    List<Alert> findByStatus(Alert.AlertStatus status);
    List<Alert> findByType(Alert.AlertType type);
}


