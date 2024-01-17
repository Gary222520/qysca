package domain.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * collection name:
 * java_component_open_dependency_table
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JavaOpenDependencyTableDO {

    private String id;

    private String parentGroupId;

    private String parentArtifactId;

    private String parentVersion;

    private String groupId;

    private String artifactId;

    private String version;

    private String name;

    private String scope;

    private Integer depth;

    private Boolean opensource;

    private String language;

    private String licenses;

    private Boolean direct;
}
