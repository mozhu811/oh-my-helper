package io.cruii.component;

import io.cruii.pojo.entity.PushConfigDO;
import io.cruii.pojo.dto.PushConfigDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * @author cruii
 * Created on 2023/2/15
 */
@Mapper(componentModel = "spring")
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PushConfigStructMapper {
    PushConfigDO toDO(PushConfigDTO pushConfigDTO);
}
