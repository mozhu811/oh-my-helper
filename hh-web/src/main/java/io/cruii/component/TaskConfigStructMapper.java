package io.cruii.component;

import io.cruii.pojo.dto.TaskConfigDTO;
import io.cruii.pojo.entity.TaskConfigDO;
import io.cruii.pojo.vo.TaskConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * @author cruii
 * Created on 2023/2/14
 */
@Mapper(componentModel = "spring")
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskConfigStructMapper {
    TaskConfigVO toVO(TaskConfigDO taskConfigDO);

    TaskConfigDO toDO(TaskConfigDTO taskConfigDTO);
}
