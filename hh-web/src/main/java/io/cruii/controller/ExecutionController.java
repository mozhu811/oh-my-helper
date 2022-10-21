package io.cruii.controller;

import cn.hutool.core.text.CharSequenceUtil;
import io.cruii.handler.ServerHandler;
import io.cruii.pojo.po.TaskConfig;
import io.cruii.service.BilibiliUserService;
import io.cruii.service.TaskConfigService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("execution")
public class ExecutionController {
    private final TaskConfigService taskConfigService;

    private final BilibiliUserService bilibiliUserService;

    private final ServerHandler serverHandler;

    public ExecutionController(TaskConfigService taskConfigService,
                               BilibiliUserService bilibiliUserService,
                               ServerHandler serverHandler) {
        this.taskConfigService = taskConfigService;
        this.bilibiliUserService = bilibiliUserService;
        this.serverHandler = serverHandler;
    }

    @PostMapping
    public void exe(@RequestParam(required = false) String dedeuserid) {
        if (!CharSequenceUtil.isBlank(dedeuserid)) {
            TaskConfig taskConfig = taskConfigService.getTask(dedeuserid);
            serverHandler.sendMsg(taskConfig);
        } else {
            List<String> dedeuseridList = bilibiliUserService.listNotRunUserId();
            taskConfigService.getTask(dedeuseridList)
                    .forEach(serverHandler::sendMsg);
        }
    }
}
