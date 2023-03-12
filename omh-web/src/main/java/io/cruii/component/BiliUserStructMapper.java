package io.cruii.component;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.cruii.pojo.dto.BiliTaskUserDTO;
import io.cruii.pojo.entity.BiliTaskUserDO;
import io.cruii.pojo.vo.BiliTaskUserVO;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * @author cruii
 * Created on 2023/2/13
 */
@Mapper(componentModel = "spring")
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BiliUserStructMapper {
    BiliTaskUserDTO toDTO(BiliTaskUserDO biliTaskUserDO);

    BiliTaskUserVO toVO(BiliTaskUserDTO biliTaskUserDTO);

    BiliTaskUserDO toDO(BiliTaskUserDTO biliTaskUserDTO);

    Page<BiliTaskUserVO> toVOPage(Page<BiliTaskUserDO> userDOPage);

    BiliTaskUserVO toVO(BiliTaskUserDO biliTaskUserDO);
}
