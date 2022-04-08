package io.cruii.execution.component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import io.cruii.component.BilibiliDelegate;
import io.cruii.component.TaskExecutor;
import io.cruii.context.BilibiliUserContext;
import io.cruii.exception.RequestException;
import io.cruii.pojo.po.BilibiliUser;
import io.cruii.pojo.po.TaskConfig;
import io.cruii.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2022/4/2
 */
@Slf4j
@Component
public class TaskWorker implements CommandLineRunner {
    private final Consumer<byte[]> consumer;

    private final Producer<byte[]> producer;

    private final ThreadPoolTaskExecutor taskExecutor;

    public TaskWorker(Consumer<byte[]> consumer,
                      Producer<byte[]> producer, ThreadPoolTaskExecutor taskExecutor) {
        this.consumer = consumer;
        this.producer = producer;
        this.taskExecutor = taskExecutor;
    }

    @Value("${push.url}")
    private String pushUrl;

    @Override
    public void run(String... args) {
        log.info("TaskWorker start");
        new Thread(() -> {
            while (true) {
                // Wait for a message
                Message<byte[]> msg = null;
                try {
                    msg = consumer.receive();
                } catch (PulsarClientException e) {
                    Thread.currentThread().interrupt();
                    log.error("TaskWorker receive error", e);
                }

                try {
                    // Do something with the message
                    assert msg != null;
                    TaskConfig taskConfig = JSONUtil.parseObj(new String(msg.getData())).toBean(TaskConfig.class);
                    taskExecutor.execute(() -> {
                        BilibiliDelegate delegate = new BilibiliDelegate(taskConfig);
                        try {
                            BilibiliUser user = delegate.getUser();
                            BilibiliUserContext.set(user);
                            TaskExecutor executor = new TaskExecutor(delegate);
                            user = executor.execute();
                            String traceId = MDC.get("traceId");
                            user.setLastRunTime(LocalDateTime.now());

                            // 通知调用端执行完成
                            producer.sendAsync(JSONUtil.toJsonStr(user).getBytes(StandardCharsets.UTF_8));

                            // 日志收集
                            String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
                            List<String> logs = FileUtil.readLines(new File("logs/execution/all-" + date + ".log"), StandardCharsets.UTF_8);

                            String content = logs
                                    .stream()
                                    .filter(line -> line.contains(traceId) && (line.contains("INFO") || line.contains("ERROR")))
                                    .map(line -> line.split("\\|\\|")[1])
                                    .collect(Collectors.joining("\n"));
                            // 推送
                            push(taskConfig.getDedeuserid(), content);
                        } catch (Exception e) {
                            log.error("任务执行失败", e);
                        } finally {
                            MDC.clear();
                            BilibiliUserContext.remove();
                        }
                    });

                    // Acknowledge the message so that it can be deleted by the message broker
                    consumer.acknowledge(msg);
                } catch (Exception e) {
                    log.warn("Message failed to process, redeliver later", e);
                    consumer.negativeAcknowledge(msg);
                }
            }
        }).start();
    }

    private void push(String dedeuserid, String content) {
        Map<String, String> params = new HashMap<>(1);
        params.put("dedeuserid", dedeuserid);
        // 推送
        HttpPost httpPost = new HttpPost(HttpUtil.buildUri(pushUrl, params));
        List<NameValuePair> formData = new ArrayList<>();
        formData.add(new BasicNameValuePair("content", content));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);

        try (CloseableHttpClient httpClient = HttpUtil.buildHttpClient();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            log.error("请求推送接口失败", e);
            throw new RequestException("请求推送接口失败", e);
        }
    }
}
