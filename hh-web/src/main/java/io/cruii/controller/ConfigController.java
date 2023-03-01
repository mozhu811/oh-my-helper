package io.cruii.controller;

import io.cruii.pojo.dto.PushConfigDTO;
import io.cruii.pojo.dto.TaskConfigDTO;
import io.cruii.service.BilibiliUserService;
import io.cruii.service.PushConfigService;
import io.cruii.service.TaskConfigService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
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

    private final BilibiliUserService biliUserService;

    public ConfigController(TaskConfigService taskConfigService,
                            PushConfigService pushConfigService,
                            BilibiliUserService biliUserService) {
        this.taskConfigService = taskConfigService;
        this.pushConfigService = pushConfigService;
        this.biliUserService = biliUserService;
    }

    @PostMapping("task")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTask(@CookieValue("dedeuserid") String dedeuserid,
                           @CookieValue("sessdata") String sessdata,
                           @CookieValue("biliJct") String biliJct,
                           @RequestBody TaskConfigDTO taskConfigDTO) {
        biliUserService.save(dedeuserid, sessdata, biliJct);
        taskConfigService.createTask(dedeuserid, sessdata, biliJct, taskConfigDTO);
    }

    @PostMapping("push")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveConfig(PushConfigDTO pushConfigDTO) {
        pushConfigService.save(pushConfigDTO);
    }

    @DeleteMapping("task")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTask(@CookieValue("sessdata") String sessdata,
                           @CookieValue("biliJct") String biliJct,
                           @RequestParam String dedeuserid) {
        // todo 验证信息
        taskConfigService.removeTask(dedeuserid);
    }
}
