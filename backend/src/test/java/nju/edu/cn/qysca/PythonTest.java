package nju.edu.cn.qysca;

import nju.edu.cn.qysca.domain.component.dos.PythonDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.PythonDependencyTreeDO;
import nju.edu.cn.qysca.service.python.PythonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class PythonTest {

    @Autowired
    private PythonService pythonService;

    @Test
    public void test1(){
        String filePath = "D:\\MyCourse\\2024Aping\\graduate design\\qysca\\backend\\src\\main\\resources\\upload\\vyper.zip";
        String builder = "zip";
        String name = "vyper";
        String version = "0.4.0b6";
        String type = "internal";
        PythonDependencyTreeDO pythonDependencyTreeDO = pythonService.dependencyTreeAnalysis(filePath, builder, name, version, type);

        List<PythonDependencyTableDO> pythonDependencyTableDOList = pythonService.dependencyTableAnalysis(pythonDependencyTreeDO);
    }

    @Test
    public void test2(){
        PythonDependencyTreeDO pythonDependencyTreeDO = pythonService.spiderDependency("kafka-counter","0.0.2");
    }
}
