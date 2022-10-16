package io.cruii;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.mapper.TaskConfigMapper;
import io.cruii.pojo.po.TaskConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@Log4j2
public class TaskConfigTest {
    @Autowired
    private TaskConfigMapper taskConfigMapper;

    @Test
    public void testQueryOne() {
        TaskConfig taskConfig = taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfig.class)
                .eq(TaskConfig::getDedeuserid, "287969457"));
        log.info(taskConfig);
        Assert.assertNotNull(taskConfig);
    }
}
