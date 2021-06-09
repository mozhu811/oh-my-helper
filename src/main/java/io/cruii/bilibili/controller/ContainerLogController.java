package io.cruii.bilibili.controller;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import io.cruii.bilibili.service.ContainerLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author cruii
 * Created on 2021/6/7
 */
@RestController
@RequestMapping("logs")
public class ContainerLogController {

    private final ContainerLogService containerLogService;

    public ContainerLogController(ContainerLogService containerLogService) {
        this.containerLogService = containerLogService;
    }

    @GetMapping
    public List<String> listLogs(@RequestParam String dedeuserid,
                                 @RequestParam("start") Long startTime,
                                 @RequestParam("end") Long endTime) throws TencentCloudSDKException {
        return containerLogService.listLogs(dedeuserid, startTime, endTime);
    }
}
