package io.cruii.bilibili.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.bilibili.component.BilibiliDelegate;
import io.cruii.bilibili.dto.BilibiliUserDTO;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.mapper.BilibiliUserMapper;
import io.cruii.bilibili.service.BilibiliUserService;
import io.cruii.bilibili.vo.BilibiliUserVO;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
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
    private final MapperFactory mapperFactory;

    public BilibiliUserServiceImpl(BilibiliUserMapper bilibiliUserMapper,
                                   MapperFactory mapperFactory) {
        this.bilibiliUserMapper = bilibiliUserMapper;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public void save(String dedeuserid, String sessdata, String biliJct) {

        BilibiliDelegate delegate = new BilibiliDelegate(dedeuserid, sessdata, biliJct);
        BilibiliUser user = delegate.getUser();

        BilibiliUser exist = bilibiliUserMapper.selectOne(Wrappers.lambdaQuery(BilibiliUser.class).eq(BilibiliUser::getDedeuserid, dedeuserid));
        if (Objects.isNull(exist)) {
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
    public List<BilibiliUserVO> list(HttpServletRequest request) {
        return bilibiliUserMapper
                .selectList(null)
                .stream()
                .sorted(Comparator.comparingInt(BilibiliUser::getCurrentExp).reversed())
                .map(user -> {
                    MapperFacade mapper = mapperFactory.getMapperFacade();
                    BilibiliUserVO userVO = mapper.map(user, BilibiliUserVO.class);

                    if (user.getLevel() < 6) {
                        userVO.setDiffExp(user.getNextExp() - user.getCurrentExp());
                    }
                    return userVO;
                })
                .collect(Collectors.toList());
    }
}
