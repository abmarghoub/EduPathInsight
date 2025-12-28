package ens.edupath.note.repository;

import ens.edupath.note.entity.KPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KPIRepository extends JpaRepository<KPI, Long> {
    Optional<KPI> findByStudentIdAndModuleId(String studentId, Long moduleId);
    List<KPI> findByStudentId(String studentId);
    List<KPI> findByModuleId(Long moduleId);
}


