package domain.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * collection name:
 * java_component_open_detail
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JavaOpenComponentDO {

    private String id;

    private String groupId;

    private String artifactId;

    private String version;

    private String language;

    private String name;

    private String description;

    private String url;

    private String downloadUrl;

    private String sourceUrl;

    private List<DeveloperDO> developers;

    private List<LicenseDO> licenses;

    private String pom;
}
