package io.cruii;

import cn.hutool.core.lang.Assert;
import io.cruii.pojo.dto.PushMessageDTO;
import io.cruii.push.service.PushService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@Log4j2
public class PushTest {

    @Autowired
    private PushService pushService;


    @Test
    public void testPush() {
        PushMessageDTO pushMessageDTO = new PushMessageDTO();
        pushMessageDTO.setDedeuserid("287969457");
        pushMessageDTO.setContent("123test");
        boolean res = pushService.push(pushMessageDTO);
        Assert.isTrue(res);
    }
}
