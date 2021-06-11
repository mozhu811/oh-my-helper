package io.cruii.bilibili.service;

import io.cruii.bilibili.dto.ContainerDTO;
import io.cruii.bilibili.dto.CreateContainerDTO;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author cruii
 * Created on 2021/6/6
 */
public interface ContainerService {

    List<ContainerDTO> listContainers();

    ContainerDTO createContainer(CreateContainerDTO createContainerDTO) throws FileNotFoundException;

    void updateTrigger(String containerName, String cronExpression);

    void removeContainer(String containerName);
}
