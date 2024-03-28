package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.PythonComponentDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PythonComponentDao extends JpaRepository<PythonComponentDO, String> {
    /**
     *  根据名称和版本号查找组件
     * @param name 名称
     * @param version 版本号
     * @return PythonComponentDO python组件信息
     */
    PythonComponentDO findByNameAndVersion(String name, String version);

    /**
     * 根据Id查询组件列表
     * @param ids id
     * @return List<PythonComponentDO> python组件信息
     */
    List<PythonComponentDO> findByIdIn(List<String> ids);
}
