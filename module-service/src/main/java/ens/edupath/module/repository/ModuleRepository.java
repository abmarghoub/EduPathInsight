package ens.edupath.module.repository;

import ens.edupath.module.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findByCode(String code);
    List<Module> findByActiveTrue();
    List<Module> findByActiveFalse();
    boolean existsByCode(String code);
}


