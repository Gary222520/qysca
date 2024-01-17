package domain.component;

import lombok.Data;

import java.util.List;

@Data
public class JavaOpenComponentInformationDO {

    private JavaOpenComponentDO javaOpenComponentDO;

    private JavaOpenDependencyTreeDO javaOpenDependencyTreeDO;

    private List<JavaOpenDependencyTableDO> javaOpenDependencyTableDO;
}
