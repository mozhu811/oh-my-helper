package io.cruii.execution.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author cruii
 * Created on 2022/9/7
 */
@FeignClient(name = "hh-push-service", path = "/push")
public interface PushFeignService {
    @PostMapping()
    boolean push(@RequestParam String dedeuserid, @RequestParam String content);
}
