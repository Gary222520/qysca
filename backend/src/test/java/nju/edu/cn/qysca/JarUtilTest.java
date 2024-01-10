package nju.edu.cn.qysca;

import nju.edu.cn.qysca.util.JarUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JarUtilTest {


    @Test
    public void testJarUtil() {
        JarUtil.extractJarForPom("src/main/qysca-v1.0.0.jar");
    }
}
