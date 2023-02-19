package io.cruii.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.cruii.pojo.entity.BiliTaskUserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BilibiliUserMapper extends BaseMapper<BiliTaskUserDO> {
    @Select("select * from bilibili_user where to_days(now()) - to_days(last_run_time) >= 1 or last_run_time is null")
    List<BiliTaskUserDO> listNotRunUser();
}