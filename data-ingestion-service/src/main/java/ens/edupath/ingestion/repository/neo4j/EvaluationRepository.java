package ens.edupath.ingestion.repository.neo4j;

import ens.edupath.ingestion.model.neo4j.Evaluation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluationRepository extends Neo4jRepository<Evaluation, String> {
    Optional<Evaluation> findByEvaluationId(String evaluationId);
}


