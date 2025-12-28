package ens.edupath.ingestion.repository.neo4j;

import ens.edupath.ingestion.model.neo4j.Module;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModuleRepository extends Neo4jRepository<Module, String> {
    Optional<Module> findByModuleId(String moduleId);
    Optional<Module> findByCode(String code);
}


