package io.cruii.bilibili.controller;

import io.cruii.bilibili.component.BilibiliDelegate;
import io.cruii.bilibili.dto.TaskConfigDTO;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.service.TaskService;
import io.cruii.bilibili.vo.TaskConfigVO;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createTask(@CookieValue("dedeuserid") String dedeuserId,
                           @CookieValue("sessdata") String sessdata,
                           @CookieValue("biliJct") String biliJct,
                           @RequestBody TaskConfigVO taskConfigVO) throws InterruptedException {
        TaskConfigDTO taskConfig = mapperFactory.getMapperFacade()
                .map(taskConfigVO, TaskConfigDTO.class);

        taskConfig.setDedeuserid(dedeuserId);
        taskConfig.setSessdata(sessdata);
        taskConfig.setBiliJct(biliJct);

        log.debug(taskConfig);
        taskService.createTask(taskConfig);
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
