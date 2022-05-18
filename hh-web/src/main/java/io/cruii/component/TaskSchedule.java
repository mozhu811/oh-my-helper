package io.cruii.component;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.mapper.BilibiliUserMapper;
import io.cruii.mapper.TaskConfigMapper;
import io.cruii.pojo.po.BilibiliUser;
import io.cruii.pojo.po.TaskConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2022/4/7
 */
@Component
@Slf4j
public class TaskSchedule {
    private final Producer<byte[]> producer;
    private final TaskConfigMapper taskConfigMapper;

    private final BilibiliUserMapper userMapper;

    public TaskSchedule(Producer<byte[]> producer,
                        TaskConfigMapper taskConfigMapper,
                        BilibiliUserMapper userMapper) {
        this.producer = producer;
        this.taskConfigMapper = taskConfigMapper;
        this.userMapper = userMapper;
    }

    @Scheduled(cron = "${task.cron:0 10 0 * * ?}")
    public void doTask() {
        // 获取当日开始时间
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        // 获取当日结束时间
        LocalDateTime end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        List<String> ids = userMapper.selectList(Wrappers.lambdaQuery(BilibiliUser.class)
                        .notBetween(BilibiliUser::getLastRunTime, start, end)
                        .or()
                        .isNull(BilibiliUser::getLastRunTime))
                .stream()
                .map(BilibiliUser::getDedeuserid)
                .collect(Collectors.toList());
        List<TaskConfig> taskConfigs = taskConfigMapper.selectList(
                Wrappers.lambdaQuery(TaskConfig.class).in(TaskConfig::getDedeuserid, ids));
        log.info("本次执行任务数量：{}", taskConfigs.size());
        taskConfigs.forEach(taskConfig -> {
            try {
                producer.send(JSONUtil.toJsonStr(taskConfig).getBytes(StandardCharsets.UTF_8));
            } catch (PulsarClientException e) {
                log.error("发送消息失败", e);
            }
        });
    }
}
