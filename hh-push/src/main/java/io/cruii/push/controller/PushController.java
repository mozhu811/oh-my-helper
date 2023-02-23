package io.cruii.push.controller;

import io.cruii.pojo.dto.PushMessageDTO;
import io.cruii.push.service.PushService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}
