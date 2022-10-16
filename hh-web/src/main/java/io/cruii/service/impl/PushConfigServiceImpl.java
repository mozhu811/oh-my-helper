package io.cruii.service.impl;

import io.cruii.mapper.PushConfigMapper;
import io.cruii.pojo.dto.PushConfigDTO;
import io.cruii.pojo.po.PushConfig;
import io.cruii.service.PushConfigService;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Service;

@Service
public class PushConfigServiceImpl implements PushConfigService {
    private final PushConfigMapper pushConfigMapper;

    private final MapperFactory mapperFactory;

    public PushConfigServiceImpl(PushConfigMapper pushConfigMapper,
                                 MapperFactory mapperFactory) {
        this.pushConfigMapper = pushConfigMapper;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public PushConfigDTO save(PushConfigDTO pushConfigDTO) {
        MapperFacade facade = mapperFactory.getMapperFacade();
        PushConfig pushConfig = facade.map(pushConfigDTO, PushConfig.class);
        pushConfigMapper.insert(pushConfig);
        return facade.map(pushConfig, PushConfigDTO.class);
    }
}
