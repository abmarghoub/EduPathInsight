package ens.edupath.ingestion.repository.jpa;

import ens.edupath.ingestion.model.jpa.AIFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIFeatureRepository extends JpaRepository<AIFeature, Long> {
    Optional<AIFeature> findByEntityTypeAndEntityId(String entityType, String entityId);
    List<AIFeature> findByEntityType(String entityType);
}


