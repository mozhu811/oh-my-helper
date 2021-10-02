package io.cruii.bilibili.service.impl;

import io.cruii.bilibili.component.BilibiliDelegate;
import io.cruii.bilibili.dao.BilibiliUserRepository;
import io.cruii.bilibili.dto.BilibiliUserDTO;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.service.BilibiliUserService;
import io.cruii.bilibili.vo.BilibiliUserVO;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/9/22
 */
@Service
@Log4j2
public class BilibiliUserServiceImpl implements BilibiliUserService {
    private final BilibiliUserRepository bilibiliUserRepository;
    private final MapperFactory mapperFactory;

    public BilibiliUserServiceImpl(BilibiliUserRepository bilibiliUserRepository,
                                   MapperFactory mapperFactory) {
        this.bilibiliUserRepository = bilibiliUserRepository;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public void save(String dedeuserid, String sessdata, String biliJct) {

        BilibiliDelegate delegate = new BilibiliDelegate(dedeuserid, sessdata, biliJct);
        BilibiliUser user = delegate.getUser();

        Optional<BilibiliUser> exist = bilibiliUserRepository
                .findOne(dedeuserid);
        exist.ifPresent(bilibiliUser -> user.setId(bilibiliUser.getId()));
        bilibiliUserRepository.saveAndFlush(user);
    }

    @Override
    public void save(BilibiliUserDTO user) {
        BilibiliUser bilibiliUser = mapperFactory.getMapperFacade().map(user, BilibiliUser.class);
        bilibiliUserRepository.saveAndFlush(bilibiliUser);
    }

    @Override
    public boolean isExist(String dedeuserid) {
        return bilibiliUserRepository.findOne(dedeuserid).isPresent();
    }

    @Override
    public List<BilibiliUserVO> list(HttpServletRequest request) {
        return bilibiliUserRepository.findAll()
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
