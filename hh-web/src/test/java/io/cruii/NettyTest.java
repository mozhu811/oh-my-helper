package io.cruii;

import io.cruii.component.NettyServer;
import io.cruii.handler.ServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author cruii
 * Created on 2022/10/17
 */
@SpringBootTest
@RunWith(org.springframework.test.context.junit4.SpringRunner.class)
@Slf4j
public class NettyTest {
    @Autowired
    private ServerHandler serverHandler;

    @Test
    public void testSend() {
        serverHandler.sendMsg("123123");
    }
}
