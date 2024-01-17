package nju.edu.cn.qysca.service.component;

import nju.edu.cn.qysca.dao.component.*;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.component.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ComponentServiceImpl implements ComponentService {
    @Autowired
    private JavaOpenComponentDao javaOpenComponentDao;

    @Autowired
    private JavaCloseComponentDao javaCloseComponentDao;

    @Autowired
    private JavaOpenDependencyTreeDao javaOpenDependencyTreeDao;

    @Autowired
    private JavaCloseDependencyTreeDao javaCloseDependencyTreeDao;

    @Autowired
    private JavaOpenDependencyTableDao javaOpenDependencyTableDao;

    @Autowired
    private JavaCloseDependencyTableDao javaCloseDependencyTableDao;

    /**
     * 分页查询开源组件
     *
     * @param searchComponentDTO 查询条件
     * @return Page<JavaOpenComponentDO> 查询结果
     */
    public Page<JavaOpenComponentDO> findOpenComponentsPage(ComponentSearchDTO searchComponentDTO) {
        // 设置查询条件
        JavaOpenComponentDO searcher = new JavaOpenComponentDO();
        searcher.setGroupId(searchComponentDTO.getGroupId().equals("") ? null : searchComponentDTO.getGroupId());
        searcher.setArtifactId(searchComponentDTO.getArtifactId().equals("") ? null : searchComponentDTO.getArtifactId());
        searcher.setVersion(searchComponentDTO.getArtifactId().equals("") ? null : searchComponentDTO.getVersion());
        searcher.setName(searchComponentDTO.getName().equals("") ? null : searchComponentDTO.getName());
        searcher.setLanguage(searchComponentDTO.getLanguage().equals("") ? null : searchComponentDTO.getLanguage());
        // 设置模糊查询器
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "description", "url", "downloadUrl", "sourceUrl", "developers", "licenses", "pom")
                .withIgnoreNullValues();
        Example<JavaOpenComponentDO> example = Example.of(searcher, matcher);
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "language"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(searchComponentDTO.getNumber() - 1, searchComponentDTO.getSize(), Sort.by(orders));
        Page<JavaOpenComponentDO> tmpRes = javaOpenComponentDao.findAll(example, pageable);
        Page<JavaOpenComponentDO> ans = new PageImpl<>(tmpRes.getContent());

        return javaOpenComponentDao.findAll(example, pageable);
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
        JavaCloseComponentDO searcher = new JavaCloseComponentDO();
        searcher.setGroupId(searchComponentDTO.getGroupId().equals("") ? null : searchComponentDTO.getGroupId());
        searcher.setArtifactId(searchComponentDTO.getArtifactId().equals("") ? null : searchComponentDTO.getArtifactId());
        searcher.setVersion(searchComponentDTO.getArtifactId().equals("") ? null : searchComponentDTO.getVersion());
        searcher.setName(searchComponentDTO.getName().equals("") ? null : searchComponentDTO.getName());
        searcher.setLanguage(searchComponentDTO.getLanguage().equals("") ? null : searchComponentDTO.getLanguage());
        // 设置模糊查询器
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "description", "url", "downloadUrl", "sourceUrl", "developers", "licenses", "pom")
                .withIgnoreNullValues();
        Example<JavaCloseComponentDO> example = Example.of(searcher, matcher);
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "language"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(searchComponentDTO.getNumber() - 1, searchComponentDTO.getSize(), Sort.by(orders));
        return javaCloseComponentDao.findAll(example, pageable);
    }

    @Override
    public Boolean saveCloseComponent(SaveCloseComponentDTO saveCloseComponentDTO) {
        // 接口获得详细信息
        return true;
    }

    /**
     * 查询开源组件依赖树信息
     *
     * @param componentGavDTO 组件gav
     * @return JavaOpenDependencyTreeDO 依赖树信息
     */
    @Override
    public JavaOpenDependencyTreeDO findOpenComponentDependencyTree(ComponentGavDTO componentGavDTO) {
        return javaOpenDependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
    }

    /**
     * 查询闭源组件依赖树信息
     *
     * @param componentGavDTO 组件gav
     * @return JavaCloseDependencyTreeDO 依赖树信息
     */
    @Override
    public JavaCloseDependencyTreeDO findCloseComponentDependencyTree(ComponentGavDTO componentGavDTO) {
        return javaCloseDependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
    }

    /**
     * 分页查询开源组件依赖平铺信息
     *
     * @param componentGavPageDTO 带分页组件gav信息
     * @return Page<ComponentTableDTO> 分页查询结果
     */
    @Override
    public Page<ComponentTableDTO> findOpenComponentDependencyTable(ComponentGavPageDTO componentGavPageDTO) {
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "depth").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(componentGavPageDTO.getNumber() - 1, componentGavPageDTO.getSize(), Sort.by(orders));
        return javaOpenDependencyTableDao.findByGAV(
                componentGavPageDTO.getGroupId(),
                componentGavPageDTO.getArtifactId(),
                componentGavPageDTO.getVersion(), pageable);
    }

    /**
     * 分页查询闭源组件依赖平铺信息
     *
     * @param componentGavPageDTO 带分页组件gav信息
     * @return Page<ComponentTableDTO> 分页查询结果
     */
    @Override
    public Page<ComponentTableDTO> findCloseComponentDependencyTable(ComponentGavPageDTO componentGavPageDTO) {
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "depth").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(componentGavPageDTO.getNumber() - 1, componentGavPageDTO.getSize(), Sort.by(orders));
        return javaCloseDependencyTableDao.findByGAV(
                componentGavPageDTO.getGroupId(),
                componentGavPageDTO.getArtifactId(),
                componentGavPageDTO.getVersion(), pageable);
    }

    /**
     * 查询指定开源组件的详细信息
     *
     * @param componentGavDTO 组件gav
     * @return ComponentDetailDTO 组件详细信息
     */
    @Override
    public ComponentDetailDTO findOpenComponentDetail(ComponentGavDTO componentGavDTO) {
        return javaOpenComponentDao.findDetailByGav(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
    }

    /**
     * 查询指定闭源组件的详细信息
     *
     * @param componentGavDTO 组件gav
     * @return ComponentDetailDTO 组件详细信息
     */
    @Override
    public ComponentDetailDTO findCloseComponentDetail(ComponentGavDTO componentGavDTO) {
        return javaCloseComponentDao.findDetailByGav(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
    }

}
