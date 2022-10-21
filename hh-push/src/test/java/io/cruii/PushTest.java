package io.cruii;

import io.cruii.push.service.PushService;
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
public class PushTest {

    @Autowired
    private PushService pushService;


    @Test
    public void testPush() {
        boolean test = pushService.push("287969457", "test");
        Assert.assertTrue(test);
    }
}
