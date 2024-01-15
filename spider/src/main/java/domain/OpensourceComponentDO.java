package domain;

import lombok.Data;
import org.json.JSONObject;

import java.util.List;

@Data
public class OpensourceComponentDO {

    private String groupId;
    private String artifactId;
    private String version;
    private String name;
    private String language;
    private String opensource;
    private String description;
    private String url;
    private String downloadUrl;
    private String sourceUrl;
    private List<Developer> developers;
    private List<License> licenses;
    private String pom;

}
