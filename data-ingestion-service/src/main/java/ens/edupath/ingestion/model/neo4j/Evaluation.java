package ens.edupath.ingestion.model.neo4j;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;

@Node("Evaluation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {
    @Id
    private String evaluationId;
    private String type; // Exam, Assignment, Project, etc.
    private String title;
    private Double score;
    private Double maxScore;
    private LocalDateTime date;
    private String status; // Completed, Pending, etc.

    @Relationship(type = "HAS_EVALUATION", direction = Relationship.Direction.INCOMING)
    private Student student;

    @Relationship(type = "EVALUATES", direction = Relationship.Direction.INCOMING)
    private Module module;
}

