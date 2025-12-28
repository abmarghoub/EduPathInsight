package ens.edupath.module.repository;

import ens.edupath.module.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByModuleIdAndStudentId(Long moduleId, String studentId);
    List<Enrollment> findByStudentId(String studentId);
    List<Enrollment> findByModuleId(Long moduleId);
    List<Enrollment> findByModuleIdAndStatus(Long moduleId, Enrollment.EnrollmentStatus status);
    boolean existsByModuleIdAndStudentId(Long moduleId, String studentId);
}


