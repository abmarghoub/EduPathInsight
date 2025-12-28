package ens.edupath.module.repository;

import ens.edupath.module.entity.EnrollmentPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnrollmentPeriodRepository extends JpaRepository<EnrollmentPeriod, Long> {
    Optional<EnrollmentPeriod> findByModuleId(Long moduleId);
    boolean existsByModuleId(Long moduleId);
}


