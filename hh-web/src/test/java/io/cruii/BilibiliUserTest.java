package io.cruii;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.mapper.BilibiliUserMapper;
import io.cruii.pojo.po.BilibiliUser;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author cruii
 * Created on 2022/8/24
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@Log4j2
public class BilibiliUserTest {

    @Autowired
    private BilibiliUserMapper bilibiliUserMapper;

    @Test
    public void testQueryList() {
        List<BilibiliUser> users = bilibiliUserMapper.selectList(null);
        Assert.assertFalse(users.isEmpty());
    }

    @Test
    public void testQueryOne() {
        BilibiliUser bilibiliUser = bilibiliUserMapper.selectOne(Wrappers.lambdaQuery(BilibiliUser.class)
                .eq(BilibiliUser::getDedeuserid, "287969457"));
        log.info(bilibiliUser);
        Assert.assertNotNull(bilibiliUser);
    }
}
