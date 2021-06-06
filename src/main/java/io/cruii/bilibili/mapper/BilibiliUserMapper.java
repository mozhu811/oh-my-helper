package io.cruii.bilibili.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.cruii.bilibili.entity.BilibiliUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Mapper
public interface BilibiliUserMapper extends BaseMapper<BilibiliUser> {
}
