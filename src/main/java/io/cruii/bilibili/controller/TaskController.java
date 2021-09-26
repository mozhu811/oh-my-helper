package io.cruii.bilibili.controller;

import io.cruii.bilibili.dto.TaskConfigDTO;
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
    public Object createContainer(@RequestBody TaskConfigVO taskConfigVO) {
        TaskConfigDTO taskConfig = mapperFactory.getMapperFacade()
                .map(taskConfigVO, TaskConfigDTO.class);
        taskService.createNewTask(taskConfig);
        return null;
    }
}
