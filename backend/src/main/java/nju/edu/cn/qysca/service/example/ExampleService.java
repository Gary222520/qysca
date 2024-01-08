package nju.edu.cn.qysca.service.example;

import nju.edu.cn.qysca.domain.example.ExampleDO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ExampleService {

    /**
     * 新增一条记录
     *
     * @param exampleDO 示例数据对象
     * @return ExampleDO 新增的记录
     */
    ExampleDO addOne(ExampleDO exampleDO);

    /**
     * 删除一条记录
     *
     * @param uuid 自动编号
     */
    public void deleteOne(String uuid);

    /**
     * 更新一条记录
     *
     * @param exampleDO 示例数据对象
     * @return ExampleDO 更新后的记录
     */
    ExampleDO updateOne(ExampleDO exampleDO);

    /**
     * 查询所有记录
     *
     * @return List<ExampleDO> 所有记录
     */
    List<ExampleDO> findAll();

    /**
     * 根据info查询记录
     *
     * @param info 信息
     * @return List<ExampleDO> 查询到的记录
     */
    List<ExampleDO> findByInfo(String info);

    /**
     * 根据规则查询记录（number<=5且info中不含有特定字符串string）
     *
     * @param string 特定字符串
     * @return List<ExampleDO> 查询到的记录
     */
    List<ExampleDO> findByRule(String string);
}
