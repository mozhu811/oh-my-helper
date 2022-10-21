package io.cruii.controller;

import io.cruii.pojo.dto.PushConfigDTO;
import io.cruii.pojo.dto.TaskConfigDTO;
import io.cruii.pojo.vo.TaskConfigVO;
import io.cruii.service.PushConfigService;
import io.cruii.service.TaskConfigService;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@RestController
@Log4j2
@RequestMapping("configs")
public class ConfigController {

    private final TaskConfigService taskConfigService;

    private final MapperFactory mapperFactory;

    private final PushConfigService pushConfigService;


    public ConfigController(TaskConfigService taskConfigService,
                            MapperFactory mapperFactory,
                            PushConfigService pushConfigService) {
        this.taskConfigService = taskConfigService;
        this.mapperFactory = mapperFactory;
        this.pushConfigService = pushConfigService;
    }

    @PostMapping("task")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskConfigDTO createTask(@CookieValue("dedeuserid") String dedeuserid,
                                    @CookieValue("sessdata") String sessdata,
                                    @CookieValue("biliJct") String biliJct,
                                    @RequestBody TaskConfigVO taskConfigVO){
        PushConfigDTO pushConfigDTO = null;
        if (Objects.nonNull(taskConfigVO.getPushConfig())){
            pushConfigDTO = mapperFactory.getMapperFacade().map(taskConfigVO.getPushConfig(), PushConfigDTO.class);
        }
        TaskConfigDTO taskConfigDTO = mapperFactory.getMapperFacade().map(taskConfigVO, TaskConfigDTO.class);

        taskConfigDTO.setDedeuserid(dedeuserid);
        taskConfigDTO.setSessdata(sessdata);
        taskConfigDTO.setBiliJct(biliJct);

        return taskConfigService.createTask(taskConfigDTO, pushConfigDTO);
    }

    @PostMapping("push")
    @ResponseStatus(HttpStatus.CREATED)
    public PushConfigDTO saveConfig(PushConfigDTO pushConfigDTO) {
        return pushConfigService.save(pushConfigDTO);
    }

    @DeleteMapping("task")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTask(@CookieValue("sessdata") String sessdata,
                           @CookieValue("biliJct") String biliJct,
                           @RequestParam String dedeuserid) {
        //BilibiliDelegate delegate = new BilibiliDelegate(dedeuserid, sessdata, biliJct);
        //BilibiliUser user = delegate.getUser();
        //if (Boolean.TRUE.equals(user.getIsLogin())) {
            taskConfigService.removeTask(dedeuserid);
        //}
    }
}
