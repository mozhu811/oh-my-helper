package io.cruii.bilibili.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.cruii.bilibili.entity.BilibiliUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BilibiliUserMapper extends BaseMapper<BilibiliUser> {

}