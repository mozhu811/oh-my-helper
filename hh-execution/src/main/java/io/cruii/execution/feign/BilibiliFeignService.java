package io.cruii.execution.feign;

import io.cruii.pojo.vo.BiliTaskUserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "hh-web-service", path = "/bilibili")
public interface BilibiliFeignService {
    @PutMapping("user")
    BiliTaskUserVO updateUser(@RequestBody BiliTaskUserVO biliTaskUserVO);
}
