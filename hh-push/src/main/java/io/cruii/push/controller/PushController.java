package io.cruii.push.controller;

import cn.hutool.core.util.StrUtil;
import io.cruii.pojo.dto.PushConfigDTO;
import io.cruii.pojo.vo.PushConfigVO;
import io.cruii.push.service.PushService;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author cruii
 * Created on 2022/4/6
 */
@RestController
@RequestMapping("/push")
@Slf4j
public class PushController {
    private final PushService pushService;
    private final MapperFactory mapperFactory;

    public PushController(PushService pushService,
                          MapperFactory mapperFactory) {
        this.pushService = pushService;
        this.mapperFactory = mapperFactory;
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public void push(@RequestParam String dedeuserid, @RequestParam String content) {
        if (!StrUtil.isEmpty(content)) {
            log.info("接收到推送用户id：{}", dedeuserid);
            pushService.push(dedeuserid, content);
        } else {
            log.error("content is empty: {}", dedeuserid);
        }
    }

    @PostMapping("config")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void saveConfig(@RequestBody PushConfigVO pushConfigVO) {
        PushConfigDTO pushConfigDTO = mapperFactory.getMapperFacade().map(pushConfigVO, PushConfigDTO.class);
        pushService.save(pushConfigDTO);
    }
}
