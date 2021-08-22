package io.cruii.bilibili.controller;

import io.cruii.bilibili.dto.ContainerDTO;
import io.cruii.bilibili.dto.CreateContainerDTO;
import io.cruii.bilibili.service.ContainerService;
import io.cruii.bilibili.vo.ContainerCardVO;
import io.cruii.bilibili.vo.CreateContainerVO;
import io.cruii.bilibili.vo.BilibiliLoginVO;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
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

    @PutMapping("cookies")
    public ContainerCardVO updateContainer(@RequestBody BilibiliLoginVO bilibiliLoginVO) {
        log.debug("更新Cookie: {}", bilibiliLoginVO);
        return containerService.updateCookies(bilibiliLoginVO.getDedeuserid(),
                bilibiliLoginVO.getSessdata(),
                bilibiliLoginVO.getBiliJct()).toCardVO();
    }

    @DeleteMapping("{dedeuserid}")
    public void deleteContainer(@PathVariable Integer dedeuserid) {
        log.debug(dedeuserid);
    }
}
