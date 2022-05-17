package io.cruii.component;

import cn.hutool.json.JSONUtil;
import io.cruii.mapper.TaskConfigMapper;
import io.cruii.pojo.po.TaskConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author cruii
 * Created on 2022/4/7
 */
@Component
@Slf4j
public class TaskSchedule {
    private final Producer<byte[]> producer;
    private final TaskConfigMapper taskConfigMapper;

    public TaskSchedule(Producer<byte[]> producer,
                        TaskConfigMapper taskConfigMapper) {
        this.producer = producer;
        this.taskConfigMapper = taskConfigMapper;
    }

    @Scheduled(cron = "${task.cron:0 10 0 * * ?}")
    public void doTask() {
        List<TaskConfig> taskConfigs = taskConfigMapper.selectList(null);
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
