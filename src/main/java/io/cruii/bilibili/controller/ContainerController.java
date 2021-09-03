package io.cruii.bilibili.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.cruii.bilibili.dto.ContainerDTO;
import io.cruii.bilibili.dto.CreateContainerDTO;
import io.cruii.bilibili.service.ContainerService;
import io.cruii.bilibili.vo.ContainerCardVO;
import io.cruii.bilibili.vo.CreateContainerVO;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.net.HttpCookie;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@RestController
@Log4j2
@RequestMapping("containers")
public class ContainerController {

    private final ContainerService containerService;

    public ContainerController(ContainerService containerService) {
        this.containerService = containerService;
    }

    @GetMapping
    public List<ContainerCardVO> listContainers() {
        return containerService.listContainers()
                .stream()
                .map(ContainerDTO::toCardVO)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContainerCardVO createContainer(@RequestBody CreateContainerVO createContainerVO) throws FileNotFoundException {
        CreateContainerDTO createContainerDTO = createContainerVO.toDto();

        ContainerDTO container = containerService.createContainer(createContainerDTO);
        return container.toCardVO();
    }

    @DeleteMapping("{dedeuserid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContainer(@PathVariable Integer dedeuserid,
                                @CookieValue String sessdata) {
        HttpCookie sessdataCookie = new HttpCookie("SESSDATA", sessdata);
        sessdataCookie.setDomain(".bilibili.com");
        String body = HttpRequest.get("https://api.bilibili.com/x/web-interface/nav")
                .cookie(sessdataCookie)
                .execute().body();
        JSONObject data = JSONUtil.parseObj(body);
        Integer code = data.getInt("code");
        if (code == 0) {
            log.info("用户信息验证成功");
            containerService.removeContainer(dedeuserid);
        }
    }
}
