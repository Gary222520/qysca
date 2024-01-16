package domain.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * collection name:
 *      java_component_open_dependency_tree
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JavaOpenDependencyTreeDO {
    private String id;

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

    private List<JavaOpenDependencyTreeDO> dependencies = new ArrayList<>();
}
