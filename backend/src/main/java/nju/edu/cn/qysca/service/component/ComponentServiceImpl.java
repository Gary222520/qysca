package nju.edu.cn.qysca.service.component;

import fr.dutra.tools.maven.deptree.core.Node;
import nju.edu.cn.qysca.dao.components.JavaCloseComponentDao;
import nju.edu.cn.qysca.dao.components.JavaCloseDependencyTableDao;
import nju.edu.cn.qysca.dao.components.JavaCloseDependencyTreeDao;
import nju.edu.cn.qysca.domain.components.SaveCloseComponentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComponentServiceImpl implements ComponentService{

    @Autowired
    private JavaCloseComponentDao javaCloseComponentDao;

    @Autowired
    private JavaCloseDependencyTreeDao javaCloseDependencyTreeDao;

    @Autowired
    private JavaCloseDependencyTableDao javaCloseDependencyTableDao;
    @Override
    public Boolean saveCloseComponent(SaveCloseComponentDTO saveCloseComponentDTO) {
        // 接口获得详细信息
        return true;
    }

}
