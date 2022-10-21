package io.cruii.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.cruii.pojo.po.BilibiliUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BilibiliUserMapper extends BaseMapper<BilibiliUser> {
    @Select("select * from bilibili_user where to_days(now()) - to_days(last_run_time) >= 1")
    List<BilibiliUser> listNotRunUser();
}