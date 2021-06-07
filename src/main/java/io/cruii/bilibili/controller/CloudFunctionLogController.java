package io.cruii.bilibili.controller;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import io.cruii.bilibili.entity.CloudFunctionLog;
import io.cruii.bilibili.service.CloudFunctionLogService;
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
public class CloudFunctionLogController {

    private final CloudFunctionLogService cloudFunctionLogService;

    public CloudFunctionLogController(CloudFunctionLogService cloudFunctionLogService) {
        this.cloudFunctionLogService = cloudFunctionLogService;
    }

    @GetMapping
    public List<CloudFunctionLog> listLogs(@RequestParam String dedeuserid,
                                           @RequestParam("start") Long startTime,
                                           @RequestParam("end") Long endTime) throws TencentCloudSDKException {
        return cloudFunctionLogService.listLogs(dedeuserid, startTime, endTime);
    }
}
