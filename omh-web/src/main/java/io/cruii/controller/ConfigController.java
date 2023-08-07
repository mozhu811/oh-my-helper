package io.cruii.controller;

import io.cruii.model.pusher.PusherConfigDTO;
import io.cruii.model.pusher.PusherConfigVO;
import io.cruii.pojo.dto.TaskConfigDTO;
import io.cruii.pojo.vo.TaskConfigVO;
import io.cruii.service.PushConfigService;
import io.cruii.service.TaskConfigService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@RestController
@Log4j2
@RequestMapping("configs")
public class ConfigController {

    private final TaskConfigService taskConfigService;

    private final PushConfigService pushConfigService;

    public ConfigController(TaskConfigService taskConfigService,
                            PushConfigService pushConfigService) {
        this.taskConfigService = taskConfigService;
        this.pushConfigService = pushConfigService;
    }

    @PostMapping("task")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTask(@CookieValue("dedeuserid") String dedeuserid,
                           @CookieValue("sessdata") String sessdata,
                           @CookieValue("biliJct") String biliJct,
                           @Validated @RequestBody TaskConfigDTO taskConfigDTO) {
        taskConfigService.saveOrUpdate(dedeuserid, sessdata, biliJct, taskConfigDTO);
    }

    @PostMapping("push")
    @ResponseStatus(HttpStatus.CREATED)
    public PusherConfigVO createPusherConfig(@CookieValue("dedeuserid") String dedeuserid,
                                             @Validated @RequestBody PusherConfigDTO pusherConfigDTO) {
        return pushConfigService.saveOrUpdate(dedeuserid, pusherConfigDTO);
    }

    @GetMapping("task")
    public TaskConfigVO getTaskConfig(@CookieValue("dedeuserid") String dedeuserid,
                                      @CookieValue("sessdata") String sessdata,
                                      @CookieValue("biliJct") String biliJct) {
        return taskConfigService.get(dedeuserid, sessdata, biliJct);
    }

    @GetMapping("push")
    public PusherConfigVO getPushConfig(@CookieValue("dedeuserid") String dedeuserid,
                                        @CookieValue("sessdata") String sessdata,
                                        @CookieValue("biliJct") String biliJct) {
        return pushConfigService.get(dedeuserid, sessdata, biliJct);
    }

    @DeleteMapping("task")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTask(@CookieValue("sessdata") String sessdata,
                           @CookieValue("biliJct") String biliJct,
                           @RequestParam String dedeuserid) {
        taskConfigService.remove(dedeuserid, sessdata, biliJct);
    }
}
