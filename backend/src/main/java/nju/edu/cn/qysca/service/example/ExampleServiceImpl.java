package nju.edu.cn.qysca.service.example;

import nju.edu.cn.qysca.dao.example.ExampleDao;
import nju.edu.cn.qysca.domain.example.ExampleDO;
import nju.edu.cn.qysca.exception.PlatformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Id;
import java.util.List;

@Service
public class ExampleServiceImpl implements ExampleService {

    @Autowired
    private ExampleDao exampleDao;

    /**
     * 新增一条记录
     *
     * @param exampleDO 示例数据对象
     * @return ExampleDO 新增的记录
     */
    @Override
    @Transactional
    public ExampleDO addOne(ExampleDO exampleDO) {
        return exampleDao.save(exampleDO);
    }

    /**
     * 删除一条记录
     *
     * @param uuid 自动编号
     */
    @Override
    @Transactional
    public void deleteOne(String uuid) {
        if (!exampleDao.existsById(uuid)) {
            throw new PlatformException(400, "删除失败，该记录不存在");
        }
        exampleDao.deleteById(uuid);
    }

    /**
     * 更新一条记录
     *
     * @param exampleDO 示例数据对象
     * @return ExampleDO 更新后的记录
     */
    @Override
    @Transactional
    public ExampleDO updateOne(ExampleDO exampleDO) {
        if (!exampleDao.existsById(exampleDO.getUuid())) {
            throw new PlatformException(400, "更新失败，该记录不存在");
        }
        exampleDao.deleteById(exampleDO.getUuid());
        return exampleDao.save(exampleDO);
    }

    /**
     * 查询所有记录
     *
     * @return List<ExampleDO> 所有记录
     */
    @Override
    public List<ExampleDO> findAll() {
        return exampleDao.findAll();
    }

    /**
     * 根据info查询记录
     *
     * @param info 信息
     * @return List<ExampleDO> 查询到的记录
     */
    @Override
    public List<ExampleDO> findByInfo(String info) {
        return exampleDao.findByInfo(info);
    }

    /**
     * 根据规则查询记录（number<=5且info中不含有特定字符串string）
     *
     * @param string 特定字符串
     * @return List<ExampleDO> 查询到的记录
     */
    @Override
    public List<ExampleDO> findByRule(String string) {
        return exampleDao.findByRule(string);
    }

}
