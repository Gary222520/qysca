package nju.edu.cn.qysca.service.example;

import nju.edu.cn.qysca.domain.components.JavaComponentDO;

import java.util.List;

public interface ExampleService {

    /**
     * 新增一个Java组件节点
     *
     * @param javaComponentDO Java组件DO
     */
    void addOne(JavaComponentDO javaComponentDO);

    /**
     * 查询所有Java组件节点
     *
     * @return List<JavaComponentDO> 所有Java组件节点
     */
    List<JavaComponentDO> findAll();

}
