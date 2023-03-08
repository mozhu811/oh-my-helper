package io.cruii.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.cruii.component.BiliUserStructMapper;
import io.cruii.component.BilibiliDelegate;
import io.cruii.mapper.BilibiliUserMapper;
import io.cruii.mapper.TaskConfigMapper;
import io.cruii.model.BiliUser;
import io.cruii.model.MedalWall;
import io.cruii.model.SpaceAccInfo;
import io.cruii.pojo.dto.BiliTaskUserDTO;
import io.cruii.pojo.entity.BiliTaskUserDO;
import io.cruii.pojo.entity.TaskConfigDO;
import io.cruii.pojo.vo.BiliTaskUserVO;
import io.cruii.service.BilibiliUserService;
import io.cruii.util.MedalWall2StrUtil;
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
        BilibiliDelegate delegate = new BilibiliDelegate(dedeuserid, sessdata, biliJct, false);

        // 从B站获取最新用户信息
        BiliUser biliUser = delegate.getUserDetails();

        BiliTaskUserDO biliUserDO = new BiliTaskUserDO();
        biliUserDO.setDedeuserid(String.valueOf(biliUser.getMid()))
                .setUsername(biliUser.getName())
                .setCoins(String.valueOf(biliUser.getCoins()))
                .setLevel(biliUser.getLevel())
                .setCurrentExp(biliUser.getLevelExp().getCurrentExp())
                .setNextExp(biliUser.getLevel() == 6 ? 0 : biliUser.getLevelExp().getNextExp())
                .setSign(biliUser.getSign())
                .setVipStatus(biliUser.getVip().getStatus())
                .setVipType(biliUser.getVip().getType())
                .setVipLabelTheme(biliUser.getVip().getLabel().getLabelTheme())
                .setIsLogin(true);

        biliUserDO.setMedals(MedalWall2StrUtil.medalWall2JsonStr(delegate.getMedalWall()));

        // 是否存在用户信息
        boolean exist = bilibiliUserMapper
                .exists(Wrappers.lambdaQuery(BiliTaskUserDO.class)
                        .eq(BiliTaskUserDO::getDedeuserid, dedeuserid));

        if (!exist) {
            biliUserDO.setCreateTime(LocalDateTime.now());
            bilibiliUserMapper.insert(biliUserDO);
        } else {
            bilibiliUserMapper.updateById(biliUserDO);
        }

        // 是否存在任务配置
        boolean existConfig = taskConfigMapper.exists(Wrappers.lambdaQuery(TaskConfigDO.class).eq(TaskConfigDO::getDedeuserid, dedeuserid));
        if (!existConfig) {
            // 初始化任务配置
            initTask(dedeuserid, sessdata, biliJct);
        }
    }

    private void initTask(String dedeuserid, String sessdata, String biliJct) {
        TaskConfigDO taskConfigDO = new TaskConfigDO();
        taskConfigDO.setDedeuserid(dedeuserid);
        taskConfigDO.setSessdata(sessdata);
        taskConfigDO.setBiliJct(biliJct);
        taskConfigDO.setAutoCharge(true);
        taskConfigDO.setAutoChargeTarget(dedeuserid);
        taskConfigDO.setDevicePlatform("ios");
        taskConfigDO.setDonateCoins(5);
        taskConfigDO.setDonateCoinStrategy(0);
        taskConfigDO.setDonateGift(true);
        taskConfigDO.setDonateGiftTarget(dedeuserid);
        taskConfigDO.setReserveCoins(100);
        taskConfigDO.setFollowDeveloper(true);

        taskConfigMapper.insert(taskConfigDO);
    }

    @Override
    public void save(BiliTaskUserDTO userDTO) {
        TaskConfigDO taskConfigDO = taskConfigMapper.selectOne(Wrappers.lambdaQuery(TaskConfigDO.class)
                .eq(TaskConfigDO::getDedeuserid, userDTO.getDedeuserid()));
        BilibiliDelegate delegate = new BilibiliDelegate(taskConfigDO.getDedeuserid(), taskConfigDO.getSessdata(), taskConfigDO.getBiliJct());
        MedalWall medalWall = delegate.getMedalWall();
        String medalWall2JsonStr;
        BiliTaskUserDO biliTaskUserDO = biliUserStructMapper.toDO(userDTO);
        if (medalWall != null) {
            medalWall2JsonStr = MedalWall2StrUtil.medalWall2JsonStr(medalWall);
            biliTaskUserDO.setMedals(medalWall2JsonStr);
        }
        SpaceAccInfo spaceAccInfo = delegate.getSpaceAccInfo(userDTO.getDedeuserid());
        if (spaceAccInfo != null) {
            biliTaskUserDO.setSign(spaceAccInfo.getSign());
        }
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
