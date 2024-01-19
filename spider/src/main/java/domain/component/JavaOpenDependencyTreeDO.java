package domain.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * collection name:
 * java_component_open_dependency_tree
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JavaOpenDependencyTreeDO {

    private String id;

    private String groupId;

    private String artifactId;

    private String version;

    private ComponentDependencyTreeDO tree;
}
