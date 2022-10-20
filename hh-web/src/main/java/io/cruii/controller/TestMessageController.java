package io.cruii.controller;

import io.cruii.handler.ServerHandler;
import io.cruii.pojo.po.TaskConfig;
import io.cruii.service.BilibiliUserService;
import io.cruii.service.TaskService;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cruii
 * Created on 2022/10/18
 */
@RestController
@Log4j2
@RequestMapping("messages")
public class TestMessageController {

    private final ServerHandler serverHandler;

    private final BilibiliUserService bilibiliUserService;
    private final TaskService taskService;
    public TestMessageController(ServerHandler serverHandler, BilibiliUserService bilibiliUserService, TaskService taskService) {
        this.serverHandler = serverHandler;
        this.bilibiliUserService = bilibiliUserService;
        this.taskService = taskService;
    }

    @PostMapping
    public void testMsg() {
        TaskConfig taskConfig = taskService.getTask("287969457");
        serverHandler.sendMsg(taskConfig);
    }
}
