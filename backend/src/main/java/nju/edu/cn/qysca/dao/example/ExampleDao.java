package nju.edu.cn.qysca.dao.example;

import nju.edu.cn.qysca.domain.example.ExampleDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExampleDao extends JpaRepository<ExampleDTO, String> {
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
    @Query(value = "SELECT * FROM example e WHERE 1=1 AND e.number<=5 AND e.info NOT LIKE CONCAT('%',CONCAT(:string,'%'))", nativeQuery = true)
    List<ExampleDTO> findByRule(String string);
}
