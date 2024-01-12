package nju.edu.cn.qysca.service.components;

import nju.edu.cn.qysca.dao.components.JavaComponentDao;
import nju.edu.cn.qysca.domain.components.ComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.components.ComponentGavDTO;
import nju.edu.cn.qysca.domain.components.JavaComponentDO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComponentsServiceImpl implements ComponentsService {
    @Autowired
    private JavaComponentDao javaComponentDao;


    /**
     * 根据组件GAV获取组件依赖树
     *
     * @param componentGavDTO 组件GAV信息DTO
     * @return ComponentDependencyTreeDO 组件依赖树DO
     */
    @Override
    public ComponentDependencyTreeDO getComponentTreeByGAV(ComponentGavDTO componentGavDTO) {
        JavaComponentDO current=javaComponentDao.findNodeByGAV(componentGavDTO.getGroupId(),componentGavDTO.getArtifactId(),componentGavDTO.getVersion());
        ComponentDependencyTreeDO root=new ComponentDependencyTreeDO();
        BeanUtils.copyProperties(current,root);
        buildTreeRecursion(root);
        return root;
    }

    /**
     * 递归构造树
     * @param root 根节点
     * @return ComponentDependencyTreeDO 构造完毕的根节点
     */
    private void buildTreeRecursion(ComponentDependencyTreeDO root){
        // 添加依赖组件
        List<JavaComponentDO> adjacentDependencies=javaComponentDao.findAdjacentDependencyByGAV(root.getGroupId(), root.getArtifactId(), root.getVersion());
        for(JavaComponentDO adjacentDependency:adjacentDependencies){
            ComponentDependencyTreeDO node=new ComponentDependencyTreeDO();
            BeanUtils.copyProperties(adjacentDependency,node);
            // 递归构造子树
            buildTreeRecursion(node);
            root.getDependencies().add(node);
        }
        // 处理parent组件依赖问题
        List<JavaComponentDO> parentDependencies=javaComponentDao.findAdjacentParentByGAV(root.getGroupId(), root.getArtifactId(), root.getVersion());
        for(JavaComponentDO parentDependency:parentDependencies) {
            ComponentDependencyTreeDO node = new ComponentDependencyTreeDO();
            BeanUtils.copyProperties(parentDependency, node);
            // 递归构造子树
            buildTreeRecursion(node);
            root.getDependencies().addAll(node.getDependencies());
        }
    }
}
