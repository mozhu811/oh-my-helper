package io.cruii.controller;

import cn.hutool.json.JSONUtil;
import io.cruii.component.BilibiliDelegate;
import io.cruii.exception.RequestException;
import io.cruii.pojo.po.BilibiliUser;
import io.cruii.pojo.dto.TaskConfigDTO;
import io.cruii.pojo.vo.PushConfigVO;
import io.cruii.pojo.vo.TaskConfigVO;
import io.cruii.service.TaskService;
import io.cruii.util.HttpUtil;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@RestController
@Log4j2
@RequestMapping("tasks")
public class TaskController {

    private final TaskService taskService;
    private final MapperFactory mapperFactory;

    public TaskController(TaskService taskService, MapperFactory mapperFactory) {
        this.taskService = taskService;
        this.mapperFactory = mapperFactory;
    }

    @Value("${push.url}")
    private String pushUrl;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createTask(@CookieValue("dedeuserid") String dedeuserid,
                           @CookieValue("sessdata") String sessdata,
                           @CookieValue("biliJct") String biliJct,
                           @RequestBody TaskConfigVO taskConfigVO) throws InterruptedException, PulsarClientException {
        TaskConfigDTO taskConfig = mapperFactory.getMapperFacade()
                .map(taskConfigVO, TaskConfigDTO.class);

        taskConfig.setDedeuserid(dedeuserid);
        taskConfig.setSessdata(sessdata);
        taskConfig.setBiliJct(biliJct);

        taskService.createTask(taskConfig);

        CompletableFuture.runAsync(() -> {
            PushConfigVO pushConfig = taskConfigVO.getPushConfig();
            pushConfig.setDedeuserid(dedeuserid);
            HttpPost httpPost = new HttpPost(pushUrl + "/config");
            StringEntity stringEntity = new StringEntity(JSONUtil.toJsonStr(pushConfig), "utf-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            try (CloseableHttpClient httpClient = HttpUtil.buildHttpClient();
                 CloseableHttpResponse response = httpClient.execute(httpPost)) {
                EntityUtils.consume(response.getEntity());
            } catch (IOException e) {
                throw new RequestException("请求推送接口失败", e);
            }
        });
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTask(@CookieValue("dedeuserid") String dedeuserid,
                           @CookieValue("sessdata") String sessdata,
                           @CookieValue("biliJct") String biliJct) {
        BilibiliDelegate delegate = new BilibiliDelegate(dedeuserid, sessdata, biliJct);
        BilibiliUser user = delegate.getUser();
        if (Boolean.TRUE.equals(user.getIsLogin())) {
            taskService.removeTask(dedeuserid);
        }
    }
}
