package nju.edu.cn.qysca;

import nju.edu.cn.qysca.utils.ZipUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ZipUtilTest {

    @Test
    public void testZipUtil() {
        ZipUtil.extractZipForPom("src/main/backend.zip");
    }
}
