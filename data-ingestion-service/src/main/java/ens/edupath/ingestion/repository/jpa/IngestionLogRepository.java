package ens.edupath.ingestion.repository.jpa;

import ens.edupath.ingestion.model.jpa.IngestionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngestionLogRepository extends JpaRepository<IngestionLog, Long> {
    List<IngestionLog> findByEntityType(String entityType);
    List<IngestionLog> findByStatus(IngestionLog.Status status);
}


