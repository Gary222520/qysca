package nju.edu.cn.qysca.service.example;

import nju.edu.cn.qysca.domain.example.ExampleDTO;

import java.util.List;

public interface ExampleService {

    /**
     * 新增一条记录
     *
     * @param exampleDTO 示例数据对象
     * @return ExampleDTO 新增的记录
     */
    ExampleDTO addOne(ExampleDTO exampleDTO);

    /**
     * 删除一条记录
     *
     * @param uuid 自动编号
     */
    public void deleteOne(String uuid);

    /**
     * 更新一条记录
     *
     * @param exampleDTO 示例数据对象
     * @return ExampleDTO 更新后的记录
     */
    ExampleDTO updateOne(ExampleDTO exampleDTO);

    /**
     * 查询所有记录
     *
     * @return List<ExampleDTO> 所有记录
     */
    List<ExampleDTO> findAll();

    /**
     * 根据info查询记录
     *
     * @param info 信息
     * @return List<ExampleDTO> 查询到的记录
     */
    List<ExampleDTO> findByInfo(String info);

    /**
     * 根据规则查询记录（number<=5且info中不含有特定字符串string）
     *
     * @param string 特定字符串
     * @return List<ExampleDTO> 查询到的记录
     */
    List<ExampleDTO> findByRule(String string);
}
