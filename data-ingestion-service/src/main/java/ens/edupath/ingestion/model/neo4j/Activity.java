package ens.edupath.ingestion.model.neo4j;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;

@Node("Activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    @Id
    private String activityId;
    private String type; // Lecture, Lab, Exercise, etc.
    private String title;
    private LocalDateTime date;
    private Integer duration; // in minutes
    private Boolean present;

    @Relationship(type = "PARTICIPATES_IN", direction = Relationship.Direction.INCOMING)
    private Student student;

    @Relationship(type = "HAS_ACTIVITY", direction = Relationship.Direction.INCOMING)
    private Module module;
}


