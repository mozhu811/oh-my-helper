package io.cruii.bilibili.component;

import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

/**
 * @author cruii
 * Created on 2021/9/26
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Log4j2
public class TaskRunnerTest {

    @Autowired
    private TaskRunner taskRunner;

    @Test
    public void run() throws InterruptedException {
        taskRunner.run();
        new CountDownLatch(1).await();
    }
}