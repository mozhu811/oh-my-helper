package io.cruii.controller;

import io.cruii.handler.ServerHandler;
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

    public TestMessageController(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    @PostMapping
    public void testMsg() {
        serverHandler.sendMsg("12313");
    }
}
