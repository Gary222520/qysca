package nju.edu.cn.qysca.service.example;

import nju.edu.cn.qysca.dao.components.JavaComponentDao;
import nju.edu.cn.qysca.domain.components.JavaComponentDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExampleServiceImpl implements ExampleService {
    @Autowired
    JavaComponentDao javaComponentDao;

    /**
     * 新增一个Java组件节点
     *
     * @param javaComponentDO Java组件DO
     */
    @Override
    @Transactional
    public void addOne(JavaComponentDO javaComponentDO) {
        javaComponentDao.save(javaComponentDO);
    }

    /**
     * 查询所有Java组件节点
     *
     * @return List<JavaComponentDO> 所有Java组件节点
     */
    @Override
    public List<JavaComponentDO> findAll() {
        return javaComponentDao.findAll();
    }
}
