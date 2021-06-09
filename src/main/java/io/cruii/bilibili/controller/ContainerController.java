package io.cruii.bilibili.controller;

import io.cruii.bilibili.dto.ContainerDTO;
import io.cruii.bilibili.dto.CreateContainerDTO;
import io.cruii.bilibili.service.ContainerService;
import io.cruii.bilibili.vo.ContainerCardVO;
import io.cruii.bilibili.vo.CreateContainerVO;
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
    public ContainerCardVO createContainer(@CookieValue String dedeUserId,
                                           @CookieValue String sessData,
                                           @CookieValue String biliJct,
                                           @RequestBody CreateContainerVO createContainerVO) throws FileNotFoundException {
        CreateContainerDTO createContainerDTO = createContainerVO.toDto();
        createContainerDTO.setDedeuserid(dedeUserId);
        createContainerDTO.setSessdata(sessData);
        createContainerDTO.setBiliJct(biliJct);

        ContainerDTO container = containerService.createContainer(createContainerDTO);
        return container.toCardVO();
    }

}
