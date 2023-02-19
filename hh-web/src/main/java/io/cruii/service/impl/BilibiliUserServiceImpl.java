package io.cruii.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.cruii.component.BiliUserStructMapper;
import io.cruii.component.BilibiliDelegate;
import io.cruii.mapper.BilibiliUserMapper;
import io.cruii.mapper.TaskConfigMapper;
import io.cruii.model.MedalWall;
import io.cruii.model.BiliUser;
import io.cruii.pojo.dto.BiliTaskUserDTO;
import io.cruii.pojo.entity.BiliTaskUserDO;
import io.cruii.pojo.vo.BiliTaskUserVO;
import io.cruii.service.BilibiliUserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/9/22
 */
@Service
@Log4j2
public class BilibiliUserServiceImpl implements BilibiliUserService {
    private final BilibiliUserMapper bilibiliUserMapper;
    private final TaskConfigMapper taskConfigMapper;
    private final BiliUserStructMapper biliUserStructMapper;

    public BilibiliUserServiceImpl(BilibiliUserMapper bilibiliUserMapper,
                                   TaskConfigMapper taskConfigMapper,
                                   BiliUserStructMapper biliUserStructMapper) {
        this.bilibiliUserMapper = bilibiliUserMapper;
        this.taskConfigMapper = taskConfigMapper;
        this.biliUserStructMapper = biliUserStructMapper;
    }

    @Override
    public void save(String dedeuserid, String sessdata, String biliJct) {
        BilibiliDelegate delegate = new BilibiliDelegate(dedeuserid, sessdata, biliJct);

        // 从B站获取最新用户信息
        BiliUser biliUser = delegate.getUserDetails();
        MedalWall medalWall = delegate.getMedalWall();

        BiliTaskUserDO biliUserDO = new BiliTaskUserDO();
        biliUserDO.setDedeuserid(String.valueOf(biliUser.getMid()))
                .setUsername(biliUser.getName())
                .setCoins(String.valueOf(biliUser.getCoins()))
                .setLevel(biliUser.getLevel())
                .setCurrentExp(biliUser.getLevelExp().getCurrentExp())
                .setNextExp(biliUser.getLevel() == 6 ? 0 : biliUser.getLevelExp().getNextExp())
                .setSign(CharSequenceUtil.isBlank(biliUser.getSign().trim()) ?
                        "这个人非常懒，什么也没有写~\\(≧▽≦)/~" : biliUser.getSign())
                .setVipType(biliUser.getVip().getType())
                .setVipStatus(biliUser.getVip().getStatus())
                .setIsLogin(true);

        List<MedalWall.Medal> medals = medalWall.getList();
        String medalWallStr = JSONUtil.toJsonStr(
                medals.stream()
                        .map(MedalWall.Medal::getMedalInfo)
                        .sorted((m1, m2) -> m2.getLevel() - m1.getLevel())
                        .limit(2L)
                        .map(mi -> {
                            JSONObject obj = JSONUtil.createObj();
                            obj.set("name", mi.getMedalName())
                                    .set("level", mi.getLevel())
                                    .set("colorStart", mi.getMedalColorStart())
                                    .set("colorEnd", mi.getMedalColorEnd())
                                    .set("colorBorder", mi.getMedalColorBorder());
                            return obj;
                        }).collect(Collectors.toList())
        );
        biliUserDO.setMedals(medalWallStr);

        // 是否已存在
        boolean exist = bilibiliUserMapper
                .exists(Wrappers.lambdaQuery(BiliTaskUserDO.class)
                        .eq(BiliTaskUserDO::getDedeuserid, dedeuserid));

        if (!exist) {
            biliUserDO.setCreateTime(LocalDateTime.now());
            bilibiliUserMapper.insert(biliUserDO);
        } else {
            bilibiliUserMapper.updateById(biliUserDO);
        }
    }

    @Override
    public void save(BiliTaskUserDTO userDTO) {
        BiliTaskUserDO biliTaskUserDO = biliUserStructMapper.toDO(userDTO);
        boolean exists = bilibiliUserMapper.exists(Wrappers.lambdaQuery(BiliTaskUserDO.class)
                .eq(BiliTaskUserDO::getDedeuserid, userDTO.getDedeuserid()));
        if (!exists) {
            bilibiliUserMapper.insert(biliTaskUserDO);
        } else {
            bilibiliUserMapper.updateById(biliTaskUserDO);
        }
    }

    @Override
    public boolean isExist(String dedeuserid) {
        return bilibiliUserMapper.exists(Wrappers.lambdaQuery(BiliTaskUserDO.class).eq(BiliTaskUserDO::getDedeuserid, dedeuserid));
    }

    @Override
    public Page<BiliTaskUserVO> list(Integer page, Integer size) {
        Page<BiliTaskUserDO> resultPage = bilibiliUserMapper.selectPage(new Page<>(page, size),
                Wrappers.lambdaQuery(BiliTaskUserDO.class)
                        .orderByDesc(BiliTaskUserDO::getIsLogin)
                        .orderByDesc(BiliTaskUserDO::getLevel)
                        .orderByDesc(BiliTaskUserDO::getCurrentExp));

        return biliUserStructMapper.toVOPage(resultPage);
    }

    @Override
    public List<String> listNotRunUserId() {
        return bilibiliUserMapper.listNotRunUser().stream().map(BiliTaskUserDO::getDedeuserid).collect(Collectors.toList());
    }
}
