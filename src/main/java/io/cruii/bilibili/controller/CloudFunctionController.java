package io.cruii.bilibili.controller;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import io.cruii.bilibili.dto.BilibiliUserDTO;
import io.cruii.bilibili.service.CloudFunctionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@RestController
@RequestMapping("functions")
public class CloudFunctionController {

    private final CloudFunctionService cloudFunctionService;

    public CloudFunctionController(CloudFunctionService cloudFunctionService) {
        this.cloudFunctionService = cloudFunctionService;
    }

    @GetMapping
    public List<BilibiliUserDTO> listFunctions() throws TencentCloudSDKException {
        return cloudFunctionService.listFunctions();
    }
}
