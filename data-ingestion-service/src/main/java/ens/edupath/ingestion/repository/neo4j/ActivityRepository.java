package ens.edupath.ingestion.repository.neo4j;

import ens.edupath.ingestion.model.neo4j.Activity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivityRepository extends Neo4jRepository<Activity, String> {
    Optional<Activity> findByActivityId(String activityId);
}


