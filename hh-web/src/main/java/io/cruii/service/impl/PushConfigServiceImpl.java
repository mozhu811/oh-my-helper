package io.cruii.service.impl;

import io.cruii.mapper.PushConfigMapper;
import io.cruii.pojo.dto.PushConfigDTO;
import io.cruii.service.PushConfigService;
import org.springframework.stereotype.Service;

@Service
public class PushConfigServiceImpl implements PushConfigService {
    private final PushConfigMapper pushConfigMapper;


    public PushConfigServiceImpl(PushConfigMapper pushConfigMapper) {
        this.pushConfigMapper = pushConfigMapper;
    }

    @Override
    public PushConfigDTO save(PushConfigDTO pushConfigDTO) {
        return null;
    }
}
