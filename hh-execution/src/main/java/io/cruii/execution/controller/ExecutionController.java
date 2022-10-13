package io.cruii.execution.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cruii
 * Created on 2022/9/8
 */
@RestController
@RequestMapping("task")
public class ExecutionController {

    @PostMapping
    public void exec(@RequestParam String dedeuserid){

    }
}
