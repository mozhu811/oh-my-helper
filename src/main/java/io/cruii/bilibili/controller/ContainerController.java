package io.cruii.bilibili.controller;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import io.cruii.bilibili.dto.ContainerDTO;
import io.cruii.bilibili.service.ContainerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@RestController
@RequestMapping("containers")
public class ContainerController {

    private final ContainerService containerService;

    public ContainerController(ContainerService containerService) {
        this.containerService = containerService;
    }

    @GetMapping
    public List<ContainerDTO> listContainers() throws TencentCloudSDKException {
        return containerService.listContainers();
    }

    @PostMapping
    public ContainerDTO createContainer(@CookieValue String dedeUserId,
                                        @CookieValue String sessData,
                                        @CookieValue String biliJct) {
        containerService.createContainer(dedeUserId, sessData, biliJct);
        return null;
    }

}
