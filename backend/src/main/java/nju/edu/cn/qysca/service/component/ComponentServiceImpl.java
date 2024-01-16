package nju.edu.cn.qysca.service.component;

import nju.edu.cn.qysca.dao.component.JavaCloseComponentDao;
import nju.edu.cn.qysca.dao.component.JavaOpenComponentDao;
import nju.edu.cn.qysca.domain.component.ComponentSearchDTO;
import nju.edu.cn.qysca.domain.component.JavaCloseComponentDO;
import nju.edu.cn.qysca.domain.component.JavaOpenComponentDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ComponentServiceImpl implements ComponentService{
    @Autowired
    private JavaOpenComponentDao javaOpenComponentDao;

    @Autowired
    private JavaCloseComponentDao javaCloseComponentDao;

    /**
     * 分页查询开源组件
     *
     * @param searchComponentDTO 查询条件
     * @return Page<JavaOpenComponentDO> 查询结果
     */
    public Page<JavaOpenComponentDO> findOpenComponentsPage(ComponentSearchDTO searchComponentDTO){
        // 设置查询条件
        JavaOpenComponentDO searcher=new JavaOpenComponentDO();
        searcher.setGroupId(searchComponentDTO.getGroupId().equals("")?null:searchComponentDTO.getGroupId());
        searcher.setArtifactId(searchComponentDTO.getArtifactId().equals("")?null: searchComponentDTO.getArtifactId());
        searcher.setVersion(searchComponentDTO.getArtifactId().equals("")?null: searchComponentDTO.getVersion());
        searcher.setName(searchComponentDTO.getName().equals("")?null: searchComponentDTO.getName());
        searcher.setLanguage(searchComponentDTO.getLanguage().equals("")?null: searchComponentDTO.getLanguage());
        // 设置模糊查询器
        ExampleMatcher matcher=ExampleMatcher.matching()
                .withIgnorePaths("id","description","url","downloadUrl","sourceUrl","developers","licenses","pom")
                .withIgnoreNullValues();
        Example<JavaOpenComponentDO> example=Example.of(searcher,matcher);
        // 设置排序规则
        List<Sort.Order> orders=new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC,"language"));
        orders.add(new Sort.Order(Sort.Direction.ASC,"name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC,"groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC,"artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC,"version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable= PageRequest.of(searchComponentDTO.getNumber()-1,searchComponentDTO.getSize(),Sort.by(orders));
        return javaOpenComponentDao.findAll(example,pageable);
    }

    /**
     * 分页查询闭源组件
     *
     * @param searchComponentDTO 查询条件
     * @return Page<JavaCloseComponentDO> 查询结果
     */
    @Override
    public Page<JavaCloseComponentDO> findCloseComponentsPage(ComponentSearchDTO searchComponentDTO) {
        // 设置查询条件
        JavaCloseComponentDO searcher=new JavaCloseComponentDO();
        searcher.setGroupId(searchComponentDTO.getGroupId().equals("")?null:searchComponentDTO.getGroupId());
        searcher.setArtifactId(searchComponentDTO.getArtifactId().equals("")?null: searchComponentDTO.getArtifactId());
        searcher.setVersion(searchComponentDTO.getArtifactId().equals("")?null: searchComponentDTO.getVersion());
        searcher.setName(searchComponentDTO.getName().equals("")?null: searchComponentDTO.getName());
        searcher.setLanguage(searchComponentDTO.getLanguage().equals("")?null: searchComponentDTO.getLanguage());
        // 设置模糊查询器
        ExampleMatcher matcher=ExampleMatcher.matching()
                .withIgnorePaths("id","description","url","downloadUrl","sourceUrl","developers","licenses","pom")
                .withIgnoreNullValues();
        Example<JavaCloseComponentDO> example=Example.of(searcher,matcher);
        // 设置排序规则
        List<Sort.Order> orders=new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC,"language"));
        orders.add(new Sort.Order(Sort.Direction.ASC,"name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC,"groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC,"artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC,"version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable= PageRequest.of(searchComponentDTO.getNumber()-1,searchComponentDTO.getSize(),Sort.by(orders));
        return javaCloseComponentDao.findAll(example,pageable);
    }

}
