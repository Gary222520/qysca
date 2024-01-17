package domain.component;

import lombok.Data;

@Data
public class JavaOpenComponentInformationDO {

    private JavaOpenComponentDO javaOpenComponentDO;

    private JavaOpenDependencyTreeDO javaOpenDependencyTreeDO;

    private JavaOpenDependencyTableDO javaOpenDependencyTableDO;
}
