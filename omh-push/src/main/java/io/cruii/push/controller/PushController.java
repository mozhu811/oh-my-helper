package io.cruii.push.controller;

import io.cruii.pojo.dto.PushMessageDTO;
import io.cruii.push.pusher.PusherFactory;
import io.cruii.push.service.PushService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author cruii
 * Created on 2022/4/6
 */
@RestController
@RequestMapping("/push")
public class PushController {
    private final PushService pushService;

    public PushController(PushService pushService) {
        this.pushService = pushService;
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public boolean push(@RequestBody PushMessageDTO messageDTO) {
        return pushService.push(messageDTO);
    }

    @PostMapping("expired")
    public void notifyExpired(@RequestBody List<String> expiredIdList) {
        pushService.notifyExpired(expiredIdList);
    }

    @PostMapping("test")
    public void testPush(@RequestParam Integer channel, @RequestBody Object config) {
        if (!PusherFactory.create(channel, config).push("测试推送")) {
            throw new RuntimeException("推送失败");
        }
    }
}
