package ens.edupath.ingestion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories(basePackages = "ens.edupath.ingestion.repository.neo4j")
public class Neo4jConfig {
    // La configuration Neo4j est gérée automatiquement par Spring Boot via application.yml
}

