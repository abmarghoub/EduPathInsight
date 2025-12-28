package ens.edupath.ingestion.model.neo4j;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("Module")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Module {
    @Id
    private String moduleId;
    private String code;
    private String name;
    private String description;
    private Integer credits;

    @Relationship(type = "ENROLLED_IN", direction = Relationship.Direction.INCOMING)
    private List<Student> students = new ArrayList<>();

    @Relationship(type = "HAS_EVALUATION", direction = Relationship.Direction.OUTGOING)
    private List<Evaluation> evaluations = new ArrayList<>();

    @Relationship(type = "HAS_RESOURCE", direction = Relationship.Direction.OUTGOING)
    private List<Resource> resources = new ArrayList<>();

    @Relationship(type = "HAS_ACTIVITY", direction = Relationship.Direction.OUTGOING)
    private List<Activity> activities = new ArrayList<>();
}


