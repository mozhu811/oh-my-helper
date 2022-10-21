package io.cruii.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.cruii.component.BilibiliDelegate;
import io.cruii.mapper.BilibiliUserMapper;
import io.cruii.mapper.TaskConfigMapper;
import io.cruii.pojo.dto.BilibiliUserDTO;
import io.cruii.pojo.po.BilibiliUser;
import io.cruii.pojo.po.TaskConfig;
import io.cruii.pojo.vo.BilibiliUserVO;
import io.cruii.service.BilibiliUserService;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
    private final MapperFactory mapperFactory;

    public BilibiliUserServiceImpl(BilibiliUserMapper bilibiliUserMapper,
                                   TaskConfigMapper taskConfigMapper,
                                   MapperFactory mapperFactory) {
        this.bilibiliUserMapper = bilibiliUserMapper;
        this.taskConfigMapper = taskConfigMapper;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public void save(String dedeuserid, String sessdata, String biliJct) {
        BilibiliDelegate delegate = new BilibiliDelegate(dedeuserid, sessdata, biliJct);
        BilibiliUser user = delegate.getUser();
        BilibiliUser exist = bilibiliUserMapper.selectOne(Wrappers.lambdaQuery(BilibiliUser.class).eq(BilibiliUser::getDedeuserid, dedeuserid));
        if (Objects.isNull(exist)) {
            user.setCreateTime(LocalDateTime.now());
            bilibiliUserMapper.insert(user);
        } else {
            user.setId(exist.getId());
            bilibiliUserMapper.updateById(user);
        }
    }

    @Override
    public void save(BilibiliUserDTO user) {
        BilibiliUser bilibiliUser = mapperFactory.getMapperFacade().map(user, BilibiliUser.class);
        BilibiliUser exist = bilibiliUserMapper.selectOne(Wrappers.lambdaQuery(BilibiliUser.class).eq(BilibiliUser::getDedeuserid, user.getDedeuserid()));
        if (Objects.isNull(exist)) {
            bilibiliUserMapper.insert(bilibiliUser);
        } else {
            bilibiliUser.setId(exist.getId());
            bilibiliUserMapper.updateById(bilibiliUser);
        }
    }

    @Override
    public boolean isExist(String dedeuserid) {
        return Objects.nonNull(bilibiliUserMapper.selectOne(Wrappers.lambdaQuery(BilibiliUser.class).eq(BilibiliUser::getDedeuserid, dedeuserid)));
    }

    @Override
    public Page<BilibiliUserVO> list(Integer page, Integer size) {
        Page<BilibiliUser> resultPage = bilibiliUserMapper.selectPage(new Page<>(page, size), Wrappers.lambdaQuery(BilibiliUser.class).orderByDesc(BilibiliUser::getCurrentExp));
        List<TaskConfig> taskConfigs = taskConfigMapper
                .selectList(Wrappers.lambdaQuery(TaskConfig.class)
                        .in(TaskConfig::getDedeuserid, resultPage.getRecords()
                                .stream()
                                .map(BilibiliUser::getDedeuserid)
                                .collect(Collectors.toList())));
        Page<BilibiliUserVO> bilibiliUserVOPage = new Page<>(page, size);
        BeanUtils.copyProperties(resultPage, bilibiliUserVOPage, "records");
        bilibiliUserVOPage.setRecords(resultPage
                .getRecords()
                .stream()
                .map(user -> {
                    MapperFacade mapper = mapperFactory.getMapperFacade();
                    BilibiliUserVO userVO = mapper.map(user, BilibiliUserVO.class);
                    taskConfigs.stream()
                            .filter(config -> config.getDedeuserid().equals(user.getDedeuserid()))
                            .findFirst()
                            .ifPresent(config -> userVO.setConfigId(config.getId()));
                    if (user.getLevel() < 6) {
                        userVO.setDiffExp(user.getNextExp() - user.getCurrentExp());
                    }
                    return userVO;
                })
                .collect(Collectors.toList()));

        return bilibiliUserVOPage;
    }

    @Override
    public List<String> listNotRunUserId() {
        return bilibiliUserMapper.listNotRunUser().stream().map(BilibiliUser::getDedeuserid).collect(Collectors.toList());
    }
}
