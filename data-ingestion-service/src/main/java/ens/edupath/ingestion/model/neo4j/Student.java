package ens.edupath.ingestion.model.neo4j;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("Student")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    private String studentId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    @Relationship(type = "ENROLLED_IN", direction = Relationship.Direction.OUTGOING)
    private List<Module> modules = new ArrayList<>();

    @Relationship(type = "HAS_EVALUATION", direction = Relationship.Direction.OUTGOING)
    private List<Evaluation> evaluations = new ArrayList<>();

    @Relationship(type = "PARTICIPATES_IN", direction = Relationship.Direction.OUTGOING)
    private List<Activity> activities = new ArrayList<>();
}


