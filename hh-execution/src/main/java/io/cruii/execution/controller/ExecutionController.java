package io.cruii.execution.controller;

import cn.hutool.core.text.CharSequenceUtil;
import io.cruii.execution.component.TaskRunner;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("execution")
public class ExecutionController {
    private final TaskRunner taskRunner;

    public ExecutionController(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    @PostMapping
    public void exe(@RequestParam(required = false) String dedeuserid) {
        if (CharSequenceUtil.isBlank(dedeuserid)) {
            
        } else {

        }
    }
}
