package io.cruii.component;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.mapper.BilibiliUserMapper;
import io.cruii.pojo.po.BilibiliUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author cruii
 * Created on 2022/4/6
 */
//@Component
@Slf4j
public class UserPostProcessor implements CommandLineRunner {
    private final Consumer<byte[]> consumer;
    private final BilibiliUserMapper bilibiliUserMapper;

    public UserPostProcessor(Consumer<byte[]> consumer,
                             BilibiliUserMapper bilibiliUserMapper) {
        this.consumer = consumer;
        this.bilibiliUserMapper = bilibiliUserMapper;
    }

    @Override
    public void run(String... args) {
        //new Thread(() -> {
        //    while (true) {
        //        // Wait for a message
        //        Message<byte[]> msg = null;
        //        try {
        //            msg = consumer.receive();
        //
        //            assert msg != null;
        //            String data = new String(msg.getData());
        //            BilibiliUser bilibiliUser;
        //            if (JSONUtil.isTypeJSON(data)) {
        //                bilibiliUser = JSONUtil.parseObj(data).toBean(BilibiliUser.class);
        //            } else {
        //                bilibiliUser = bilibiliUserMapper.selectOne(Wrappers.<BilibiliUser>lambdaQuery().eq(BilibiliUser::getDedeuserid, data));
        //            }
        //            bilibiliUser.setLastRunTime(LocalDateTime.now());
        //            bilibiliUserMapper.update(bilibiliUser, Wrappers.<BilibiliUser>lambdaUpdate().eq(BilibiliUser::getDedeuserid, bilibiliUser.getDedeuserid()));
        //
        //            // Acknowledge the message so that it can be deleted by the message broker
        //            consumer.acknowledge(msg);
        //        } catch (PulsarClientException e) {
        //            log.warn("Message failed to process, redeliver later", e);
        //            consumer.negativeAcknowledge(msg);
        //        }
        //    }
        //}).
        //
        //        start();
    }
}
