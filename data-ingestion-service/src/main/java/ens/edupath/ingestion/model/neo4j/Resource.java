package ens.edupath.ingestion.model.neo4j;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Resource")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {
    @Id
    private String resourceId;
    private String title;
    private String type; // Video, Document, Link, etc.
    private String url;
    private String description;

    @Relationship(type = "HAS_RESOURCE", direction = Relationship.Direction.INCOMING)
    private Module module;
}


